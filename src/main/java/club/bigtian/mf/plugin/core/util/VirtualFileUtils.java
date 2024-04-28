package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.CustomConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VirtualFileUtils {
    private static Map<String, PsiDirectory> PSI_DIRECTORY_MAP = new HashMap<>();
    private static FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

    public static PsiFile getPsiFile(Project project, VirtualFile virtualFile) {
        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findFile(virtualFile);
    }

    public static PsiFile getPsiFile(Document document) {

        return PsiDocumentManager.getInstance(ProjectUtils.getCurrentProject()).getPsiFile(document);
    }

    public static VirtualFile getVirtualFile(Document document) {
        return fileDocumentManager.getFile(document);

    }


    /**
     * 只获取target/build目录下的文件
     *
     * @param baseDir
     * @param config
     * @return
     */
    @Nullable
    public static VirtualFile getVirtualFile(VirtualFile baseDir, CustomConfig config) {
        String genPath = config.getGenPath();
        if (StrUtil.isNotBlank(genPath)) {
            PsiDirectory psiDirectory = VirtualFileUtils.getPsiDirectory(ProjectUtils.getCurrentProject(), genPath);
            if (ObjectUtil.isNotNull(psiDirectory)) {
                return psiDirectory.getVirtualFile();
            }
        }
        VirtualFile file = baseDir.findChild("target");
        if (ObjectUtil.isNull(file)) {
            file = baseDir.findChild("build");
            if (baseDir.getPath().contains("kapt")) {
                return baseDir;
            }
        }
        if (ObjectUtil.isNull(file)) {
            return null;
        }
        VirtualFile[] children = file.getChildren();
        if (ArrayUtil.isEmpty(children)) {
            return null;
        }
        for (VirtualFile child : children) {
            if (child.getName().startsWith("generated")) {
                return child;
            }
        }
        return file;
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
        if (ObjectUtil.isNull(virtualFile)) {

            return null;
        }
        return psiManager.findDirectory(virtualFile);
    }

    /**
     * 获取psi目录如果没有则创建
     *
     * @param path
     * @return
     */

    public static PsiDirectory getPsiDirectoryAndCreate(String path, String mkdirName) {
        PsiDirectory psiDirectory = getPsiDirectory(ProjectUtils.getCurrentProject(), path);
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path + File.separator + mkdirName);
        if (ObjectUtil.isNull(virtualFile)) {
            PsiDirectory targetDirectory = WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), (Computable<PsiDirectory>) () -> {
                return psiDirectory.createSubdirectory(mkdirName);
            });
            return targetDirectory;
        }
        return getPsiDirectory(ProjectUtils.getCurrentProject(), path + File.separator + mkdirName);
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
        Set javaResourceRootTypes;
        if (MybatisFlexConstant.XML.equals(key) && Template.getConfigData(MybatisFlexConstant.MAPPER_XML_TYPE, "resource").equals("resource")) {
            javaResourceRootTypes = JavaModuleSourceRootTypes.RESOURCES;
        } else {
            javaResourceRootTypes = JavaModuleSourceRootTypes.SOURCES;
        }
        PsiDirectory psiDirectory = PSI_DIRECTORY_MAP.get(packageName + key);
        if (ObjectUtil.isNull(psiDirectory)) {
            AtomicReference<PsiDirectory> subPsiDirectory = new AtomicReference<>();
            WriteCommandAction.runWriteCommandAction(module.getProject(), () -> {
                subPsiDirectory.set(createSubDirectory(module, javaResourceRootTypes, packageName));
            });
            psiDirectory = subPsiDirectory.get();
            PSI_DIRECTORY_MAP.put(packageName + key, psiDirectory);
        }
        return psiDirectory;
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

    public static PsiDirectory createSubDirectory(Module module, String packageName) {
        PsiDirectory targetDirectory = getPsiDirectory(module.getProject(), packageName);
        if (ObjectUtil.isNotNull(targetDirectory)) {
            return targetDirectory;
        }
        String path = Modules.getPath(module);
        targetDirectory = getPsiDirectory(module.getProject(), path);
        if (targetDirectory != null) {
            String[] directories = StrUtil.subAfter(packageName, path, false).split("/");
            for (String directoryName : directories) {
                AtomicReference<PsiDirectory> subdirectory = new AtomicReference<>(targetDirectory.findSubdirectory(directoryName));
                if (subdirectory.get() == null) {
                    PsiDirectory finalTargetDirectory = targetDirectory;
                    WriteCommandAction.runWriteCommandAction(module.getProject(), () -> {
                        subdirectory.set(finalTargetDirectory.createSubdirectory(directoryName));
                    });
                }
                targetDirectory = subdirectory.get();
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

    public static  void getAllXmlFiles(PsiDirectory psiDirectory, List<XmlFile> xmlFiles) {
        // 获取所有的xml文件，具体实现逻辑
        for (PsiElement psiFile : psiDirectory.getChildren()) {
            if (psiFile instanceof XmlFile) {
                xmlFiles.add((XmlFile) psiFile);
            } else if (psiFile instanceof PsiDirectory) {
                getAllXmlFiles((PsiDirectory) psiFile, xmlFiles);
            }
        }
    }

    public static Map<String, XmlFile> getAllResourceFiles() {
        Project project = ProjectUtils.getCurrentProject();
        List<XmlFile> resourceFiles = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            PsiDirectory psiDirectory = Modules.getModuleDirectory(module, JavaModuleSourceRootTypes.RESOURCES);
            if(ObjectUtil.isNull(psiDirectory)){
                continue;
            }
            //     获取所有的xml文件
            ArrayList<XmlFile> objects = new ArrayList<>();
            getAllXmlFiles(psiDirectory, objects);
            resourceFiles.addAll(objects);
        }
        Map<String, XmlFile> namespace = resourceFiles.stream()
                .collect(Collectors.toMap(el -> el.getRootTag().getAttributeValue("namespace"), el -> el, (k1, k2) -> k1));

        return namespace;
    }
}
