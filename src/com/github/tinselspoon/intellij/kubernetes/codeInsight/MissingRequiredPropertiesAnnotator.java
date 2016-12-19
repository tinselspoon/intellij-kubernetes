package com.github.tinselspoon.intellij.kubernetes.codeInsight;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import com.github.tinselspoon.intellij.kubernetes.KubernetesYamlPsiUtil;
import com.github.tinselspoon.intellij.kubernetes.ResourceTypeKey;
import com.github.tinselspoon.intellij.kubernetes.model.Model;
import com.github.tinselspoon.intellij.kubernetes.model.ModelProvider;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

/**
 * Inspects {@link YAMLKeyValue}s with a {@link YAMLMapping} value, and raises an error if the corresponding schema element declares properties that are required but are not in the mapping.
 */
public class MissingRequiredPropertiesAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder annotationHolder) {
        if (!KubernetesYamlPsiUtil.isKubernetesFile(element)) {
            return;
        }
        final ModelProvider modelProvider = ModelProvider.INSTANCE;
        final ResourceTypeKey resourceKey = KubernetesYamlPsiUtil.findResourceKey(element);
        if (resourceKey != null && element instanceof YAMLKeyValue) {
            final YAMLKeyValue keyValue = (YAMLKeyValue) element;
            final Model model = KubernetesYamlPsiUtil.modelForKey(modelProvider, resourceKey, keyValue);
            if (model != null && keyValue.getKey() != null) {
                if (keyValue.getValue() instanceof YAMLMapping) {
                    final YAMLMapping mapping = (YAMLMapping) keyValue.getValue();
                    addErrors(annotationHolder, model, keyValue.getKey(), mapping);
                } else if (keyValue.getValue() instanceof YAMLSequence) {
                    final YAMLSequence sequence = (YAMLSequence) keyValue.getValue();
                    for (final YAMLSequenceItem item : sequence.getItems()) {
                        if (item.getValue() instanceof YAMLMapping) {
                            addErrors(annotationHolder, model, item.getFirstChild(), (YAMLMapping) item.getValue());
                        }
                    }
                }
            }
        }
    }

    private void addErrors(final @NotNull AnnotationHolder annotationHolder, final Model model, final PsiElement errorTarget, final YAMLMapping mapping) {
        final Set<String> existingKeys = mapping.getKeyValues().stream().map(YAMLKeyValue::getKeyText).collect(Collectors.toSet());

        // Find out the keys that are needed, and remove any which have been defined
        // The resulting set are the properties required but not added
        final Set<String> requiredProperties = new HashSet<>(model.getRequiredProperties());
        requiredProperties.removeAll(existingKeys);

        if (!requiredProperties.isEmpty()) {
            annotationHolder.createWarningAnnotation(errorTarget, "Missing required properties on " + model.getId() + ": " + String.join(", ", requiredProperties))
                            .registerFix(new CreateMissingPropertiesIntentionAction(requiredProperties, mapping));
        }
    }
}
