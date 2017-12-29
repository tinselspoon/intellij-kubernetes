package com.github.tinselspoon.intellij.kubernetes.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jetbrains.annotations.NotNull;

import com.github.tinselspoon.intellij.kubernetes.config.ApiPackage;
import com.github.tinselspoon.intellij.kubernetes.config.ConfigState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.ReflectionUtil;

/**
 * Responsible for loading a set of Swagger definitions corresponding to the active configuration.
 */
class ModelLoader {

    /** The version of Kubernetes specs to include if no version is specified. */
    private static final String DEFAULT_KUBERNETES_VERSION = "1.9";

    /** The version of OpenShift specs to include if no version is specified. */
    private static final String DEFAULT_OPENSHIFT_VERSION = "3.6";

    /** Cached specs that have already been parsed from a zip file. */
    private final Map<String, List<SwaggerSpec>> cachedSpecs = new HashMap<>();

    /** The current configuration. */
    private final ConfigState configState = ServiceManager.getService(ConfigState.class);

    /**
     * Gets a list of all active specifications, as directed by the active configuration.
     *
     * @return the active specs.
     */
    @NotNull
    List<SwaggerSpec> getActiveSpecs() {
        final Class<?> callerClass = ReflectionUtil.getGrandCallerClass();
        assert callerClass != null;
        final ClassLoader classLoader = callerClass.getClassLoader();
        final List<SwaggerSpec> activeSpecs = new ArrayList<>();
        activeSpecs.addAll(getSpecsFromApiPackage(classLoader, configState.getKubernetesPackage(), "kubernetes", DEFAULT_KUBERNETES_VERSION));
        activeSpecs.addAll(getSpecsFromApiPackage(classLoader, configState.getOpenshiftPackage(), "openshift", DEFAULT_OPENSHIFT_VERSION));
        return activeSpecs;
    }

    /**
     * Gets specs that are part of the specified {@link ApiPackage}.
     *
     * @param classLoader the class loader to use when loading resources.
     * @param apiPackage the package to load.
     * @param prefix the prefix to use for the zip file.
     * @param defaultVersion the version to use if the {@code apiPackage} does not specify one.
     * @return a list of specs loaded using the package, may be empty if the package is not enabled or version did not exist.
     */
    @NotNull
    private List<SwaggerSpec> getSpecsFromApiPackage(final ClassLoader classLoader, final ApiPackage apiPackage, final String prefix, final String defaultVersion) {
        if (apiPackage.getEnabled()) {
            final String version = Optional.ofNullable(apiPackage.getVersion()).orElse(defaultVersion);
            final String pattern = "com/github/tinselspoon/intellij/kubernetes/%s-%s.zip";
            return loadSpecFromZipWithCache(String.format(pattern, prefix, version), classLoader);
        }
        return Collections.emptyList();
    }

    /**
     * Load specs from a zip file, using a cached value if possible.
     *
     * @param resourceName the path to the zip file resource.
     * @param classLoader the class loader to use when loading resources.
     * @return a list of specs, may be empty if the version did not exist.
     */
    @NotNull
    private List<SwaggerSpec> loadSpecFromZipWithCache(final String resourceName, final ClassLoader classLoader) {
        return cachedSpecs.computeIfAbsent(resourceName, k -> loadSpecsFromZip(k, classLoader));
    }

    /**
     * Load specs from a zip file.
     *
     * @param resourceName the path to the zip file resource.
     * @param classLoader the class loader to use when loading resources.
     * @return a list of specs, may be empty if the version did not exist.
     */
    @NotNull
    private List<SwaggerSpec> loadSpecsFromZip(final String resourceName, final ClassLoader classLoader) {
        final InputStream resource = classLoader.getResourceAsStream(resourceName);
        if (resource == null) {
            return Collections.emptyList();
        }
        try (ZipInputStream resourceStream = new ZipInputStream(resource)) {
            final List<SwaggerSpec> specs = new ArrayList<>();
            ZipEntry entry;
            while ((entry = resourceStream.getNextEntry()) != null) {
                if (entry.getName().endsWith("json")) {
                    final SwaggerSpec parsedSpec = SwaggerSpec.loadFrom(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));
                    if (parsedSpec != null) {
                        specs.add(parsedSpec);
                    }
                }
            }
            return specs;
        } catch (final IOException e) {
            throw new RuntimeException("Error reading Swagger resource.", e);
        }
    }
}
