package com.mybatisflex.plugin.core.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.mybatisflex.plugin.core.Template;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import com.mybatisflex.plugin.core.constant.MybatisFlexConstant;
import com.mybatisflex.plugin.core.persistent.MybatisFlexPluginConfigData;
import com.mybatisflex.plugin.core.util.ProjectUtils;
import com.mybatisflex.plugin.windows.MybatisFlexSetting;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MybatisFlexPluginSettings implements Configurable {
    MybatisFlexSetting mybatisFlexSetting;

    boolean isModified;


    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建设置页面的UI组件
        mybatisFlexSetting = new MybatisFlexSetting(ProjectUtils.getCurrentProject(), e -> {
            isModified = e;
        });
        return mybatisFlexSetting.getMainPanel();
    }

    @Override
    public boolean isModified() {
        // 判断设置是否被修改
        return isModified;
    }

    @Override
    public void apply() throws ConfigurationException {
        MybatisFlexConfig config = mybatisFlexSetting.getConfigData();
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(config);
        Project project = ProjectManager.getInstance().getDefaultProject();
        Messages.showMessageDialog(project, "保存成功", "提示", Messages.getInformationIcon());
        isModified = false;
    }

    @Override
    public void reset() {
        // 重置设置
        isModified = false;
        mybatisFlexSetting.init();
    }

    @Override
    public void disposeUIResources() {
        // 释放UI资源
        Template.clear();
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