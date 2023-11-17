package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.function.SimpleFunction;
import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.FileChooserUtil;
import club.bigtian.mf.plugin.core.util.MybatisFlexUtil;
import club.bigtian.mf.plugin.entity.TabInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

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
    private com.intellij.openapi.ui.FixedSizeButton returnBtn;
    private JTextField contrPath;
    private JTextField servicePath;
    private JTextField implPath;
    private JTextField domainPath;
    private JTextField xmlPath;
    private JTextField mapperPath;
    private JCheckBox accessorsCheckBox;
    private JTabbedPane tabbedPane2;
    private JCheckBox activeRecordCheckBox;
    private JLabel insideSchema;
    private JCheckBox requiredArgsConstructorCheckBox;

    private com.intellij.openapi.ui.FixedSizeButton buttonFixedSizeButton;
    private JCheckBox fromCheckBox;
    private JComboBox sqlDialect;
    private JComboBox mapperXmlType;
    private JTextField interfacePre;
    private com.intellij.openapi.ui.FixedSizeButton addTab;
    private FixedSizeButton updateTab;
    private FixedSizeButton tabDelete;
    private Project project;

    // 是否开启内部模式
    public static boolean insideSchemaFlag = false;
    SimpleFunction simpleFunction;
    List<JTextField> list = Arrays.asList(contrPath, servicePath, implPath, domainPath, xmlPath, mapperPath);
    Map<String, String> pathMap;
    List<TabInfo> tabList = new ArrayList<>();

    public MybatisFlexSettingDialog(Project project, SimpleFunction simpleFunction) {
        this.project = project;
        this.simpleFunction = simpleFunction;
        setContentPane(contentPane);
        setModal(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.getWidth() * 0.8, screenSize.getHeight() * 0.7);
        setSize(screenSize);
        setMinimumSize(new Dimension(700, 500));
        getRootPane().setDefaultButton(buttonOK);
        DialogUtil.centerShow(this);
        buttonOK.addActionListener(e -> onOK());
        insideSchema.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TemplatePreviewDialog dialog = new TemplatePreviewDialog();
                // dialog.show();
                int clickCount = e.getClickCount();
                if (clickCount != 2) {
                    return;
                }
                insideSchemaFlag = !insideSchemaFlag;
                Messages.showInfoMessage(insideSchemaFlag ? "内部模式已开启" : "内部模式已关闭", "提示");
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
        buttonFixedSizeButton.addActionListener(e -> new CommonSettingDialog().show());
        addTab.addActionListener(e -> {
            CustomTabDialog dialog = new CustomTabDialog();
            dialog.setVisible(true);
            String genPath = dialog.getGenPath();
            String title = dialog.getTitle();
            String fileSuffix = dialog.getFileSuffix();
            if (StrUtil.isEmpty(title) || StrUtil.isEmpty(genPath) || StrUtil.isEmpty(fileSuffix)) {
                return;
            }
            boolean flag = tabList.stream()
                    .map(TabInfo::getTitle)
                    .anyMatch(el -> el.equals(title));
            if (flag) {
                Messages.showWarningDialog(StrUtil.format("该标题【{}】已经存在,请使用其他名称", title), "提示");
                return;
            }
            tabbedPane1.addTab(title, Icons.DONATE, createTabView(title, "", genPath, fileSuffix, false));
            int idx = tabbedPane1.indexOfTab(title);
            tabbedPane1.setSelectedIndex(idx);
        });
        updateTab.addActionListener(e -> {
            checkDefaultTab();
            int selectedIndex = tabbedPane1.getSelectedIndex();
            String title = tabbedPane1.getTitleAt(selectedIndex);
            tabList.stream()
                    .filter(el -> el.getTitle().equals(title))
                    .findFirst()
                    .ifPresent(el -> {
                        CustomTabDialog dialog = new CustomTabDialog(el.getGenPath(), el.getTitle(), el.getSuffix());
                        dialog.show();
                        String genPath = dialog.getGenPath();
                        if (StrUtil.isEmpty(genPath)) {
                            return;
                        }
                        el.setSuffix(dialog.getFileSuffix());
                        el.setTitle(dialog.getTitle());
                        el.setGenPath(dialog.getGenPath());
                        if (!dialog.getTitle().equals(title)) {
                            tabbedPane1.remove(selectedIndex);
                            tabbedPane1.insertTab(el.getTitle(), Icons.DONATE, createTabView(el.getTitle(),
                                    el.getTextField().getText(),
                                    el.getGenPath(),
                                    el.getSuffix(),
                                    true), "", selectedIndex);
                            tabbedPane1.setSelectedIndex(selectedIndex);
                            el.getTextField().requestFocusInWindow();
                        }
                    });
        });
        tabDelete.addActionListener(e -> {
            checkDefaultTab();
            MessageDialogBuilder.YesNo yes = MessageDialogBuilder.yesNo("提示", "确定要删除吗？");
            int show = yes.show();
            if (show == 1) {
                return;
            }
            int selectedIndex = tabbedPane1.getSelectedIndex();
            String title = tabbedPane1.getTitleAt(selectedIndex);
            tabList.removeIf(el -> el.getTitle().equals(title));
            tabbedPane1.remove(selectedIndex);
        });

        for (int i = 0; i < tabbedPane1.getTabCount(); i++) {
            tabbedPane1.setIconAt(i, Icons.DONATE);
        }
    }

    private void checkDefaultTab() {
        int selectedIndex = tabbedPane1.getSelectedIndex();
        if (selectedIndex <= 5) {
            Messages.showWarningDialog("默认模板不能额外操作", "提示");
            Assert.isTrue(false, "默认模板不能额外操作");
        }
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
        builderCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_BUILDER));
        dataCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_DATA));
        allArgsConstructorCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_ALL_ARGS_CONSTRUCTOR));
        noArgsConstructorCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_NO_ARGS_CONSTRUCTOR));
        swaggerCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.SWAGGER));

        controllerSuffix.setText(Template.getSuffix(MybatisFlexConstant.CONTROLLER_SUFFIX));
        interfaceSuffix.setText(Template.getSuffix(MybatisFlexConstant.INTERFACE_SUFFIX));
        interfacePre.setText(Template.getSuffix(MybatisFlexConstant.INTERFACE_PRE, "I"));
        implSuffix.setText(Template.getSuffix(MybatisFlexConstant.IMPL_SUFFIX));
        modelSuffix.setText(Template.getSuffix(MybatisFlexConstant.MODEL_SUFFIX));
        mapperSuffix.setText(Template.getSuffix(MybatisFlexConstant.MAPPER_SUFFIX));

        cacheCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.CACHE));
        overrideCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.OVERRIDE));
        swagger3CheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.SWAGGER3));

        contrPath.setText(Template.getConfigData(MybatisFlexConstant.CONTR_PATH));
        servicePath.setText(Template.getConfigData(MybatisFlexConstant.SERVICE_PATH));
        implPath.setText(Template.getConfigData(MybatisFlexConstant.IMPL_PATH));
        domainPath.setText(Template.getConfigData(MybatisFlexConstant.DOMAIN_PATH));
        xmlPath.setText(Template.getConfigData(MybatisFlexConstant.XML_PATH));
        mapperPath.setText(Template.getConfigData(MybatisFlexConstant.MAPPER_PATH));

        accessorsCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_ACCESSORS));
        activeRecordCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.ACTIVE_RECORD));
        requiredArgsConstructorCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.LOMBOK_REQUIRED_ARGS_CONSTRUCTOR));
        fromCheckBox.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.FROM, true));
        mapperXmlType.setSelectedItem(Template.getConfigData(MybatisFlexConstant.MAPPER_XML_TYPE, "resource"));
        initDialectComBox();
        String dialectChinese = MybatisFlexUtil.getDialectChinese(Template.getConfigData(MybatisFlexConstant.SQL_DIALECT, "MYSQL"));
        sqlDialect.setSelectedItem(dialectChinese);
        initSinceComBox();
        pathMap = new HashMap<>();
        for (JTextField textField : list) {
            pathMap.put(textField.getName(), textField.getText());
        }
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        List<TabInfo> infoList = config.getTabList();
        if (CollUtil.isNotEmpty(infoList)) {
            for (TabInfo tabInfo : infoList) {
                tabbedPane1.addTab(tabInfo.getTitle(), createTabView(tabInfo.getTitle(), tabInfo.getContent(), tabInfo.getGenPath(), tabInfo.getSuffix(), false));
            }
        }
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

    public void initDialectComBox() {
        MybatisFlexUtil.getTargetClassFieldRemark().forEach(el -> sqlDialect.addItem(el));
        sqlDialect.revalidate();
        sqlDialect.repaint();
    }

    public JPanel createTabView(String title, String content, String genPath, String fileSuffix, boolean isUpdate) {
        JPanel jPanel = new JPanel(new GridLayout());
        JScrollPane pane = new JScrollPane();
        LanguageTextField languageTextField = new LanguageTextField(JavaLanguage.INSTANCE, project, content, false);
        pane.setViewportView(languageTextField);
        jPanel.add(pane);
        languageTextField.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                // 重新渲染JScrollPane，否则没有滚动条，无法滚动
                pane.revalidate();
                pane.repaint();
            }
        });
        if (!isUpdate) {
            TabInfo tabInfo = new TabInfo(title, content, genPath, fileSuffix, languageTextField);
            tabList.add(tabInfo);
        }
        pane.getVerticalScrollBar().setUnitIncrement(10);
        return jPanel;
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
        // scrollPane7 = new JScrollPane();
        // scrollPane8 = new JScrollPane();
        scrollPane1.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane2.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane3.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane4.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane5.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane6.getVerticalScrollBar().setUnitIncrement(10);
        // scrollPane7.getVerticalScrollBar().setUnitIncrement(10);
        // scrollPane8.getVerticalScrollBar().setUnitIncrement(10);
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
        config.setInterfacePre(interfacePre.getText());
        config.setImplSuffix(implSuffix.getText());
        config.setModelSuffix(modelSuffix.getText());
        config.setMapperSuffix(mapperSuffix.getText());

        config.setCache(cacheCheckBox.isSelected());
        config.setSwagger3(swagger3CheckBox.isSelected());
        config.setOverrideCheckBox(overrideCheckBox.isSelected());
        config.setContrPath(contrPath.getText());
        config.setServicePath(servicePath.getText());
        config.setImplPath(implPath.getText());
        config.setDomainPath(domainPath.getText());
        config.setXmlPath(xmlPath.getText());
        config.setMapperPath(mapperPath.getText());
        config.setAccessors(accessorsCheckBox.isSelected());
        config.setActiveRecord(activeRecordCheckBox.isSelected());
        config.setRequiredArgsConstructor(requiredArgsConstructorCheckBox.isSelected());
        config.setFromCheck(fromCheckBox.isSelected());
        config.setSqlDialect(MybatisFlexUtil.getDialectType(sqlDialect.getSelectedItem().toString()));
        config.setMapperXmlType(mapperXmlType.getSelectedItem().toString());
        for (TabInfo tabInfo : tabList) {
            tabInfo.setContent(tabInfo.getTextField().getText());
        }
        config.setTabList(tabList);
        return config;
    }


    private void onOK() {
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(getConfigData());
        Messages.showInfoMessage("保存成功", "提示");
        for (JTextField textField : list) {
            String value = pathMap.get(textField.getName());
            if (value.equals(textField.getText())) {
                continue;
            }
            simpleFunction.apply();
            break;
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
