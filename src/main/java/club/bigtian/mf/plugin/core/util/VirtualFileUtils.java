package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VirtualFileUtils {
    private static Map<String, PsiDirectory> PSI_DIRECTORY_MAP = new HashMap<>();
    private static FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

    public static PsiFile getPsiFile(Project project, VirtualFile virtualFile) {
        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findFile(virtualFile);
    }

    public static VirtualFile getVirtualFile(Document document) {
        return fileDocumentManager.getFile(document);

    }

    /**
     * 反式到java文件
     *
     * @param path 路径
     * @return {@code VirtualFile}
     */
    public static VirtualFile transToJavaFile(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

    /**
     * 根据路径获取 psi目录
     *
     * @param project 项目
     * @param path    路径
     * @return {@code PsiDirectory}
     */
    public static PsiDirectory psiDirectory(Project project, String path) {
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        assert file != null;
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
        return psiManager.findDirectory(virtualFile);
    }

    /**
     * 得到psi目录
     *
     * @param project     项目
     * @param virtualFile 虚拟文件
     * @return {@code PsiDirectory}
     */
    public static PsiDirectory getPsiDirectory(Project project, VirtualFile virtualFile) {
        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findDirectory(virtualFile);
    }


    /**
     * 得到psi目录
     *
     * @param module 模块
     * @param key
     * @return {@code PsiDirectory}
     */
    public static PsiDirectory getPsiDirectory(Module module, String packageName, String key) {
        Set javaResourceRootTypes = StrUtil.isEmpty(key) ? JavaModuleSourceRootTypes.RESOURCES : JavaModuleSourceRootTypes.SOURCES;
        return PSI_DIRECTORY_MAP.getOrDefault(packageName, createSubDirectory(module, javaResourceRootTypes, packageName));
    }

    public static PsiDirectory createSubDirectory(Module module, Set javaResourceRootTypes, String packageName) {
        PsiDirectory targetDirectory = Modules.getModuleDirectory(module, javaResourceRootTypes);
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
        return targetDirectory;
    }

    /**
     * 清空psi目录
     */
    public static void clearPsiDirectoryMap() {
        PSI_DIRECTORY_MAP.clear();
    }
}
