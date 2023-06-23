package com.mybatisflex.plugin.core.plugin;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MybatisFlexPluginSettings implements Configurable {
    private JPanel myPanel;

    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建设置页面的UI组件
        myPanel = new JPanel();
        myPanel.add(new JLabel("My Plugin Settings龙艳华大傻狗"));
        return myPanel;
    }

    @Override
    public boolean isModified() {
        // 判断设置是否被修改
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        // 应用设置
    }

    @Override
    public void reset() {
        // 重置设置
    }

    @Override
    public void disposeUIResources() {
        // 释放UI资源
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        // 获取设置页面的显示名称
        return "My Plugin Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        // 获取设置页面的帮助主题
        return null;
    }
}