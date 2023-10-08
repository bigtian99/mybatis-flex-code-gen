package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class AutoCompileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        ProjectUtils.setCurrentProject(project);
        PsiJavaFileUtil.createAptFile();
    }
}
