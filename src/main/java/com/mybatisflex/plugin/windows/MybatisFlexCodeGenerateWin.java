package com.mybatisflex.plugin.windows;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.containers.JBIterable;
import com.mybatisflex.plugin.core.Modules;
import com.mybatisflex.plugin.core.Package;
import com.mybatisflex.plugin.core.constant.MybatisFlexConstant;
import com.mybatisflex.plugin.core.persistent.MybatisFlexPluginConfigData;
import com.mybatisflex.plugin.core.plugin.MybatisFlexPluginSettings;
import com.mybatisflex.plugin.entity.ColumnInfo;
import com.mybatisflex.plugin.entity.TableInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private AnActionEvent actionEvent;
    List<JComboBox> list = Arrays.asList(cotrollerCombox, modelCombox, serviceInteCombox, serviceImplComBox, mapperComBox, xmlComBox);

    public MybatisFlexCodeGenerateWin(AnActionEvent actionEvent) {
        this.actionEvent = actionEvent;
        setContentPane(contentPane);
        setModal(true);
        setTitle("Mybatis Flex Code Generate");
        getRootPane().setDefaultButton(generateBtn);
        setSize(800, 450);
        // 将对话框相对于屏幕居中显示
        setLocationRelativeTo(null);
        Project project = actionEvent.getProject();
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

        List<TableInfo> selectedTableInfo = getSelectedTableInfo(actionEvent);


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
            MybatisFlexPluginConfigData.setData(MybatisFlexConstant.MYBATIS_FLEX_CONFIG, getConfigData());
            Messages.showMessageDialog(project, "保存成功", "提示", Messages.getInformationIcon());
        });
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
        initInput();
        // MybatisFlexPluginConfigData.clear();
        initConfigData();
    }

    private void initInput() {
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
                columnInfo.setComment(dasColumn.getComment());
                columnInfo.setType(dasColumn.getDataType().typeName);
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
        dispose();
    }

    /**
     * 取消按钮事件
     */
    private void onCancel() {
        dispose();
    }


    public String getConfigData() {
        JSONObject data = new JSONObject();
        data.put(MybatisFlexConstant.MODEL_PACKAGE_PATH, modelPackagePath.getText());
        data.put(MybatisFlexConstant.SERVICE_INTEFACE_PATH, serviceIntefacePath.getText());
        data.put(MybatisFlexConstant.SERVICE_IMP_PATH, serviceImpPath.getText());
        data.put(MybatisFlexConstant.MAPPER_PACKAGE_PATH, mapperPackagePath.getText());
        data.put(MybatisFlexConstant.MAPPER_XML_PATH, mapperXmlPath.getText());
        data.put(MybatisFlexConstant.CONTROLLER_PATH, controllerPath.getText());
        data.put(MybatisFlexConstant.SYNC_CHECK_BOX, syncCheckBox.isSelected());
        data.put(MybatisFlexConstant.COTROLLER_COMBOX, cotrollerCombox.getSelectedIndex());
        data.put(MybatisFlexConstant.MODEL_COMBOX, modelCombox.getSelectedIndex());
        data.put(MybatisFlexConstant.SERVICE_INTE_COMBOX, serviceInteCombox.getSelectedIndex());
        data.put(MybatisFlexConstant.SERVICE_IMPL_COM_BOX, serviceImplComBox.getSelectedIndex());
        data.put(MybatisFlexConstant.MAPPER_COM_BOX, mapperComBox.getSelectedIndex());
        data.put(MybatisFlexConstant.XML_COM_BOX, xmlComBox.getSelectedIndex());
        return data.toJSONString();
    }

    public  void initConfigData(){
        JSONObject configData = MybatisFlexPluginConfigData.getConfigData(MybatisFlexConstant.MYBATIS_FLEX_CONFIG);
        modelPackagePath.setText(configData.getString(MybatisFlexConstant.MODEL_PACKAGE_PATH));
        serviceIntefacePath.setText(configData.getString(MybatisFlexConstant.SERVICE_INTEFACE_PATH));
        serviceImpPath.setText(configData.getString(MybatisFlexConstant.SERVICE_IMP_PATH));
        mapperPackagePath.setText(configData.getString(MybatisFlexConstant.MAPPER_PACKAGE_PATH));
        mapperXmlPath.setText(configData.getString(MybatisFlexConstant.MAPPER_XML_PATH));
        controllerPath.setText(configData.getString(MybatisFlexConstant.CONTROLLER_PATH));
        syncCheckBox.setSelected(configData.getBooleanValue(MybatisFlexConstant.SYNC_CHECK_BOX,false));
        cotrollerCombox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.COTROLLER_COMBOX,1));
        modelCombox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.MODEL_COMBOX,1));
        serviceInteCombox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.SERVICE_INTE_COMBOX,1));
        serviceImplComBox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.SERVICE_IMPL_COM_BOX,1));
        mapperComBox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.MAPPER_COM_BOX,1));
        xmlComBox.setSelectedIndex(configData.getIntValue(MybatisFlexConstant.XML_COM_BOX,1));
    }


}
