package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.CompilerManagerUtil;
import club.bigtian.mf.plugin.core.util.Modules;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.util.ArrayList;
import java.util.List;

public class AutoCompileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        ProjectUtils.setCurrentProject(project);
        Module[] modules = Modules.getModule(project);
        PsiManager psiManager = PsiManager.getInstance(project);
        List<VirtualFile> virtualFiles = new ArrayList<>();
        for (Module module : modules) {
            FileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();
            fileIndex.iterateContent(fileOrDir -> {
                if (fileOrDir.isDirectory() && (fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.SOURCES))) {
                    PsiDirectory psiDirectory = psiManager.findDirectory(fileOrDir);
                    String path = psiDirectory.getVirtualFile().getPath();
                    if (!path.contains("src/main")) {
                        return true;
                    }
                    PsiElement firstChild = psiDirectory.getFirstChild();
                    if (ObjectUtil.isNotNull(firstChild) && firstChild.getText().contains("@Table")) {
                        for (PsiFile file : psiDirectory.getFiles()) {
                            virtualFiles.add(file.getVirtualFile());
                        }
                    }
                }
                return true;
            });
        }
        CompilerManagerUtil.compile(virtualFiles.toArray(new VirtualFile[virtualFiles.size()]),null);
    }
}
