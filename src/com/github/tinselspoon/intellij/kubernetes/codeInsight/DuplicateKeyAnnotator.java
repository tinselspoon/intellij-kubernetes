package com.github.tinselspoon.intellij.kubernetes.codeInsight;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import com.github.tinselspoon.intellij.kubernetes.KubernetesYamlPsiUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

/**
 * Finds {@link YAMLMapping}s with duplicated keys.
 */
public class DuplicateKeyAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder annotationHolder) {
        if (!KubernetesYamlPsiUtil.isKubernetesFile(element)) {
            return;
        }
        if (element instanceof YAMLMapping) {
            final YAMLMapping mapping = (YAMLMapping) element;
            final Collection<YAMLKeyValue> keyValues = mapping.getKeyValues();
            final Set<String> existingKeys = new HashSet<>(keyValues.size());
            for (final YAMLKeyValue keyValue : keyValues) {
                if (keyValue.getKey() != null && !existingKeys.add(keyValue.getKeyText().trim())) {
                    annotationHolder.createErrorAnnotation(keyValue.getKey(), "Duplicated property '" + keyValue.getKeyText() + "'").registerFix(new DeletePropertyIntentionAction());
                }
            }
        }
    }
}
