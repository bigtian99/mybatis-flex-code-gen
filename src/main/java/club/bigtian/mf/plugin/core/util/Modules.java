package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.config.CustomConfig;
import club.bigtian.mf.plugin.core.filter.FilterComboBoxModel;
import club.bigtian.mf.plugin.core.render.ModuleComBoxRender;
import club.bigtian.mf.plugin.core.search.InvertedIndexSearch;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
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
    private static Map<String, Module> moduleMap;
    public static Map<String, Map<String, String>> modulePackageMap;
    private static Boolean isManvenProject;


    public static boolean containsModule(String moduleName) {
        return moduleMap.containsKey(moduleName);
    }

    public static Set<String> getModuleNames() {
        return moduleMap.keySet();
    }

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
            return "";
            // throw new RuntimeException(StrUtil.format("模块不存在:{}", moduleName));
        }
        return moduleMap.getOrDefault(packageName, "");
    }

    public static Module[] getModule(Project project) {
        return ModuleManager.getInstance(project).getModules();
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
                    .filter(module -> {
                        if (isManvenProject) {
                            return ArrayUtil.isNotEmpty(ModuleRootManager.getInstance(module).getSourceRoots());
                        }
                        // 非maven项目只显示main模块,只有main模块才有java目录
                        return module.getName().contains(".main");
                    })
                    .collect(Collectors.toMap(el -> {
                        String name = el.getName();
                        if (name.contains(".")) {
                            String[] strArr = name.split("\\.");
                            return strArr[strArr.length - 2];
                        }
                        return name;
                    }, module -> module));
            List<String> moduleList = moduleMap.keySet().stream().collect(Collectors.toList());
            FilterComboBoxModel model = new FilterComboBoxModel(moduleList);
            modulesCombox.setModel(model);
            setModuleText(modulesCombox);
        }
        getModulePackages();
    }

    public static void setModuleText(JComboBox comboBox) {
        List<String> moduleList = moduleMap.keySet().stream().collect(Collectors.toList());
        JTextField field = (JTextField) comboBox.getEditor().getEditorComponent();
        field.setText(moduleList.get(0));
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
                    PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
                    if (aPackage != null) {
                        moduleMap.put(aPackage.getName(), aPackage.getQualifiedName());
                    }
                }
                return true;
            });
            String name = module.getName();
            if (name.contains(".")) {
                String[] strArr = name.split("\\.");
                name = strArr[strArr.length - 2];
            }
            modulePackageMap.put(name, moduleMap);
        }
        InvertedIndexSearch.indexText(modulePackageMap.keySet(), "module");
    }

    /**
     * 判断是否manven项目
     *
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

    public static String getProjectTypeSuffix(Module module) {
        return isManvenProject(module) ? ".java" : ".kt";
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
            setSelectedItem(serviceImplComBox, serviceInteCombox.getSelectedItem().toString());
        });
        serviceImplComBox.addActionListener(e -> {
            setSelectedItem(serviceInteCombox, serviceImplComBox.getSelectedItem().toString());
        });
    }

    public static void setSelectedItem(JComboBox comboBox, String item) {
        comboBox.setSelectedItem(item);
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.setText(item);
        comboBox.revalidate();
        comboBox.repaint();
    }

    /**
     * 同步controller模块
     *
     * @param modulesComboxs
     * @param idx
     */
    public static void syncModules(List<JComboBox> modulesComboxs, Object selectItem) {
        for (JComboBox modulesCombox : modulesComboxs) {
            JTextField textField = (JTextField) modulesCombox.getEditor().getEditorComponent();
            textField.setText(selectItem.toString());
            modulesCombox.setSelectedItem(selectItem);
            modulesCombox.revalidate();
            modulesCombox.repaint();
        }

    }

    public static Module getModuleForFile(PsiJavaFile file) {
        return ModuleUtilCore.findModuleForPsiElement(file);
    }

    public static String getModuleName(Module module) {
        return module.getName().replaceAll("\\.main", "");
    }

    public static String getPath(Module moduleForFile) {
        PsiDirectory moduleDirectory = getModuleDirectory(moduleForFile, JavaModuleSourceRootTypes.SOURCES);
        if (moduleDirectory != null) {
            String path = moduleDirectory.getVirtualFile().getPath();
            return StrUtil.subBefore(path, "src", false);
        }
        return "";
    }


    public static CustomConfig moduleConfig(Module module) {
        if (ObjectUtil.isNull(module)) {
            return new CustomConfig();
        }
        String path = getPath(module);
        PsiFile file = null;
        PsiDirectory psiDirectory = VirtualFileUtils.getPsiDirectory(module.getProject(), path);
        while (ObjectUtil.isNull(file) && ObjectUtil.isNotNull(psiDirectory)) {
            file = psiDirectory.findFile("mybatis-flex.config");
            if (ObjectUtil.isNull(file)) {
                // 往上找
                psiDirectory = psiDirectory.getParent();
            }
        }
        if (ObjectUtil.isNull(file)) {
            return new CustomConfig();
        }
        CustomConfig config = new CustomConfig();
        try {
            Arrays.stream(file.getText().split("\n"))
                    .filter(el -> el.startsWith("processor"))
                    .forEach(el -> {
                        String text = StrUtil.subAfter(el, ".", false);
                        if (StrUtil.count(el, ".") > 1) {
                            String[] split = text.split("\\.");
                            text = split[0];
                            if (split.length > 1) {
                                text += StrUtil.upperFirst(split[1]);
                            }
                        }
                        String prefix = StrUtil.toCamelCase(StrUtil.subBefore(text, "=", false)).trim();
                        String suffix = StrUtil.subAfter(text, "=", false).trim();
                        ReflectUtil.setFieldValue(config, prefix, suffix);
                    });
        } catch (Exception e) {

        }
        return config;
    }

    public static Module getModuleFromDirectory(PsiDirectory directory) {
        return ModuleUtilCore.findModuleForFile(directory.getVirtualFile(), directory.getProject());
    }
}
