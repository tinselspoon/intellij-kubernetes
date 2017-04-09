package com.github.tinselspoon.intellij.kubernetes.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.tinselspoon.intellij.kubernetes.ResourceTypeKey;

/**
 * Provides information on the schema of Kubernetes resources.
 */
public class ModelProvider {

    /** Singleton instance. */
    public static final ModelProvider INSTANCE = new ModelProvider();

    private final ModelLoader modelLoader = new ModelLoader();

    /** The specs currently loaded. */
    private List<SwaggerSpec> specs;

    /** Singleton private constructor. */
    private ModelProvider() {
    }

    /**
     * Find the model that governs the property described by navigating from the base model of the {@link ResourceTypeKey} through the properties given in the {@code path}.
     *
     * @param resourceTypeKey the resource at which to begin the search.
     * @param path a series of properties to navigate through, may be empty to return the root model of the {@code ResourceTypeKey}.
     * @return the model, or {@code null} if one cannot be found.
     */
    @Nullable
    public Model findModel(final ResourceTypeKey resourceTypeKey, final List<String> path) {
        final SwaggerSpec spec = getSpec(resourceTypeKey);
        final String search = modelIdFromResourceKey(resourceTypeKey);
        if (spec != null) {
            Model model = spec.getModels().get(search);
            for (final String target : path) {
                if (model != null) {
                    final Property property = model.getProperties().get(target);
                    if (property != null) {
                        if (property.getRef() != null) {
                            // Look up the ref for the referenced object
                            model = spec.getModels().get(property.getRef());
                            continue;
                        } else if (property.getType() == FieldType.ARRAY && property.getItems() != null && property.getItems().getRef() != null) {
                            // Look up the ref for the array items
                            model = spec.getModels().get(property.getItems().getRef());
                            continue;
                        }
                    }
                    model = null;
                }
            }
            return model;
        }
        return null;
    }

    /**
     * Find the properties that may exist as children of the property described by navigating from the base model of the {@link ResourceTypeKey} through the properties given in the {@code path}.
     *
     * @param resourceTypeKey the resource at which to begin the search.
     * @param path a series of properties to navigate through, may be empty to return the properties that may be defined on the root of the {@code ResourceTypeKey}.
     * @return the map of property names to property specifications, may be empty if none can be found.
     */
    @NotNull
    public Map<String, Property> findProperties(final ResourceTypeKey resourceTypeKey, final List<String> path) {
        final SwaggerSpec spec = getSpec(resourceTypeKey);
        if (spec != null) {
            return findProperties(spec, resourceTypeKey, path);
        }
        return Collections.emptyMap();
    }

    /**
     * Suggest a set of values for the "apiVersion" field.
     *
     * @return all possible API versions.
     */
    @NotNull
    public Set<String> suggestApiVersions() {
        return getSpecs().stream().map(SwaggerSpec::getApiVersion).filter(v -> v != null && !"".equals(v)).collect(Collectors.toSet());
    }

    /**
     * Suggest a set of values for the "kind" field.
     *
     * @param apiVersion the API version for which kinds will be returned; if null, then kinds for all API versions will be returned.
     * @return a set of possible kinds.
     */
    public Set<ResourceTypeKey> suggestKinds(@Nullable final String apiVersion) {
        final Stream<SwaggerSpec> applicableSpecs;
        if (apiVersion == null) {
            applicableSpecs = getSpecs().stream();
        } else {
            applicableSpecs = getSpecs().stream().filter(s -> apiVersion.equals(s.getApiVersion()));
        }

        // Make a map of kinds to apiVersions - this is not the final data structure we want but is helpful for when we preserve only the most recent API version for a particular kind
        // TODO This does assume that no two API groups will declare the same kind - currently this doesn't happen but the Kubernetes API structure does allow for it
        Map<String, String> typeKeys = new HashMap<>();
        // Suggest any resource that appears as a return type from an API request
        applicableSpecs.forEach(s -> {
            final Set<String> apiTypes = new HashSet<>();
            final Set<String> models = new HashSet<>();
            for (final Api api : s.getApis()) {
                for (final ApiOperation operation : api.getOperations()) {
                    if ("POST".equals(operation.getMethod())) {
                        apiTypes.add(operation.getType());
                    }
                }

            }
            for (final Model model : s.getModels().values()) {
                models.add(model.getId());
            }
            // Make sure all API types have an associated model, otherwise there isn't much point in suggesting them as we can't offer anything useful
            apiTypes.retainAll(models);
            for (final String apiType : apiTypes) {
                String kind = stripModelIdPrefix(apiType);
                // Only keep the "highest" API version for a kind to ensure we are using the latest version available
                typeKeys.merge(kind, s.getApiVersion(), (a, b) -> ApiVersionComparator.INSTANCE.compare(a, b) > 0 ? a : b);
            }
        });

        // Convert map of type keys to ResourceTypeKey objects
        return typeKeys.entrySet().stream().map(e -> new ResourceTypeKey(e.getValue(), e.getKey())).collect(Collectors.toSet());
    }

    /**
     * Gets the child properties of the given {@link Property}. This is done by resolving the property's {@link Property#getRef() ref} to a model and obtaining the properties defined there.
     *
     * @param spec the spec to search for references in.
     * @param property the property to obtain children for; if {@code null} an empty map is returned.
     * @return the child properties, or an empty map if no properties could be resolved - e.g. if the property has no ref or the model referenced does not exist.
     */
    @NotNull
    private Map<String, Property> findChildProperties(@NotNull final SwaggerSpec spec, @Nullable final Property property) {
        if (property != null) {
            if (property.getRef() != null) {
                // Look up the ref for the referenced object
                return getModelProperties(spec, property.getRef());
            } else if (property.getType() == FieldType.ARRAY && property.getItems() != null && property.getItems().getRef() != null) {
                // Look up the ref for the array items
                return getModelProperties(spec, property.getItems().getRef());
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Find the properties that may exist as children of the property described by navigating from the base model of the {@link ResourceTypeKey} through the properties given in the {@code path}.
     *
     * @param spec the spec to search within.
     * @param resourceTypeKey the resource at which to begin the search.
     * @param path a series of properties to navigate through, may be empty to return the properties that may be defined on the root of the {@code ResourceTypeKey}.
     * @return the map of property names to property specifications, may be empty if none can be found.
     */
    @NotNull
    private Map<String, Property> findProperties(final SwaggerSpec spec, final ResourceTypeKey resourceTypeKey, final List<String> path) {
        final String search = modelIdFromResourceKey(resourceTypeKey);
        final Model startingModel = spec.getModels().get(search);
        if (startingModel != null) {
            Map<String, Property> properties = startingModel.getProperties();
            for (final String targetKey : path) {
                final Property property = properties.get(targetKey);
                properties = findChildProperties(spec, property);
            }
            return properties;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets the properties for the model with the specified ID.
     *
     * @param spec the spec to search within.
     * @param modelId the model ID.
     * @return the model properties, or an empty map if a model with the specified ID cannot be located.
     */
    @NotNull
    private Map<String, Property> getModelProperties(final SwaggerSpec spec, final String modelId) {
        return spec.getModels().values().stream().filter(m -> modelId.equals(m.getId())).map(Model::getProperties).findAny().orElse(Collections.emptyMap());
    }

    /**
     * Gets the {@link SwaggerSpec} that contains a definition for the given resource key.
     *
     * @param resourceTypeKey the resource key to search for.
     * @return the corresponding spec, or {@code null} if one cannot be found.
     */
    @Nullable
    private SwaggerSpec getSpec(@NotNull final ResourceTypeKey resourceTypeKey) {
        final String apiVersion = resourceTypeKey.getApiVersion();
        final String modelId = modelIdFromResourceKey(resourceTypeKey);
        return getSpecs().stream().filter(s -> apiVersion.equals(s.getApiVersion()) && s.getModels().containsKey(modelId)).findAny().orElse(null);
    }

    /**
     * Get a set of loaded {@link SwaggerSpec}s, initialising if necessary.
     *
     * @return the swagger specs.
     */
    @NotNull
    private List<SwaggerSpec> getSpecs() {
        return modelLoader.getActiveSpecs();
    }

    /**
     * Generates the model ID from the specified {@link ResourceTypeKey}.
     * <p>
     * This is achieved by concatenating the "version" part (e.g. {@code v1} in {@code batch/v1}) of the resource key {@linkplain ResourceTypeKey#apiVersion API version}, a dot, and the {@linkplain
     * ResourceTypeKey#getKind() kind}.
     *
     * @param resourceTypeKey the resource type key.
     * @return the model ID.
     */
    @NotNull
    private String modelIdFromResourceKey(final ResourceTypeKey resourceTypeKey) {
        String resourceApiVersion = resourceTypeKey.getApiVersion();
        if (resourceApiVersion.indexOf('/') > -1) {
            resourceApiVersion = resourceApiVersion.substring(resourceApiVersion.indexOf('/') + 1);
        }
        return resourceApiVersion + "." + resourceTypeKey.getKind();
    }

    /**
     * Removes the API version prefix from a model identifier.
     *
     * @param modelId the model ID to clean.
     * @return the cleaned model ID.
     */
    @NotNull
    private String stripModelIdPrefix(@NotNull final String modelId) {
        final int dot = modelId.indexOf('.');
        if (dot > -1) {
            return modelId.substring(dot + 1);
        } else {
            return modelId;
        }
    }

}
