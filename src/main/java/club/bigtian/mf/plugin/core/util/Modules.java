package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.filter.FilterComboBoxModel;
import club.bigtian.mf.plugin.core.render.ModuleComBoxRender;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class Modules {
    private static Map<String, Module> moduleMap;

    /**
     * 获取模块
     *
     * @param project 项目
     * @return {@code List<String>}
     */
    public static void addModulesItem(Project project, List<JComboBox> modulesComboxs) {
        VirtualFile virtualFile = project.getBaseDir().findChild("pom.xml");
        boolean isManvenProject = ObjectUtil.isNotNull(virtualFile);
        Module[] modules = ModuleManager.getInstance(project).getModules();
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
                    .collect(Collectors.toMap(Module::getName, module -> module));
            FilterComboBoxModel model = new FilterComboBoxModel(moduleMap.keySet().stream().toList());
            modulesCombox.setModel(model);
            modulesCombox.setSelectedIndex(0);
        }
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
        return getModulePath(module);
    }

    public static String getModulePath(Module module) {
        Project project = module.getProject();
        return project.getBasePath() + File.separator;
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
}
