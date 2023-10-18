package club.bigtian.mf.plugin.action.flex;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
public class MybatisFlexInternal extends AnAction implements IntentionAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        // TODO: insert action logic here
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "MybatisFlexInternal";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "MybatisFlexInternal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    public Icon getIcon(int flags) {
        return AllIcons.General.Add; // Replace with your custom icon
    }
}
