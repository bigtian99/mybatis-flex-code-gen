package com.mybatisflex.plugin.core;

import cn.hutool.core.util.StrUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Package {
    /**
     * 选择包路径
     *
     * @param module 模块
     * @return {@code String}
     */
    public static String selectPackage(Module module, String... packagePath) {
        PackageChooserDialog chooser = new PackageChooserDialog("Select Package", module);
        if (packagePath.length > 0) {
            chooser.selectPackage(packagePath[0]);
        }
        // 显示对话框并等待用户选择
        chooser.show();
        PsiPackage selectedPackage = chooser.getSelectedPackage();
        return selectedPackage.getQualifiedName();
    }

    /**
     * 选择包+resources下面的路径
     *
     * @param module 模块
     * @return {@code String}
     */
    public static String selectPackageResources(Module module, String... packagePath) {
        Project project = module.getProject();
        String separator = File.separator;
        String path = project.getBaseDir().getCanonicalPath() + separator + StrUtil.format("src{}main{}resources{}", separator, separator, separator);
        ArrayList<String> resourcesList = new ArrayList<>();
        getSubDirectory(path, resourcesList);
        PsiManager psiManager = PsiManager.getInstance(project);
        PackageChooserDialogBigtian chooser = new PackageChooserDialogBigtian("Select Package", module);
        for (String path1 : resourcesList) {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path1);
            PsiDirectory psiDirectory = psiManager.findDirectory(file);
            PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
            chooser.addPackage(aPackage);
        }
        if (packagePath.length > 0) {
            chooser.selectPackage(packagePath[0]);
        }
        // 显示对话框并等待用户选择
        chooser.show();
        PsiPackage selectedPackage = chooser.getSelectedPackage();
        return selectedPackage.getQualifiedName();
    }

    private static void getSubDirectory(String path, ArrayList<String> resourcesList) {
        File file = new File(path);
        file.listFiles(pathname -> {
            if (pathname.isDirectory()) {
                resourcesList.add(pathname.getAbsolutePath());
                getSubDirectory(pathname.getAbsolutePath(), resourcesList);
                return true;
            }
            return false;
        });
    }
}
