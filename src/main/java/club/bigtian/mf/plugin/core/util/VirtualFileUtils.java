package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.io.File;

public class VirtualFileUtils {

    /**
     * 反式到java文件
     *
     * @param path 路径
     * @return {@code VirtualFile}
     */
    public static VirtualFile transToJavaFile(String path) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        return file;
    }

    public static PsiDirectory psiDirectory(Project project, String path) {
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        return psiManager.findDirectory(file);
    }


    /**
     * 得到psi目录
     *
     * @param project 项目
     * @param path    路径
     * @return {@code PsiDirectory}
     */
    public static PsiDirectory getPsiDirectory(Project project, String path) {
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile virtualFile = transToJavaFile(path);
        PsiDirectory psiDirectory = psiManager.findDirectory(virtualFile);
        return psiDirectory;
    }

    /**
     * 得到psi目录
     *
     * @param module 模块
     * @param key
     * @return {@code PsiDirectory}
     */
    public static PsiDirectory getPsiDirectory(Module module, String packageName, String key) {
        String modulePath = Modules.getModulePath(module);
        String name = Modules.getModuleName(module);
        String separator = File.separator;
        if (!modulePath.contains(name)) {
            modulePath = modulePath + separator + name + separator;
        }
        String path = StrUtil.format("src{}main{}java{}", separator, separator, separator);
        if (StrUtil.isEmpty(key)) {
            path = StrUtil.format("src{}main{}resources{}", separator, separator, separator);
        }
        createSubDirectory(module.getProject(), modulePath + path, packageName);
        modulePath = modulePath + path + packageName.replace(".", separator);
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        VirtualFile virtualFile = transToJavaFile(modulePath);
        PsiDirectory psiDirectory = null;
        try {
            psiDirectory = psiManager.findDirectory(virtualFile);
        } catch (Exception e) {
            Messages.showErrorDialog(StrUtil.format("找不到路径为【{}】的文件夹", modulePath), "错误");
            throw new RuntimeException(e);
        }
        return psiDirectory;
    }

    public static void createSubDirectory(Project project, String targetPath, String packageName) {
        PsiDirectory targetDirectory = getPsiDirectory(project, targetPath);
        if (targetDirectory != null) {
            String[] directories = packageName.split("\\.");
            for (String directoryName : directories) {
                PsiDirectory subdirectory = targetDirectory.findSubdirectory(directoryName);
                if (subdirectory == null) {
                    subdirectory = targetDirectory.createSubdirectory(directoryName);
                }
                targetDirectory = subdirectory;
            }
        }
    }
}
