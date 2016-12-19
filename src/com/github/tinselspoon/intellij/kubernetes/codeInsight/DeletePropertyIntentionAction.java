package com.github.tinselspoon.intellij.kubernetes.codeInsight;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * Quick fix which removes the {@link YAMLKeyValue} it is invoked on.
 */
public class DeletePropertyIntentionAction extends PsiElementBaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Kubernetes YAML Fixes";
    }

    @NotNull
    @Override
    public String getText() {
        return "Delete property";
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) throws IncorrectOperationException {
        final YAMLKeyValue keyValue = PsiTreeUtil.getParentOfType(psiElement, YAMLKeyValue.class);
        if (keyValue != null) {
            keyValue.delete();
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
            removeEmptyLine(editor);
        }
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) {
        return true;
    }

    /**
     * If an empty line is under the caret within the editor, remove it.
     * <p>
     * This code is adapted from: https://github.com/zalando/intellij-swagger/blob/master/src/main/java/org/zalando/intellij/swagger/intention/field/RemoveYamlFieldIntentionAction.java
     *
     * @param editor the editor in use.
     */
    private void removeEmptyLine(@NotNull final Editor editor) {
        final int offset = editor.getCaretModel().getOffset();
        final int lineNumber = editor.getDocument().getLineNumber(offset);
        final int lineStartOffset = editor.getDocument().getLineStartOffset(lineNumber);
        final int lineEndOffset = editor.getDocument().getLineEndOffset(lineNumber);
        final String lineContent = editor.getDocument().getText().substring(lineStartOffset, lineEndOffset);
        if ("".equals(lineContent.trim())) {
            final int endIndex = editor.getDocument().getText().length() > lineEndOffset ? lineEndOffset + 1 : lineEndOffset;
            editor.getDocument().deleteString(lineStartOffset, endIndex);
        }
    }
}
