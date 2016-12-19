package com.github.tinselspoon.intellij.kubernetes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import com.github.tinselspoon.intellij.kubernetes.model.Model;
import com.github.tinselspoon.intellij.kubernetes.model.ModelProvider;
import com.github.tinselspoon.intellij.kubernetes.model.Property;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Utilities for working with YAML-based Kubernetes resource documents.
 */
public final class KubernetesYamlPsiUtil {

    /** Static class private constructor. */
    private KubernetesYamlPsiUtil() {
        // no construction
    }

    /**
     * Find the {@link ResourceTypeKey} for the current document.
     *
     * @param element an element within the document to check.
     * @return the resource type key, or {@code null} if the "apiVersion" and "kind" fields are not present.
     */
    @Nullable
    public static ResourceTypeKey findResourceKey(final PsiElement element) {
        // Get the top-level mapping
        final YAMLMapping topLevelMapping = getTopLevelMapping(element);
        final String apiVersion = getValueText(topLevelMapping, "apiVersion");
        final String kind = getValueText(topLevelMapping, "kind");
        if (apiVersion != null && kind != null) {
            return new ResourceTypeKey(apiVersion, kind);
        } else {
            return null;
        }
    }

    /**
     * Gets the top-level mapping in the document, if present.
     *
     * @param element an element within the document.
     * @return the top-level mapping, or {@code null} if one is not defined (e.g. in an empty document).
     */
    @Nullable
    public static YAMLMapping getTopLevelMapping(final PsiElement element) {
        final YAMLDocument document = PsiTreeUtil.getParentOfType(element, YAMLDocument.class);
        if (document != null) {
            final YAMLValue topLevelValue = document.getTopLevelValue();
            if (topLevelValue instanceof YAMLMapping) {
                return (YAMLMapping) topLevelValue;
            }
        }
        return null;
    }

    /**
     * Gets the text of the value held by the given key within a mapping.
     *
     * @param mapping the mapping to search through.
     * @param key the key to search for.
     * @return the trimmed text value of the key, or {@code null} if the mapping is null or the key was not found.
     */
    @Nullable
    public static String getValueText(@Nullable final YAMLMapping mapping, @NotNull final String key) {
        return Optional.ofNullable(mapping).map(m -> m.getKeyValueByKey(key)).map(YAMLKeyValue::getValueText).map(String::trim).orElse(null);
    }

    /**
     * Determines whether the element is within a Kubernetes YAML file. This is done by checking for the presence of "apiVersion" or "kind" as top-level keys within the first document of the file.
     *
     * @param element an element within the file to check.
     * @return true if the element is within A Kubernetes YAML file, otherwise, false.
     */
    public static boolean isKubernetesFile(final PsiElement element) {
        final PsiFile file = element.getContainingFile();
        if (file instanceof YAMLFile) {
            final Collection<YAMLKeyValue> keys = YAMLUtil.getTopLevelKeys((YAMLFile) file);
            return keys.stream().map(YAMLKeyValue::getKeyText).anyMatch(s -> "apiVersion".equals(s) || "kind".equals(s));
        }
        return false;
    }

    /**
     * Find the corresponding {@link Model} object that represents the value of a given {@link YAMLKeyValue}.
     *
     * @param modelProvider the model provider to use for looking up schema resources.
     * @param resourceKey the top-level key of the resource.
     * @param keyValue the {@code YAMLKeyValue} to search back from.
     * @return the corresponding model or {@code null} if one cannot be located.
     */
    @Nullable
    public static Model modelForKey(final ModelProvider modelProvider, final ResourceTypeKey resourceKey, final YAMLKeyValue keyValue) {
        // Get the tree of keys leading up to this one
        final List<String> keys = new ArrayList<>();
        YAMLKeyValue currentKey = keyValue;
        do {
            keys.add(currentKey.getKeyText());
        } while ((currentKey = PsiTreeUtil.getParentOfType(currentKey, YAMLKeyValue.class)) != null);

        // We have iterated from the inside out, so flip this around to get it in the correct direction for the ModelProvider
        Collections.reverse(keys);
        return modelProvider.findModel(resourceKey, keys);
    }

    /**
     * Find the corresponding {@link Property} object that relates to the given {@link YAMLKeyValue}.
     *
     * @param modelProvider the model provider to use for looking up schema resources.
     * @param resourceKey the top-level key of the resource.
     * @param keyValue the {@code YAMLKeyValue} to search back from.
     * @return the corresponding property or {@code null} if one cannot be located.
     */
    @Nullable
    public static Property propertyForKey(@NotNull final ModelProvider modelProvider, @NotNull final ResourceTypeKey resourceKey, @NotNull final YAMLKeyValue keyValue) {
        // Get the tree of keys leading up to this one
        final List<String> keys = new ArrayList<>();
        YAMLKeyValue currentKey = keyValue;
        while ((currentKey = PsiTreeUtil.getParentOfType(currentKey, YAMLKeyValue.class)) != null) {
            keys.add(currentKey.getKeyText());
        }

        // We have iterated from the inside out, so flip this around to get it in the correct direction for the ModelProvider
        Collections.reverse(keys);
        return modelProvider.findProperties(resourceKey, keys).get(keyValue.getKeyText());
    }
}
