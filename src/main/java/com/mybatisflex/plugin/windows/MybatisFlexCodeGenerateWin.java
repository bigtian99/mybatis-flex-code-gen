package com.mybatisflex.plugin.windows;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.containers.JBIterable;
import com.mybatisflex.plugin.core.util.Modules;
import com.mybatisflex.plugin.core.Package;
import com.mybatisflex.plugin.core.RenderMybatisFlexTemplate;
import com.mybatisflex.plugin.core.Template;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import com.mybatisflex.plugin.core.persistent.MybatisFlexPluginConfigData;
import com.mybatisflex.plugin.core.plugin.MybatisFlexPluginSettings;
import com.mybatisflex.plugin.core.validator.InputValidatorImpl;
import com.mybatisflex.plugin.entity.ColumnInfo;
import com.mybatisflex.plugin.entity.TableInfo;
import com.mybatisflex.plugin.utils.DDLUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.database.view.DatabaseView.DATABASE_NODES;

public class MybatisFlexCodeGenerateWin extends JDialog {
    private JPanel contentPane;
    private JButton generateBtn;
    private JButton cancelBtn;
    private JComboBox IdTypeCombox;
    private JComboBox cotrollerCombox;
    private ExtendableTextField modelPackagePath;
    private JPanel modelPanel;
    private ExtendableTextField mapperPackagePath;
    private com.intellij.openapi.ui.FixedSizeButton mapperBtn;
    private com.intellij.openapi.ui.FixedSizeButton modelBtn;
    private JTextField serviceIntefacePath;
    private com.intellij.openapi.ui.FixedSizeButton serviceInterfaceBtn;
    private JTextField serviceImpPath;
    private com.intellij.openapi.ui.FixedSizeButton serviceImplBtn;
    private JTextField controllerPath;
    private com.intellij.openapi.ui.FixedSizeButton controllerBtn;
    private JComboBox modelCombox;
    private JComboBox serviceInteCombox;
    private JComboBox serviceImplComBox;
    private JComboBox mapperComBox;
    private JTextField mapperXmlPath;
    private FixedSizeButton mapperXmlBtn;
    private JComboBox xmlComBox;
    private JCheckBox syncCheckBox;
    private ActionLink settingLabel;
    private JButton saveConfig;
    private JButton restBtn;
    private JComboBox sinceComBox;
    private AnActionEvent actionEvent;
    List<JComboBox> list = Arrays.asList(cotrollerCombox, modelCombox, serviceInteCombox, serviceImplComBox, mapperComBox, xmlComBox);
    List<JTextField> packageList = Arrays.asList(controllerPath, modelPackagePath, serviceIntefacePath, serviceImpPath, mapperPackagePath, mapperXmlPath);
    Project project;

    public MybatisFlexCodeGenerateWin(AnActionEvent actionEvent) {
        this.actionEvent = actionEvent;
        setContentPane(contentPane);
        setModal(true);
        setTitle("Mybatis Flex Code Generate");
        getRootPane().setDefaultButton(generateBtn);
        setSize(800, 450);
        // 将对话框相对于屏幕居中显示
        setLocationRelativeTo(null);
        project = actionEvent.getProject();
        generateBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGenerate();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        init(project);

        syncCheckBox.addActionListener(e -> {
            if (syncCheckBox.isSelected()) {
                Modules.syncModules(list, cotrollerCombox.getSelectedIndex());
            }
        });
        cotrollerCombox.addActionListener(e -> {
            if (syncCheckBox.isSelected()) {
                Modules.syncModules(list, cotrollerCombox.getSelectedIndex());
            }
        });

        settingLabel.addActionListener(e -> new ShowSettingsUtilImpl().showSettingsDialog(project, MybatisFlexPluginSettings.class));
        saveConfig.addActionListener(e -> {
            Messages.InputDialog dialog = new Messages.InputDialog("请输入配置名称", "配置名称", Messages.getQuestionIcon(), "", new InputValidatorImpl());
            dialog.show();
            String configName = dialog.getInputString();
            if (StrUtil.isEmpty(configName)) {
                return;
            }
            Map<String, MybatisFlexConfig> configMap = new HashMap<>();
            configMap.put(configName, getConfigData());
            MybatisFlexPluginConfigData.configSince(configMap);
            Messages.showMessageDialog(project, "保存成功", "提示", Messages.getInformationIcon());
            sinceComBox.addItem(configName);
            sinceComBox.repaint();

        });
        restBtn.addActionListener(e -> {

            MybatisFlexPluginConfigData.clearCode();
            Messages.showMessageDialog(project, "清除成功", "提示", Messages.getInformationIcon());
        });

        sinceComBox.addActionListener(e -> {
            Object selectedItem = sinceComBox.getSelectedItem();
            if (ObjectUtil.isNotNull(selectedItem)) {
                String key = selectedItem.toString();
                MybatisFlexConfig config = MybatisFlexPluginConfigData.getConfig(key);
                initConfigData(config);
            }
        });
        initSinceComBox();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                super.windowActivated(e);
                initSinceComBox();
            }
        });
    }

    public void initSinceComBox() {
        Set<String> list = MybatisFlexPluginConfigData.getSinceMap().keySet();
        sinceComBox.removeAllItems();
        for (String item : list) {
            sinceComBox.addItem(item);
        }
        sinceComBox.revalidate();
        sinceComBox.repaint();
    }

    /**
     * 初始化工作
     *
     * @param project
     */
    private void init(Project project) {
        // 初始化模块
        Modules.initModules(project, list);
        Modules.comBoxGanged(serviceInteCombox, serviceImplComBox);
        initBtn();
        initConfigData(null);
        initPackageList();
    }

    public void initPackageList() {
        packageList.stream().forEach(textField -> {
            ComponentValidator validator = new ComponentValidator(project);
            validator.withValidator(() -> {
                String pt = textField.getText();
                return StrUtil.isEmpty(pt) ? new ValidationInfo("请选择生成路径", textField) : null;
            }).installOn(textField);

            textField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    ComponentValidator.getInstance(textField).ifPresent(v -> v.revalidate());
                    enableGenerate();
                }
            });
        });
    }


    public void enableGenerate() {
        Set<Boolean> booleanSet = packageList.stream()
                .map(el -> StrUtil.isNotEmpty(el.getText()))
                .collect(Collectors.toSet());
        generateBtn.setEnabled(booleanSet.contains(true) && booleanSet.size() == 1);
    }

    private void initBtn() {
        mapperBtn.addActionListener(e -> mapperPackagePath.setText(Package.selectPackage(Modules.getModule(mapperComBox.getSelectedItem().toString()))));
        modelBtn.addActionListener(e -> modelPackagePath.setText(Package.selectPackage(Modules.getModule(modelCombox.getSelectedItem().toString()))));
        serviceInterfaceBtn.addActionListener(e -> {
            String packagePath = Package.selectPackage(Modules.getModule(serviceInteCombox.getSelectedItem().toString()));
            serviceIntefacePath.setText(packagePath);
            serviceImpPath.setText(packagePath + ".impl");
        });
        serviceImplBtn.addActionListener(e -> {
            String packagePath = Package.selectPackage(Modules.getModule(serviceImplComBox.getSelectedItem().toString()));
            serviceImpPath.setText(packagePath);
            serviceIntefacePath.setText(packagePath.substring(0, packagePath.lastIndexOf(".")));
        });
        controllerBtn.addActionListener(e -> {
            controllerPath.setText(Package.selectPackage(Modules.getModule(cotrollerCombox.getSelectedItem().toString())));
        });
        mapperXmlBtn.addActionListener(e -> {
            mapperXmlPath.setText(Package.selectPackageResources(Modules.getModule(xmlComBox.getSelectedItem().toString())));
        });
    }


    /**
     * 得到选中表信息
     *
     * @param actionEvent 行动事件
     * @return {@code List<TableInfo>}
     */
    private List<TableInfo> getSelectedTableInfo(AnActionEvent actionEvent) {
        List<TableInfo> tableInfoList = new ArrayList<>();
        List<DasObject> selectedTableList = Arrays.stream(actionEvent.getData(DATABASE_NODES)).map(el -> (DasObject) el).collect(Collectors.toList());
        for (DasObject dasObject : selectedTableList) {
            TableInfo tableInfo = new TableInfo();
            DbTableImpl table = (DbTableImpl) actionEvent.getData(CommonDataKeys.PSI_ELEMENT);
            tableInfo.setName(dasObject.getName());
            tableInfo.setComment(dasObject.getComment());
            List<ColumnInfo> columnList = new ArrayList<>();
            JBIterable<? extends DasObject> columns = dasObject.getDasChildren(ObjectKind.COLUMN);
            for (DasObject column : columns) {
                ColumnInfo columnInfo = new ColumnInfo();
                DasColumn dasColumn = (DasColumn) column;
                columnInfo.setName(dasColumn.getName());
                columnInfo.setFieldName(StrUtil.toCamelCase(dasColumn.getName()));
                columnInfo.setFieldType(DDLUtils.mapFieldType(dasColumn.getDataType().typeName));
                columnInfo.setComment(dasColumn.getComment());
                columnInfo.setMethodName(StrUtil.upperFirst(columnInfo.getFieldName()));
                columnInfo.setType(DDLUtils.getFieldType(dasColumn.getDataType().typeName).toUpperCase());
                columnInfo.setPrimaryKey(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.PRIMARY_KEY));
                columnInfo.setAutoIncrement(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.AUTO_GENERATED));
                columnList.add(columnInfo);
            }
            tableInfo.setColumnList(columnList);
            tableInfoList.add(tableInfo);
        }
        return tableInfoList;
    }


    /**
     * 生成按钮事件
     */
    private void onGenerate() {
        List<TableInfo> selectedTableInfo = getSelectedTableInfo(actionEvent);
        RenderMybatisFlexTemplate.assembleData(selectedTableInfo, getConfigData(), actionEvent.getProject());
        Messages.showDialog("代码生成成功", "提示", new String[]{"确定"}, -1, Messages.getInformationIcon());
        dispose();
    }

    /**
     * 取消按钮事件
     */
    private void onCancel() {
        dispose();
    }


    public MybatisFlexConfig getConfigData() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        config.setControllerPackage(controllerPath.getText());
        config.setControllerModule(cotrollerCombox.getSelectedItem().toString());
        config.setMapperPackage(mapperPackagePath.getText());
        config.setMapperModule(mapperComBox.getSelectedItem().toString());
        config.setXmlPackage(mapperXmlPath.getText());
        config.setXmlModule(xmlComBox.getSelectedItem().toString());
        config.setModelPackage(modelPackagePath.getText());
        config.setModelModule(modelCombox.getSelectedItem().toString());
        config.setInterfacePackage(serviceIntefacePath.getText());
        config.setInterfaceModule(serviceInteCombox.getSelectedItem().toString());
        config.setImplPackage(serviceImpPath.getText());
        config.setImplModule(serviceImplComBox.getSelectedItem().toString());
        config.setSync(syncCheckBox.isSelected());
        config.setIdType(IdTypeCombox.getSelectedItem().toString());
        return config;
    }

    public void initConfigData(MybatisFlexConfig config) {
        if (ObjectUtil.isNull(config)) {
            config = Template.getMybatisFlexConfig();
        }
        controllerPath.setText(config.getControllerPackage());
        String controllerModule = config.getControllerModule();
        if (StrUtil.isNotEmpty(controllerModule)) {
            cotrollerCombox.setSelectedItem(controllerModule);
        }
        mapperPackagePath.setText(config.getMapperPackage());
        String mapperModule = config.getMapperModule();
        if (StrUtil.isNotEmpty(mapperModule)) {
            mapperComBox.setSelectedItem(mapperModule);
        }
        mapperXmlPath.setText(config.getXmlPackage());
        String xmlModule = config.getXmlModule();
        if (StrUtil.isNotEmpty(xmlModule)) {
            xmlComBox.setSelectedItem(xmlModule);
        }
        modelPackagePath.setText(config.getModelPackage());
        String modelModule = config.getModelModule();
        if (StrUtil.isNotEmpty(modelModule)) {
            modelCombox.setSelectedItem(modelModule);
        }
        serviceIntefacePath.setText(config.getInterfacePackage());
        String interfaceModule = config.getInterfaceModule();
        if (StrUtil.isNotEmpty(interfaceModule)) {
            serviceInteCombox.setSelectedItem(interfaceModule);
        }
        serviceImpPath.setText(config.getImplPackage());
        String implModule = config.getImplModule();
        if (StrUtil.isNotEmpty(implModule)) {
            serviceImplComBox.setSelectedItem(implModule);
        }
        syncCheckBox.setSelected(config.isSync());
        for (JComboBox jComboBox : list) {
            Object selectedItem = jComboBox.getSelectedItem();
            if (ObjectUtil.isNotEmpty(selectedItem)) {
                jComboBox.repaint();
            }
        }
    }


}
