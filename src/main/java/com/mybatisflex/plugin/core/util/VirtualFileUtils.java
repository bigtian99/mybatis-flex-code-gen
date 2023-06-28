package com.mybatisflex.plugin.core.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

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
        String path= "/src/main/java/";
        if(StrUtil.isEmpty(key)){
            path="/src/main/resources/";
        }
        modulePath = modulePath +path+ packageName.replace(".", "/");
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        VirtualFile virtualFile = transToJavaFile(modulePath);
        PsiDirectory psiDirectory = psiManager.findDirectory(virtualFile);
        return psiDirectory;
    }


}
