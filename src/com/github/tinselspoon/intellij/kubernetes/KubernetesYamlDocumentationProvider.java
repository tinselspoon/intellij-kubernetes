package com.github.tinselspoon.intellij.kubernetes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import com.github.tinselspoon.intellij.kubernetes.model.ModelProvider;
import com.github.tinselspoon.intellij.kubernetes.model.Property;
import com.intellij.lang.Language;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * Provides documentation for Kubernetes resources by matching properties to the corresponding schema definitions.
 */
public class KubernetesYamlDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
        if (!(element instanceof DocElement) && !KubernetesYamlPsiUtil.isKubernetesFile(element)) {
            return null;
        }

        // See if this is a DocElement passed from the getDocumentationElementForLookupItem method - if not, find it out the long way
        final PropertyCompletionItem targetProperty;
        if (element instanceof DocElement) {
            targetProperty = ((DocElement) element).getTargetProperty();
        } else {
            targetProperty = findPropertyFromElement(element);
        }
        if (targetProperty != null) {
            final String type = ModelUtil.typeStringFor(targetProperty.getProperty());
            final String description = targetProperty.getProperty().getDescription();
            return String.format("<b>%s</b> <code>(%s)</code><p>%s", targetProperty.getName(), type, description);
        }
        return null;

    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(final PsiManager psiManager, final Object object, final PsiElement element) {
        if (element != null && object instanceof PropertyCompletionItem) {
            return new DocElement(psiManager, element.getLanguage(), (PropertyCompletionItem) object);
        }
        return null;
    }

    /**
     * Attempts to find the matching schema information on the property described by the given element.
     *
     * @param element the element to search from.
     * @return the matching {@link PropertyCompletionItem}, or {@code null} if one cannot be determined.
     */
    @Nullable
    private PropertyCompletionItem findPropertyFromElement(@NotNull final PsiElement element) {
        final ModelProvider modelProvider = ModelProvider.INSTANCE;
        final ResourceTypeKey resourceKey = KubernetesYamlPsiUtil.findResourceKey(element);
        if (resourceKey != null && element instanceof YAMLKeyValue) {
            final YAMLKeyValue keyValue = (YAMLKeyValue) element;
            final Property property = KubernetesYamlPsiUtil.propertyForKey(modelProvider, resourceKey, keyValue);
            if (property != null) {
                return new PropertyCompletionItem(keyValue.getKeyText(), property);
            }
        }
        return null;
    }

    /**
     * Lightweight PSI element that exists to solely allow us to pass data between from the {@link #getDocumentationElementForLookupItem(PsiManager, Object, PsiElement)} method to the {@link
     * #generateDoc(PsiElement, PsiElement)} method.
     */
    private static class DocElement extends LightElement {

        /** The property being described by this element. */
        private final PropertyCompletionItem targetProperty;

        /**
         * Default constructor.
         *
         * @param targetProperty the property being described by this element.
         */
        protected DocElement(@NotNull final PsiManager manager, @NotNull final Language language, @NotNull final PropertyCompletionItem targetProperty) {
            super(manager, language);
            this.targetProperty = targetProperty;
        }

        /**
         * Gets the property being described by this element.
         *
         * @return the target property.
         */
        public PropertyCompletionItem getTargetProperty() {
            return targetProperty;
        }

        @Override
        public String toString() {
            return "DocElement for " + targetProperty;
        }
    }
}
