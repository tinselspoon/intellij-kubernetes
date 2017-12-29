package com.github.tinselspoon.intellij.kubernetes;

import java.util.Collections;
import java.util.Map.Entry;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import com.github.tinselspoon.intellij.kubernetes.model.FieldType;
import com.github.tinselspoon.intellij.kubernetes.model.Model;
import com.github.tinselspoon.intellij.kubernetes.model.ModelProvider;
import com.github.tinselspoon.intellij.kubernetes.model.Property;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons.Json;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;

/**
 * Completion contributor for Kubernetes YAML files.
 */
public class KubernetesYamlCompletionContributor extends CompletionContributor {

    /** Default constructor. */
    public KubernetesYamlCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE), new Provider());
    }

    /**
     * Adds suggestions for possible items to insert under the value of a given {@link YAMLKeyValue}.
     *
     * @param modelProvider the store for model info.
     * @param resultSet the result set to append suggestions to.
     * @param resourceKey the identifier of the resource in question.
     * @param keyValue the {@link YAMLKeyValue} to obtain suggestions for.
     */
    private static void addValueSuggestionsForKey(@NotNull final ModelProvider modelProvider, final @NotNull CompletionResultSet resultSet, @NotNull final ResourceTypeKey resourceKey,
            @NotNull final YAMLKeyValue keyValue) {
        final Property keyProperty = KubernetesYamlPsiUtil.propertyForKey(modelProvider, resourceKey, keyValue);
        final Model keyModel = KubernetesYamlPsiUtil.modelForKey(modelProvider, resourceKey, keyValue);
        if (keyProperty != null && keyProperty.getType() == FieldType.BOOLEAN) {
            resultSet.addElement(LookupElementBuilder.create("true").withBoldness(true));
            resultSet.addElement(LookupElementBuilder.create("false").withBoldness(true));
        }
        if (keyModel != null) {
            for (final Entry<String, Property> property : keyModel.getProperties().entrySet()) {
                resultSet.addElement(createKeyLookupElement(property.getKey(), property.getValue()));
            }
        }
    }

    /**
     * Create a {@link LookupElementBuilder} for completing the text of a key. Do not use when completing a value.
     *
     * @param completionObject the object to pass to {@link LookupElementBuilder#create(Object)}.
     * @param addLayerOfNesting whether a newline and indent should be added when accepting the completed value - this is used when inserting the value will introduce a level of nesting (i.e. for an
     * object or array type).
     * @return the created {@code LookupElementBuilder}.
     */
    private static LookupElementBuilder createKeyLookupElement(@NotNull final Object completionObject, final boolean addLayerOfNesting) {
        return LookupElementBuilder.create(completionObject).withInsertHandler((insertionContext, lookupElement) -> {
            // If the caret is at the end of the line, add in the property colon when completing
            if (insertionContext.getCompletionChar() != ':' && insertionContext.getCompletionChar() != ' ') {
                final Editor editor = insertionContext.getEditor();
                final int offset = editor.getCaretModel().getOffset();
                final int lineNumber = editor.getDocument().getLineNumber(offset);
                final int lineEndOffset = editor.getDocument().getLineEndOffset(lineNumber);
                if (lineEndOffset == offset) {
                    final String autocompleteString;
                    if (addLayerOfNesting) {
                        // Copy the indentation characters present on this line, and add one additional level of indentation
                        final int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);
                        final String lineContent = editor.getDocument().getText().substring(lineStartOffset, lineEndOffset);
                        final int offsetOfContent = lineContent.length() - StringUtil.trimLeading(lineContent).length();
                        final String indentToLine = lineContent.substring(0, offsetOfContent);
                        final CodeStyleSettings currentSettings = CodeStyleSettingsManager.getSettings(insertionContext.getProject());
                        final CommonCodeStyleSettings.IndentOptions indentOptions = currentSettings.getIndentOptions(insertionContext.getFile().getFileType());
                        final String additionalIndent = indentOptions.USE_TAB_CHARACTER ? "\t" : StringUtil.repeatSymbol(' ', indentOptions.INDENT_SIZE);
                        autocompleteString = ":\n" + indentToLine + additionalIndent;
                    } else {
                        autocompleteString = ": ";
                    }
                    EditorModificationUtil.insertStringAtCaret(editor, autocompleteString);
                }
            }
        });
    }

    /**
     * Create a {@link LookupElementBuilder} when completing the text of a key identified by the given name and definition.
     *
     * @param propertyName the name of the property.
     * @param propertySpec the schema definition of the property.
     * @return the created {@code LookupElementBuilder}.
     */
    @NotNull
    private static LookupElementBuilder createKeyLookupElement(@NotNull final String propertyName, @NotNull final Property propertySpec) {
        final String typeText = ModelUtil.typeStringFor(propertySpec);
        Icon icon = PlatformIcons.PROPERTY_ICON;
        boolean addLayerOfNesting = false;
        if (propertySpec.getType() == FieldType.ARRAY) {
            icon = Json.Array;
            addLayerOfNesting = true;
        } else if (propertySpec.getRef() != null || propertySpec.getType() == FieldType.OBJECT) {
            icon = Json.Object;
            addLayerOfNesting = true;
        }
        return createKeyLookupElement(new PropertyCompletionItem(propertyName, propertySpec), addLayerOfNesting).withTypeText(typeText, true).withIcon(icon);
    }

    /**
     * Gets whether a given {@link YAMLKeyValue} is at the root level of the document.
     *
     * @param keyValue the element to evaluate.
     * @return {@code true} if this is a top-level mapping; otherwise, {@code false}.
     */
    private static boolean isTopLevelMapping(final YAMLKeyValue keyValue) {
        return keyValue.getParentMapping() != null && keyValue.getParentMapping().getParent() instanceof YAMLDocument;
    }

    /** The main actor in generating completion suggestions. */
    private static class Provider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull final CompletionParameters completionParameters, final ProcessingContext processingContext, @NotNull final CompletionResultSet resultSet) {
            // Make sure we are actually in a document that resembles a Kubernetes resource before offering completion
            final PsiElement element = completionParameters.getPosition();
            if (!KubernetesYamlPsiUtil.isKubernetesFile(element) || element instanceof PsiComment) {
                return;
            }

            // Get the current key/value being worked on
            final ModelProvider modelProvider = ModelProvider.INSTANCE;
            final YAMLMapping topLevelMapping = KubernetesYamlPsiUtil.getTopLevelMapping(element);
            final YAMLKeyValue keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue.class);

            // Try and find the resource key which will aid our completion
            final ResourceTypeKey resourceKey = KubernetesYamlPsiUtil.findResourceKey(element);

            // We must be at the very top level if there is no enclosing keyValue
            if (keyValue == null) {
                if (resourceKey == null) {
                    // If we don't know what the resource type is, add the "apiVersion" and "kind" fields which will be applicable to all resources
                    resultSet.addElement(createKeyLookupElement("apiVersion", false));
                    resultSet.addElement(createKeyLookupElement("kind", false));
                } else {
                    // If we do know the resource type, add the fields relevant to that resource
                    modelProvider.findProperties(resourceKey, Collections.emptyList()).forEach((key, value) -> resultSet.addElement(createKeyLookupElement(key, value)));
                }
            } else {
                // The "apiVersion" and "kind" fields on the top level are special cases where we have to calculate the completion
                if (isTopLevelMapping(keyValue)) {
                    if ("apiVersion".equals(keyValue.getKeyText())) {
                        for (final String apiVersion : modelProvider.suggestApiVersions()) {
                            resultSet.addElement(LookupElementBuilder.create(apiVersion).withIcon(PlatformIcons.PACKAGE_ICON));
                        }
                    } else if ("kind".equals(keyValue.getKeyText())) {
                        final String apiVersion = KubernetesYamlPsiUtil.getValueText(topLevelMapping, "apiVersion");
                        for (final ResourceTypeKey kind : modelProvider.suggestKinds(apiVersion)) {
                            final String kindApiVersion = kind.getApiVersion();
                            // Add on the apiVersion
                            resultSet.addElement(LookupElementBuilder.create(kind.getKind())
                                                                     .withTypeText(kindApiVersion, true)
                                                                     .withIcon(PlatformIcons.CLASS_ICON)
                                                                     .withInsertHandler((insertionContext, lookupElement) -> {
                                                                         if (topLevelMapping == null || topLevelMapping.getKeyValueByKey("apiVersion") == null) {
                                                                             EditorModificationUtil.insertStringAtCaret(insertionContext.getEditor(), "\napiVersion: " + kindApiVersion + "\n");
                                                                         }
                                                                     }));
                        }
                    }
                }

                if (resourceKey != null) {
                    addValueSuggestionsForKey(modelProvider, resultSet, resourceKey, keyValue);
                }
            }
        }
    }
}
