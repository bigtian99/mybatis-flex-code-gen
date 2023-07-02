package club.bigtian.mf.plugin.core.plugin;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.windows.MybatisFlexSetting;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import org.jetbrains.annotations.Nls;
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