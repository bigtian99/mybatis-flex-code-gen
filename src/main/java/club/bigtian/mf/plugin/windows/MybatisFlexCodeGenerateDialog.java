package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.RenderMybatisFlexTemplate;
import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.render.TableListCellRenderer;
import club.bigtian.mf.plugin.core.search.InvertedIndexSearch;
import club.bigtian.mf.plugin.core.util.Package;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.core.validator.InputValidatorImpl;
import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MybatisFlexCodeGenerateDialog extends JDialog {
    public static final String SINCE_CONFIG = "---请选择配置---";
    public static final String SINCE_CONFIG_ADD = "添加配置";
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
    private FixedSizeButton settingLabel;

    private JComboBox sinceComBox;
    private JList tableList;
    private JCheckBox selectAllChexBox;
    private JTextField tableSearch;
    private FixedSizeButton sortBtn;
    private JProgressBar progressBar;
    private JCheckBox strictComBox;
    private JButton button1;

    private AnActionEvent actionEvent;
    List<JComboBox> list = Arrays.asList(cotrollerCombox, modelCombox, serviceInteCombox, serviceImplComBox, mapperComBox, xmlComBox);
    List<JTextField> packageList = Arrays.asList(controllerPath, modelPackagePath, serviceIntefacePath, serviceImpPath, mapperPackagePath, mapperXmlPath);
    Project project;
    List<String> tableNameList;

    Map<String, TableInfo> tableInfoMap;

    private boolean sinceFlag;


    public MybatisFlexCodeGenerateDialog(AnActionEvent actionEvent) {
        this.actionEvent = actionEvent;
        setContentPane(contentPane);
        setModal(true);
        setTitle("Mybatis Flex Code Generate");
        getRootPane().setDefaultButton(generateBtn);
        setSize(1050, 460);
        DialogUtil.centerShow(this);
        project = actionEvent.getProject();

        ProjectUtils.setCurrentProject(project);
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

        settingLabel.addActionListener(e -> {
            Set<String> sinces = MybatisFlexPluginConfigData.getSinceMap().keySet();
            MybatisFlexSettingDialog dialog = new MybatisFlexSettingDialog(project);
            dialog.show();
            sinceFlag = true;
            //避免用户配置后，直接点击设置界面，再回来导致配置丢失
            MybatisFlexConfig configData = getConfigData();
            Set<String> sinceSet = MybatisFlexPluginConfigData.getSinceMap().keySet();

            if (sinces.size() > sinceSet.size()) {
                initSinceComBox(0);
            } else {
                initSinceComBox(CollUtil.isEmpty(list) ? null : sinceComBox.getSelectedIndex());
            }
            //再次设置是因为initSinceComBox最终会把sinceFlag设置为false
            sinceFlag = true;
            initConfigData(configData);
        });

        sinceComBox.addActionListener(e -> {
            Object selectedItem = sinceComBox.getSelectedItem();
            if (ObjectUtil.isNull(selectedItem)) {
                return;
            }
            if (selectedItem.toString().equals(SINCE_CONFIG_ADD)) {
                sinceComBox.hidePopup();
                Messages.InputDialog dialog = new Messages.InputDialog("请输入配置名称", "配置名称", Messages.getQuestionIcon(), "", new InputValidatorImpl());
                dialog.show();
                String configName = dialog.getInputString();
                if (StrUtil.isEmpty(configName)) {
                    return;
                }
                MybatisFlexPluginConfigData.configSince(configName, getConfigData());
                NotificationUtils.notifySuccess("保存成功", project);
                initSinceComBox(null);
                return;
            }
            String key = selectedItem.toString();
            MybatisFlexConfig config = MybatisFlexPluginConfigData.getConfig(key);
            sinceFlag = !SINCE_CONFIG.equals(selectedItem.toString());
            initConfigData(config);
        });
        initSinceComBox(null);
        initPackagePath();

        tableList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            boolean selected = selectAllChexBox.isSelected();
            if (selected) {
                selectAllChexBox.setSelected(false);
            }
            int size = tableList.getSelectedValuesList().size();
            if (size == tableList.getModel().getSize() && size > 0) {
                selectAllChexBox.setSelected(true);
            }
        });
        tableInfoMap = TableUtils.getAllTables(actionEvent)
                .stream()
                .collect(Collectors.toMap(TableInfo::getName, Function.identity()));

        DefaultListModel model = new DefaultListModel();
        // tableNameSet按照字母降序
        tableNameList = new ArrayList<>(tableInfoMap.keySet());
        // 初始化倒排索引
        InvertedIndexSearch.indexText(tableNameList);
        Collections.sort(tableNameList);
        model.addAll(tableNameList);
        tableList.setModel(model);
        TableListCellRenderer cellRenderer = new TableListCellRenderer(tableInfoMap);
        tableList.setCellRenderer(cellRenderer);
        sortBtn.addActionListener(e -> {
            String tableName = tableSearch.getText();
            tableNameList = search(tableName, cellRenderer).stream().collect(Collectors.toList());
            if (sortBtn.getToolTipText().equals("升序")) {
                sortBtn.setToolTipText("降序");
                Collections.sort(tableNameList, Comparator.reverseOrder());
            } else {
                sortBtn.setToolTipText("升序");
                Collections.sort(tableNameList);
            }
            selectAllChexBox.setSelected(false);
            model.removeAllElements();
            model.addAll(tableNameList);
        });

        selectAllChexBox.addActionListener(e -> {
            if (selectAllChexBox.isSelected()) {
                tableList.setSelectionInterval(0, tableList.getModel().getSize() - 1);
            } else {
                tableList.clearSelection();
            }
        });

        tableSearch.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String tableName = tableSearch.getText();
                Set<String> search = search(tableName, cellRenderer);
                model.removeAllElements();
                model.addAll(search);
            }
        });
        setSelectTalbe(actionEvent);

        strictComBox.addChangeListener(e -> generateBtn.setEnabled(!strictComBox.isSelected()));

        cotrollerCombox.addActionListener(e -> {
            if (sinceFlag) {
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            controllerPath.setText(Modules.getPackagePath(cotrollerCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getContrPath(), "controller")));
        });
        modelCombox.addActionListener(e -> {
            if (sinceFlag) {
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            modelPackagePath.setText(Modules.getPackagePath(modelCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getDomainPath(), "domain")));
        });
        serviceInteCombox.addActionListener(e -> {
            if (sinceFlag) {
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            serviceIntefacePath.setText(Modules.getPackagePath(serviceInteCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getServicePath(), "service")));
        });
        serviceImplComBox.addActionListener(e -> {
            if (sinceFlag) {
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            serviceImpPath.setText(Modules.getPackagePath(serviceImplComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getImplPath(), "impl")));
        });
        mapperComBox.addActionListener(e -> {
            if (sinceFlag) {
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            mapperPackagePath.setText(Modules.getPackagePath(mapperComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getMapperPath(), "mapper")));
        });
        xmlComBox.addActionListener(e -> {
            if (sinceFlag) {
                sinceFlag = false;
                return;
            }
            MybatisFlexConfig configData = getConfigData();
            mapperXmlPath.setText(Modules.getPackagePath(xmlComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getXmlPath(), "mappers")));
        });
        button1.addActionListener(e -> {
            CustomMappingDialog dialog = new CustomMappingDialog();
            dialog.show();
            tableInfoMap = TableUtils.getAllTables(actionEvent)
                    .stream()
                    .collect(Collectors.toMap(TableInfo::getName, Function.identity()));
            setSelectTalbe(actionEvent);
        });
    }

    private static Set<String> search(String tableName, TableListCellRenderer cellRenderer) {
        Map<String, String> highlightKey = InvertedIndexSearch.highlightKey(tableName);
        cellRenderer.setSearchTableName(tableName);
        cellRenderer.setHighlightKey(highlightKey);
        return highlightKey.keySet();
    }

    /**
     * 设置选中的表
     *
     * @param event 事件
     */
    public void setSelectTalbe(AnActionEvent event) {
        List<String> selectedTableName = TableUtils.getSelectedTableName(event);
        for (String tableName : selectedTableName) {
            int idx = tableNameList.indexOf(tableName);
            tableList.addSelectionInterval(idx, idx);
        }
    }

    public void initSinceComBox(Integer idx) {
        Set<String> list = MybatisFlexPluginConfigData.getSinceMap().keySet();
        sinceComBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (SINCE_CONFIG_ADD.equals(value.toString())) {
                    setIcon(AllIcons.General.Add);
                }else {
                    setIcon(null); // 清除图标
                }
                setText(value.toString());
                return this;
            }
        });
        sinceComBox.removeAllItems();
        sinceComBox.addItem(SINCE_CONFIG);
        for (String item : list) {
            sinceComBox.insertItemAt(item, 1);
        }
        sinceComBox.addItem(SINCE_CONFIG_ADD);
        if (ObjectUtil.isNull(idx)) {
            sinceComBox.setSelectedIndex(sinceComBox.getItemCount() > 2 ? 1 : 0);
        } else {
            sinceComBox.setSelectedIndex(idx);
        }
        sinceComBox.revalidate();
        sinceComBox.repaint();
    }

    private void initPackagePath() {
        int idx = sinceComBox.getSelectedIndex();
        if (idx == 0) {
            MybatisFlexConfig configData = getConfigData();
            controllerPath.setText(Modules.getPackagePath(cotrollerCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getContrPath(), "controller")));
            modelPackagePath.setText(Modules.getPackagePath(modelCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getDomainPath(), "domain")));
            serviceIntefacePath.setText(Modules.getPackagePath(serviceInteCombox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getServicePath(), "service")));
            serviceImpPath.setText(Modules.getPackagePath(serviceImplComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getImplPath(), "impl")));
            mapperPackagePath.setText(Modules.getPackagePath(mapperComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getMapperPath(), "mapper")));
            mapperXmlPath.setText(Modules.getPackagePath(xmlComBox.getSelectedItem().toString(), ObjectUtil.defaultIfNull(configData.getXmlPath(), "mappers")));
        }
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
        mapperBtn.addActionListener(e -> mapperPackagePath.setText(Package.selectPackage(Modules.getModule(mapperComBox.getSelectedItem().toString()), mapperPackagePath.getText())));
        modelBtn.addActionListener(e -> modelPackagePath.setText(Package.selectPackage(Modules.getModule(modelCombox.getSelectedItem().toString()), modelPackagePath.getText())));
        serviceInterfaceBtn.addActionListener(e -> {
            String packagePath = Package.selectPackage(Modules.getModule(serviceInteCombox.getSelectedItem().toString()), serviceIntefacePath.getText());
            serviceIntefacePath.setText(packagePath);
            serviceImpPath.setText(packagePath + ".impl");
        });
        serviceImplBtn.addActionListener(e -> {
            String packagePath = Package.selectPackage(Modules.getModule(serviceImplComBox.getSelectedItem().toString()), serviceImpPath.getText());
            serviceImpPath.setText(packagePath);
            serviceIntefacePath.setText(packagePath.substring(0, packagePath.lastIndexOf(".")));
        });
        controllerBtn.addActionListener(e -> {
            String path = Package.selectPackage(Modules.getModule(cotrollerCombox.getSelectedItem().toString()), controllerPath.getText());
            controllerPath.setText(path);
        });
        mapperXmlBtn.addActionListener(e -> {
            mapperXmlPath.setText(Package.selectPackageResources(Modules.getModule(xmlComBox.getSelectedItem().toString()), mapperXmlPath.getText()));
        });
    }


    /**
     * 生成按钮事件
     */
    private void onGenerate() {
        List<String> selectedTabeList = tableList.getSelectedValuesList();
        if (CollUtil.isEmpty(selectedTabeList)) {
            Messages.showWarningDialog("请选择要生成的表", "提示");
            return;
        }
        List<TableInfo> selectedTableInfo = new ArrayList<>();
        for (String tableName : selectedTabeList) {
            selectedTableInfo.add(tableInfoMap.get(tableName));
        }
        boolean flag = checkTableInfo(selectedTableInfo);
        if (flag) {
            String since = sinceComBox.getSelectedItem().toString();
            if (!SINCE_CONFIG.equals(since)) {
                MybatisFlexConfig configData = getConfigData();
                MybatisFlexPluginConfigData.removeSinceConfig(since);
                MybatisFlexPluginConfigData.configSince(since, configData);
            }

            startGenCode(selectedTableInfo);
        }
    }

    private void startGenCode(List<TableInfo> selectedTableInfo) {
        progressBar.setMaximum(selectedTableInfo.size());
        RenderMybatisFlexTemplate.assembleData(selectedTableInfo, getConfigData(), actionEvent.getProject());
        NotificationUtils.notifySuccess("代码生成成功", project);
        onCancel();
    }

    private boolean checkTableInfo(List<TableInfo> selectedTableInfo) {
        List<TableInfo> confirmTableList = selectedTableInfo.stream()
                .filter(el -> {
                    Optional<ColumnInfo> columnInfo = el.getColumnList()
                            .stream()
                            .filter(els -> "Object".equalsIgnoreCase(els.getFieldType()))
                            .findAny();
                    return columnInfo.isPresent();
                })
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(confirmTableList)) {
            ConfirmTableInfoDialog dialog = new ConfirmTableInfoDialog(confirmTableList, () -> {
                startGenCode(selectedTableInfo);
            });
            dialog.show();
        }
        return CollUtil.isEmpty(confirmTableList);
    }


    /**
     * 取消按钮事件
     */
    private void onCancel() {
        InvertedIndexSearch.clear();
        VirtualFileUtils.clearPsiDirectoryMap();
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
        } else {
            cotrollerCombox.setSelectedIndex(0);
        }
        serviceIntefacePath.setText(config.getInterfacePackage());
        String interfaceModule = config.getInterfaceModule();
        if (StrUtil.isNotEmpty(interfaceModule)) {
            serviceInteCombox.setSelectedItem(interfaceModule);
        } else {
            serviceInteCombox.setSelectedIndex(0);
        }
        serviceImpPath.setText(config.getImplPackage());
        String implModule = config.getImplModule();
        if (StrUtil.isNotEmpty(implModule)) {
            serviceImplComBox.setSelectedItem(implModule);
        } else {
            serviceImplComBox.setSelectedIndex(0);
        }
        modelPackagePath.setText(config.getModelPackage());
        String modelModule = config.getModelModule();
        if (StrUtil.isNotEmpty(modelModule)) {
            modelCombox.setSelectedItem(modelModule);
        } else {
            modelCombox.setSelectedIndex(0);
        }

        mapperPackagePath.setText(config.getMapperPackage());
        String mapperModule = config.getMapperModule();
        if (StrUtil.isNotEmpty(mapperModule)) {
            mapperComBox.setSelectedItem(mapperModule);
        } else {
            mapperComBox.setSelectedIndex(0);
        }
        mapperXmlPath.setText(config.getXmlPackage());
        String xmlModule = config.getXmlModule();
        if (StrUtil.isNotEmpty(xmlModule)) {
            xmlComBox.setSelectedItem(xmlModule);
        } else {
            xmlComBox.setSelectedIndex(0);
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
