package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.FileChooserUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

public class MybatisFlexSettingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel mainPanel;
    private com.intellij.ui.components.fields.ExpandableTextField tablePrefix;
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
    private JCheckBox overrideCheckBox;
    private JButton exportBtn;
    private JButton importBtn;
    private JTabbedPane tabbedPane1;
    private JPanel controllerTab;
    private JPanel modelTab;
    private JPanel panel1;
    private JButton restBtn;
    private JCheckBox swagger3CheckBox;
    private JButton returnBtn;
    private com.intellij.ui.components.fields.ExpandableTextField logicTF;
    private JTextField contrPath;
    private JTextField servicePath;
    private JTextField implPath;
    private JTextField domainPath;
    private JTextField xmlPath;
    private JTextField mapperPath;
    private JCheckBox accessorsCheckBox;
    private JTabbedPane tabbedPane2;
    private Project project;

    public MybatisFlexSettingDialog(Project project) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.getWidth() * 0.8, screenSize.getHeight() * 0.8);
        setSize(screenSize);
        getRootPane().setDefaultButton(buttonOK);
        DialogUtil.centerShow(this);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        init();

        resetBtn.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定要重置吗？", "提示", Messages.getQuestionIcon());
            if (0 == flag) {
                MybatisFlexPluginConfigData.clear();
                init();
                Messages.showInfoMessage("重置成功", "提示");
            }
        });

        clearAll.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定要清空吗？", "提示", Messages.getQuestionIcon());
            if (0 == flag) {
                MybatisFlexPluginConfigData.clearSince();
                sinceConfigComBox.removeAllItems();
                sinceConfigComBox.repaint();
                Messages.showInfoMessage("清空成功", "提示");
            }
        });

        del.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定要删除吗？", "提示", Messages.getQuestionIcon());
            if (0 == flag) {
                Object selectedItem = sinceConfigComBox.getSelectedItem();
                if (ObjectUtil.isNull(selectedItem)) {
                    Messages.showErrorDialog("请选择要删除的配置", "提示");
                    return;
                }
                String since = selectedItem.toString();
                MybatisFlexPluginConfigData.removeSinceConfig(since);
                sinceConfigComBox.removeItemAt(sinceConfigComBox.getSelectedIndex());
                sinceConfigComBox.repaint();
                Messages.showInfoMessage("删除成功", "提示");
            }
        });

        exportBtn.addActionListener(e -> {

            String exportPath = FileChooserUtil.chooseDirectory(project);

            if (StrUtil.isEmpty(exportPath)) {
                return;
            }
            MybatisFlexPluginConfigData.export(exportPath);
        });

        importBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooserUtil.chooseFileVirtual(project);
            if (ObjectUtil.isNull(virtualFile)) {
                return;
            }
            String path = virtualFile.getPath();
            MybatisFlexPluginConfigData.importConfig(path);
            init();
        });

        restBtn.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定要恢复自带模板吗？", "提示", Messages.getQuestionIcon());
            if (0 == flag) {
                MybatisFlexPluginConfigData.clearCode();
                init();
                Messages.showInfoMessage("恢复成功", "提示");
            }
        });

        swagger3CheckBox.addActionListener(e -> {
            if (swagger3CheckBox.isSelected()) {
                boolean selected = swaggerCheckBox.isSelected();
                if (selected) {
                    Messages.showWarningDialog("swagger2和swagger3只能选一个", "提示");
                    swagger3CheckBox.setSelected(false);
                }
            }
        });
        swaggerCheckBox.addActionListener(e -> {
            if (swaggerCheckBox.isSelected()) {
                boolean selected = swagger3CheckBox.isSelected();
                if (selected) {
                    Messages.showWarningDialog("swagger2和swagger3只能选一个", "提示");
                    swaggerCheckBox.setSelected(false);
                }
            }
        });

        returnBtn.addActionListener(e -> {
            ReturnInfoDialog dialog = new ReturnInfoDialog();
            dialog.show();
        });
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
        controllerSuffix.setText(Template.getSuffix(MybatisFlexConstant.CONTROLLER_SUFFIX, controllerSuffix.getText()));
        interfaceSuffix.setText(Template.getSuffix(MybatisFlexConstant.INTERFACE_SUFFIX, interfaceSuffix.getText()));
        implSuffix.setText(Template.getSuffix(MybatisFlexConstant.IMPL_SUFFIX, implSuffix.getText()));
        modelSuffix.setText(Template.getSuffix(MybatisFlexConstant.MODEL_SUFFIX, modelSuffix.getText()));
        mapperSuffix.setText(Template.getSuffix(MybatisFlexConstant.MAPPER_SUFFIX, mapperSuffix.getText()));
        cacheCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.CACHE));
        overrideCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.OVERRIDE));
        swagger3CheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.SWAGGER3));
        logicTF.setText(Template.getConfigData(MybatisFlexConstant.LOGIC_DELETE_FIELD));
        contrPath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.CONTR_PATH), contrPath.getText()));
        servicePath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.SERVICE_PATH), servicePath.getText()));
        implPath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.IMPL_PATH), implPath.getText()));
        domainPath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.DOMAIN_PATH), domainPath.getText()));
        xmlPath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.XML_PATH), xmlPath.getText()));
        mapperPath.setText(ObjectUtil.defaultIfBlank(Template.getConfigData(MybatisFlexConstant.MAPPER_PATH), mapperPath.getText()));
        accessorsCheckBox.setSelected(Template.getChecBoxConfig(MybatisFlexConstant.LOMBOK_ACCESSORS));
        initSinceComBox();
    }

    public void initSinceComBox() {
        Set<String> list = MybatisFlexPluginConfigData.getSinceMap().keySet();
        sinceConfigComBox.removeAllItems();
        for (String item : list) {
            sinceConfigComBox.insertItemAt(item, 0);
        }
        if (sinceConfigComBox.getItemCount() > 0) {
            sinceConfigComBox.setSelectedIndex(0);
        }
        sinceConfigComBox.revalidate();
        sinceConfigComBox.repaint();
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


    public MybatisFlexConfig getConfigData() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
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
        config.setSwagger3(swagger3CheckBox.isSelected());
        config.setOverrideCheckBox(overrideCheckBox.isSelected());
        config.setLogicDeleteField(logicTF.getText());
        config.setContrPath(contrPath.getText());
        config.setServicePath(servicePath.getText());
        config.setImplPath(implPath.getText());
        config.setDomainPath(domainPath.getText());
        config.setXmlPath(xmlPath.getText());
        config.setMapperPath(mapperPath.getText());
        config.setAccessors(accessorsCheckBox.isSelected());
        return config;
    }


    private void onOK() {
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(getConfigData());
        Messages.showInfoMessage("保存成功", "提示");
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
