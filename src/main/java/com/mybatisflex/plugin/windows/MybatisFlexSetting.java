package com.mybatisflex.plugin.windows;

import com.intellij.lang.java.JavaLanguage;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;

public class MybatisFlexSetting {
    private JPanel mainPanel;
    private JTextField textField1;
    private JTabbedPane tabbedPane1;
    private JPanel controllerTab;
    private JPanel modelTab;
    private LanguageTextField controllerTemplate;
    private JToolBar toolBar1;
    private LanguageTextField modelTemplate;
    private LanguageTextField interfaceTempalate;
    private LanguageTextField implTemplate;
    private LanguageTextField mapperTemplate;
    private LanguageTextField xmlTemplate;

    private Project project;

    public MybatisFlexSetting(Project project) {


    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        controllerTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        modelTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        interfaceTempalate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        implTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        mapperTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        xmlTemplate = new LanguageTextField(XMLLanguage.INSTANCE, project, "", false);

    }
}
