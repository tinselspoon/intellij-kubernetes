package com.github.tinselspoon.intellij.kubernetes.codeInsight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLValue;

import com.github.tinselspoon.intellij.kubernetes.KubernetesYamlPsiUtil;
import com.github.tinselspoon.intellij.kubernetes.ResourceTypeKey;
import com.github.tinselspoon.intellij.kubernetes.model.ModelProvider;
import com.github.tinselspoon.intellij.kubernetes.model.Property;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

/**
 * Ensures the values of {@link YAMLKeyValue}s conform to the data type of the corresponding schema element.
 */
public class DataTypeCheckerAnnotator implements Annotator {

    // TODO: implement checking for the scalar types
    //    private static final Pattern NULL_REGEX = Pattern.compile("null|Null|NULL|~");
    //    private static final Pattern BOOL_REGEX = Pattern.compile("true|True|TRUE|false|False|FALSE");
    //    private static final Pattern INT_REGEX = Pattern.compile("[-+]?[0-9]+|0o[0-7]+|0x[0-9a-fA-F]+");
    //    private static final Pattern FLOAT_REGEX = Pattern.compile("[-+]?(\\.[0-9]+|[0-9]+(\\.[0-9]*)?)([eE][-+]?[0-9]+)?|[-+]?(\\.inf|\\.Inf|\\.INF)|\\.nan|\\.NaN|\\.NAN");

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder annotationHolder) {
        if (!KubernetesYamlPsiUtil.isKubernetesFile(element)) {
            return;
        }
        final ModelProvider modelProvider = ModelProvider.INSTANCE;
        final ResourceTypeKey resourceKey = KubernetesYamlPsiUtil.findResourceKey(element);
        if (resourceKey != null && element instanceof YAMLKeyValue) {
            final YAMLKeyValue keyValue = (YAMLKeyValue) element;
            final Property property = KubernetesYamlPsiUtil.propertyForKey(modelProvider, resourceKey, keyValue);
            final YAMLValue value = keyValue.getValue();
            if (property != null && property.getType() != null && value != null) {
                switch (property.getType()) {
                    case ARRAY:
                        if (!(value instanceof YAMLSequence)) {
                            annotationHolder.createErrorAnnotation(value, "The content of " + keyValue.getKeyText() + " should be an array.");
                        }
                        break;
                    case OBJECT:
                        if (!(value instanceof YAMLMapping)) {
                            annotationHolder.createErrorAnnotation(value, "The content of " + keyValue.getKeyText() + " should be an object.");
                        }
                        break;
                }
            }
        }
    }

}
