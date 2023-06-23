package com.mybatisflex.plugin.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.mybatisflex.plugin.core.filter.FilterComboBoxModel;
import com.mybatisflex.plugin.core.render.ModuleComBoxRender;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
     * @param project       项目
     * @param modulesCombox
     * @return {@code List<String>}
     */
    public static void addModulesItem(Project project, List<JComboBox> modulesComboxs) {
        for (JComboBox modulesCombox : modulesComboxs) {
            modulesCombox.setRenderer(new ModuleComBoxRender());
            moduleMap = Arrays.stream(ModuleManager.getInstance(project).getModules())
                    .collect(Collectors.toMap(Module::getName, module -> module));
            FilterComboBoxModel model = new FilterComboBoxModel(moduleMap.keySet().stream().toList());
            modulesCombox.setModel(model);
            modulesCombox.setSelectedIndex(1);
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
