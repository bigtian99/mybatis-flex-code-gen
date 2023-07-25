package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.filter.FilterComboBoxModel;
import club.bigtian.mf.plugin.core.render.ModuleComBoxRender;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 模块
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class Modules {
    private static final Logger LOG = Logger.getInstance(Modules.class);
    private static Map<String, Module> moduleMap;
    private static Map<String, Map<String, String>> modulePackageMap;
    private static Boolean isManvenProject;

    /**
     * 得到包路径
     *
     * @param moduleName  模块名称
     * @param packageName 系统配置包名
     * @return {@code String}
     */
    public static String getPackagePath(String moduleName, String packageName) {
        Map<String, String> moduleMap = modulePackageMap.get(moduleName);
        if (CollUtil.isEmpty(moduleMap)) {
            NotificationUtils.notifyError("模块不存在!", "", ProjectUtils.getCurrentProject());
            throw new RuntimeException("模块不存在");
        }
        return moduleMap.getOrDefault(packageName, "");
    }

    /**
     * 获取模块
     *
     * @param project 项目
     * @return {@code List<String>}
     */
    public static void addModulesItem(Project project, List<JComboBox> modulesComboxs) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        if (ArrayUtil.isEmpty(modules)) {
            NotificationUtils.notifyError("目录层级有误!", "", project);
            return;
        }

        boolean isManvenProject = isManvenProject(modules[0]);
        for (JComboBox modulesCombox : modulesComboxs) {
            modulesCombox.setRenderer(new ModuleComBoxRender());

            moduleMap = Arrays.stream(modules)
                    .filter(el -> {
                        if (isManvenProject) {
                            return true;
                        }
                        // 非maven项目只显示main模块,只有main模块才有java目录
                        return el.getName().contains(".main");
                    })
                    .collect(Collectors.toMap(el -> el.getName().split("\\.")[0], module -> module));
            FilterComboBoxModel model = new FilterComboBoxModel(moduleMap.keySet().stream().collect(Collectors.toList()));
            modulesCombox.setModel(model);
            modulesCombox.setSelectedIndex(0);
        }
        getModulePackages();
    }

    public static void getModulePackages() {
        modulePackageMap = new HashMap<>();
        Project project = ProjectUtils.getCurrentProject();
        for (Module module : moduleMap.values()) {
            Map<String, String> moduleMap = new HashMap<>();
            PsiManager psiManager = PsiManager.getInstance(project);
            FileIndex fileIndex = module != null ? ModuleRootManager.getInstance(module).getFileIndex() : ProjectRootManager.getInstance(project).getFileIndex();
            fileIndex.iterateContent(fileOrDir -> {
                if (fileOrDir.isDirectory() && (fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.SOURCES) || fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.RESOURCES))) {
                    PsiDirectory psiDirectory = psiManager.findDirectory(fileOrDir);
                    LOG.assertTrue(psiDirectory != null);
                    PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
                    if (aPackage != null) {
                        moduleMap.put(aPackage.getName(), aPackage.getQualifiedName());
                    }
                }
                return true;
            });
            modulePackageMap.put(module.getName(), moduleMap);
        }
    }

    /**
     * 判断是否manven项目
     *
     * @param project 项目
     * @return boolean
     */
    public static boolean isManvenProject(Module module) {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        if (ArrayUtil.isEmpty(contentRoots)) {
            return false;
        }
        VirtualFile contentRoot = contentRoots[0];
        VirtualFile virtualFile = contentRoot.findChild("pom.xml");
        isManvenProject = ObjectUtil.isNotNull(virtualFile);
        return isManvenProject;
    }


    /**
     * 获取模块
     *
     * @param moduleName 模块名称
     * @return {@code Module}
     */
    public static Module getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }

    /**
     * 获取模块路径
     *
     * @param moduleName 模块名称
     * @return {@code String}
     */
    public static String getModulePath(String moduleName) {
        Module module = moduleMap.get(moduleName);
        return getModulePath(module, JavaModuleSourceRootTypes.SOURCES);
    }

    public static String getModulePath(Module module, Set javaResourceRootTypes) {
        AtomicReference<String> path = new AtomicReference<>();
        ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();
        fileIndex.iterateContent(fileOrDir -> {
            if (fileOrDir.isDirectory() && fileIndex.isUnderSourceRootOfType(fileOrDir, javaResourceRootTypes)) {
                String canonicalPath = fileOrDir.getCanonicalPath();
                path.set(canonicalPath);
                return false;
            }
            return true;
        });
        return path.get();
    }


    public static PsiDirectory getModuleDirectory(Module module, Set javaResourceRootTypes) {
        AtomicReference<PsiDirectory> directory = new AtomicReference<>();
        ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();
        fileIndex.iterateContent(fileOrDir -> {
            if (fileOrDir.isDirectory() && fileIndex.isUnderSourceRootOfType(fileOrDir, javaResourceRootTypes)) {
                directory.set(VirtualFileUtils.getPsiDirectory(module.getProject(), fileOrDir));
                return false;
            }
            return true;
        });
        return directory.get();
    }


    /**
     * 初始化模块
     *
     * @param project 项目
     * @param list    列表
     */
    public static void initModules(Project project, List<JComboBox> list) {
        addModulesItem(project, list);
    }

    /**
     * service联动
     *
     * @param serviceInteCombox 服务强度combox
     * @param serviceImplComBox 服务impl com盒子
     */
    public static void comBoxGanged(JComboBox serviceInteCombox, JComboBox serviceImplComBox) {
        serviceInteCombox.addActionListener(e -> {
            serviceImplComBox.setSelectedIndex(serviceInteCombox.getSelectedIndex());
            serviceImplComBox.revalidate();
            serviceImplComBox.repaint();
        });

        serviceImplComBox.addActionListener(e -> {
            serviceInteCombox.setSelectedIndex(serviceImplComBox.getSelectedIndex());
            serviceInteCombox.revalidate();
            serviceInteCombox.repaint();
        });
    }

    /**
     * 同步controller模块
     *
     * @param modulesComboxs
     * @param idx
     */
    public static void syncModules(List<JComboBox> modulesComboxs, int idx) {
        for (JComboBox modulesCombox : modulesComboxs) {
            modulesCombox.setSelectedIndex(idx);
            modulesCombox.revalidate();
            modulesCombox.repaint();
        }

    }

    public static String getModuleName(Module module) {
        return module.getName().replaceAll("\\.main", "");
    }
}
