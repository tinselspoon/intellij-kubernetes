package com.github.tinselspoon.intellij.kubernetes.codeInsight;

import java.util.Set;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * Quick fix to create missing required properties on a given complex {@link YAMLKeyValue}.
 */
public class CreateMissingPropertiesIntentionAction extends PsiElementBaseIntentionAction {

    /** The mapping under which to create the keys. */
    private final YAMLMapping mapping;

    /** The keys that need to be created. */
    private final Set<String> missingKeys;

    /**
     * Default constructor.
     *
     * @param missingKeys the keys that need to be created.
     * @param mapping the mapping under which to create the keys.
     */
    public CreateMissingPropertiesIntentionAction(final Set<String> missingKeys, final YAMLMapping mapping) {
        this.missingKeys = missingKeys;
        this.mapping = mapping;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Kubernetes YAML Fixes";
    }

    @NotNull
    @Override
    public String getText() {
        return "Create missing required properties";
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) throws IncorrectOperationException {
        final YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
        for (final String missingKey : missingKeys) {
            final YAMLKeyValue newKeyValue = elementGenerator.createYamlKeyValue(missingKey, "");
            mapping.add(elementGenerator.createEol());
            mapping.add(elementGenerator.createIndent(YAMLUtil.getIndentToThisElement(mapping)));
            mapping.add(newKeyValue);
        }
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) {
        return true;
    }
}
