package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.Modules;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

public class AutoCompileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Module[] modules = Modules.getModule(project);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (Module module : modules) {
            FileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();

            fileIndex.iterateContent(fileOrDir -> {
                if (fileOrDir.isDirectory() && (fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.SOURCES))) {
                    PsiDirectory psiDirectory = psiManager.findDirectory(fileOrDir);
                    String path = psiDirectory.getVirtualFile().getPath();
                    if(!path.contains("src/main")){
                        return true;
                    }
                    System.out.println(path);
                }
                return true;
            });
        }

    }
}
