package com.mybatisflex.plugin.windows;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson2.JSON;
import com.intellij.lang.java.JavaLanguage;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageConstants;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.LanguageTextField;
import com.mybatisflex.plugin.core.Template;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import com.mybatisflex.plugin.core.constant.MybatisFlexConstant;
import com.mybatisflex.plugin.core.functions.SimpleFunction;
import com.mybatisflex.plugin.core.persistent.MybatisFlexPluginConfigData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MybatisFlexSetting {
    private JPanel mainPanel;
    private JPanel controllerTab;
    private JPanel modelTab;
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTextField tablePrefix;
    private LanguageTextField controllerTemplate;
    private LanguageTextField modelTemplate;
    private LanguageTextField interfaceTempalate;
    private LanguageTextField implTemplate;
    private LanguageTextField mapperTemplate;
    private LanguageTextField xmlTemplate;
    private JButton resetBtn;
    private JTextField author;
    private JTextField since;
    private JCheckBox builderCheckBox;
    private JCheckBox dataCheckBox;
    private JCheckBox allArgsConstructorCheckBox;
    private JCheckBox noArgsConstructorCheckBox;
    private JCheckBox swaggerCheckBox;
    private JTextField controllerSuffix;
    private JTextField interfaceSuffix;
    private JTextField implSuffix;
    private JTextField modelSuffix;
    private JTextField mapperSuffix;
    private JCheckBox cacheCheckBox;
    private JScrollPane scrollPane1;
    private JScrollPane scrollPane2;
    private JScrollPane scrollPane3;
    private JScrollPane scrollPane4;
    private JScrollPane scrollPane5;
    private JScrollPane scrollPane6;
    private JComboBox sinceConfigComBox;
    private JButton del;
    private JButton clearAll;

    private Project project;
    SimpleFunction callback;

    public MybatisFlexSetting(Project project, SimpleFunction<Boolean> callback) {
        this.callback = callback;
        this.project = project;
        init();

        resetBtn.addActionListener(e -> {
            int flag = Messages.showYesNoDialog(project, "确定要重置吗？", "提示", Messages.getQuestionIcon());
            if (MessageConstants.YES == flag) {
                MybatisFlexPluginConfigData.clear();
                init();
            }

        });
        clearAll.addActionListener(e -> {
            int flag = Messages.showYesNoDialog(project, "确定要清空吗？", "提示", Messages.getQuestionIcon());
            if (MessageConstants.YES == flag) {
                MybatisFlexPluginConfigData.clearSince();
                sinceConfigComBox.removeAllItems();
                sinceConfigComBox.repaint();
            }
        });

        del.addActionListener(e -> {
            int flag = Messages.showYesNoDialog(project, "确定要删除吗？", "提示", Messages.getQuestionIcon());
            if (MessageConstants.YES == flag) {
                String since = sinceConfigComBox.getSelectedItem().toString();
                MybatisFlexPluginConfigData.removeSinceConfig(since);
                sinceConfigComBox.removeItemAt(sinceConfigComBox.getSelectedIndex());
//                sinceConfigComBox.revalidate();
                sinceConfigComBox.repaint();
            }
        });
        initSinceComBox();
    }

    public void init() {
        controllerTemplate.setText(Template.getVmCode(MybatisFlexConstant.CONTROLLER_TEMPLATE));
        modelTemplate.setText(Template.getVmCode(MybatisFlexConstant.MODEL_TEMPLATE));
        interfaceTempalate.setText(Template.getVmCode(MybatisFlexConstant.INTERFACE_TEMPLATE));
        implTemplate.setText(Template.getVmCode(MybatisFlexConstant.IMPL_TEMPLATE));
        mapperTemplate.setText(Template.getVmCode(MybatisFlexConstant.MAPPER_TEMPLATE));
        xmlTemplate.setText(Template.getVmCode(MybatisFlexConstant.XML_TEMPLATE));
        tablePrefix.setText(Template.getTablePrefix());
        author.setText(Template.getAuthor());
        since.setText(Template.getSince());
        builderCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.LOMBOK_BUILDER));
        dataCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.LOMBOK_DATA));
        allArgsConstructorCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.LOMBOK_ALL_ARGS_CONSTRUCTOR));
        noArgsConstructorCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.LOMBOK_NO_ARGS_CONSTRUCTOR));
        swaggerCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.SWAGGER));
        controllerSuffix.setText(Template.getSuffix("controllerSuffix", controllerSuffix.getText()));
        interfaceSuffix.setText(Template.getSuffix("interfaceSuffix", interfaceSuffix.getText()));
        implSuffix.setText(Template.getSuffix("implSuffix", implSuffix.getText()));
        modelSuffix.setText(Template.getSuffix("modelSuffix", modelSuffix.getText()));
        mapperSuffix.setText(Template.getSuffix("mapperSuffix", mapperSuffix.getText()));
        cacheCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.CACHE));
        setEvent();
    }

    public void initSinceComBox() {
        Set<String> list = MybatisFlexPluginConfigData.getSinceMap().keySet();
        for (String item : list) {
            sinceConfigComBox.addItem(item);
        }
    }

    /**
     * 为控件添加事件
     */
    private void setEvent() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Field field : ReflectUtil.getFields(MybatisFlexSetting.class)) {
            Object fieldValue = ReflectUtil.getFieldValue(this, field.getName());
            if (fieldValue instanceof LanguageTextField fieldValue1) {
                fieldValue1.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void documentChanged(@NotNull DocumentEvent event) {
                        callback.apply(!Template.contains(fieldValue1.getText() + fieldValue1.getName()));
                    }
                });
            } else if (fieldValue instanceof JTextField fieldValue1) {
                fieldValue1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    @Override
                    public void insertUpdate(javax.swing.event.DocumentEvent e) {
                        callback.apply(!Template.contains(fieldValue1.getText() + fieldValue1.getName()));
                    }

                    @Override
                    public void removeUpdate(javax.swing.event.DocumentEvent e) {
                        callback.apply(!Template.contains(fieldValue1.getText() + fieldValue1.getName()));
                    }

                    @Override
                    public void changedUpdate(javax.swing.event.DocumentEvent e) {
                        callback.apply(!Template.contains(fieldValue1.getText() + fieldValue1.getName()));
                    }
                });
            } else if (fieldValue instanceof JCheckBox fieldValue1) {
                fieldValue1.addActionListener(e -> {
                    callback.apply(!Template.contains(fieldValue1.isSelected() + fieldValue1.getName()));
                });
            }else if(fieldValue instanceof  JCheckBox fieldValue1){
                JCheckBox checkBox = (JCheckBox) fieldValue1;
                fieldValue1.addActionListener(e -> {
                    callback.apply(!Template.contains(fieldValue1.isSelected() + checkBox.getName()));
                });

            }
        }

    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * 创建自定义控件
     */
    private void createUIComponents() {
        controllerTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        modelTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        interfaceTempalate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        implTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        mapperTemplate = new LanguageTextField(JavaLanguage.INSTANCE, project, "", false);
        xmlTemplate = new LanguageTextField(XMLLanguage.INSTANCE, project, "", false);
        scrollPane1 = new JScrollPane();
        scrollPane2 = new JScrollPane();
        scrollPane3 = new JScrollPane();
        scrollPane4 = new JScrollPane();
        scrollPane5 = new JScrollPane();
        scrollPane6 = new JScrollPane();
        scrollPane1.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane2.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane3.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane4.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane5.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane6.getVerticalScrollBar().setUnitIncrement(10);
    }


    public String getConfigData() {
        MybatisFlexConfig config = new MybatisFlexConfig();
        config.setControllerTemplate(controllerTemplate.getText());
        config.setModelTemplate(modelTemplate.getText());
        config.setInterfaceTempalate(interfaceTempalate.getText());
        config.setImplTemplate(implTemplate.getText());
        config.setMapperTemplate(mapperTemplate.getText());
        config.setXmlTemplate(xmlTemplate.getText());
        config.setTablePrefix(tablePrefix.getText());
        config.setAuthor(author.getText());
        config.setSince(since.getText());
        config.setBuilder(builderCheckBox.isSelected());
        config.setData(dataCheckBox.isSelected());
        config.setAllArgsConstructor(allArgsConstructorCheckBox.isSelected());
        config.setNoArgsConstructor(noArgsConstructorCheckBox.isSelected());
        config.setSwagger(swaggerCheckBox.isSelected());
        config.setControllerSuffix(controllerSuffix.getText());
        config.setInterfaceSuffix(interfaceSuffix.getText());
        config.setImplSuffix(implSuffix.getText());
        config.setModelSuffix(modelSuffix.getText());
        config.setMapperSuffix(mapperSuffix.getText());
        config.setCache(cacheCheckBox.isSelected());
        return JSON.toJSONString(config);
    }

}
