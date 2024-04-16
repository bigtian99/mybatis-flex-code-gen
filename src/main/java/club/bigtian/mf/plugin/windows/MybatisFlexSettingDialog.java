package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.RenderMybatisFlexTemplate;
import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.function.SimpleFunction;
import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.entity.TabInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.components.fields.ExpandableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MybatisFlexSettingDialog extends JDialog {
    public static Key<Boolean> flexTemplate = Key.create("flexTemplate");

    private JPanel contentPane;
    private List defaultTempList = Arrays.asList("Controller", "Entity", "Service", "ServiceImpl", "Mapper", "Xml");
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel mainPanel;
    private ExpandableTextField tablePrefix;
    private Editor templateEditor;
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
    private JButton restBtn;
    private JCheckBox swagger3CheckBox;
    private FixedSizeButton returnBtn;
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

    private FixedSizeButton buttonFixedSizeButton;
    private JCheckBox fromCheckBox;
    private JComboBox sqlDialect;
    private JComboBox mapperXmlType;
    private JTextField interfacePre;
    private FixedSizeButton addTab;
    private FixedSizeButton updateTab;
    private FixedSizeButton tabDelete;
    private JCheckBox enableDebug;
    private JButton saveBtn;
    private JCheckBox ktFile;
    private JButton remoteBtn;
    private JList list1;
    private JPanel listHeader;
    private JPanel edtiorPanel;
    private JCheckBox logCheck;
    private Project project;

    // 是否开启内部模式
    public static boolean insideSchemaFlag = false;
    SimpleFunction simpleFunction;
    List<JTextField> list = Arrays.asList(contrPath, servicePath, implPath, domainPath, xmlPath, mapperPath);
    Map<String, String> pathMap;
    Map<String, TabInfo> tabMap;

    List<TableInfo> selectedTableInfo;

    boolean isPreviewCode = false;

    public MybatisFlexSettingDialog(Project project, List<TableInfo> selectedTableInfo, SimpleFunction simpleFunction) {
        this.project = project;
        this.simpleFunction = simpleFunction;
        setContentPane(contentPane);
        setModal(true);
        this.selectedTableInfo = selectedTableInfo;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.getWidth() * 0.8, screenSize.getHeight() * 0.7);
        setSize(screenSize);
        setMinimumSize(new Dimension(700, 500));
        getRootPane().setDefaultButton(buttonOK);

        listHeader.setPreferredSize(new Dimension(50, 600));
        // 添加到顶部
        ActionToolbar actionToolbar = toolBar();
        actionToolbar.setTargetComponent(listHeader);
        listHeader.add(actionToolbar.getComponent(), BorderLayout.NORTH);
        DialogUtil.centerShow(this);
        insideSchema.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickCount = e.getClickCount();
                if (clickCount != 2) {
                    return;
                }
                insideSchemaFlag = !insideSchemaFlag;
                Messages.showInfoMessage(insideSchemaFlag ? "内部模式已开启" : "内部模式已关闭", "提示");
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


        saveBtn.addActionListener(e -> onOK());
        remoteBtn.addActionListener(e -> {
            RemoteDataConfigDialog dialog = new RemoteDataConfigDialog();
            dialog.setVisible(true);
        });
    }


    public void init() {

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
        ktFile.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.KT_FILE, false));
        initDialectComBox();
        String dialectChinese = MybatisFlexUtil.getDialectChinese(Template.getConfigData(MybatisFlexConstant.SQL_DIALECT, "MYSQL"));
        sqlDialect.setSelectedItem(dialectChinese);
        enableDebug.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.ENABLE_DEBUG, true));
        logCheck.setSelected(Template.getCheckBoxConfig(MybatisFlexConstant.ENABLE_LOG, true));
        initSinceComBox();
        pathMap = new HashMap<>();
        for (JTextField textField : list) {
            pathMap.put(textField.getName(), textField.getText());
        }
        templateList();
    }

    private void templateList() {
        List<TabInfo> infoList = getTabInfos();
        tabMap = infoList.stream().collect(Collectors.toMap(TabInfo::getTitle, Function.identity()));
        DefaultListModel model = new DefaultListModel();
        model.addAll(infoList.stream().map(TabInfo::getTitle).toList());
        list1.setModel(model);
        list1.setSelectedIndex(0);
        Document document = templateEditor.getDocument();
        list1.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || ObjectUtil.isNull(list1.getSelectedValue())) {
                return;
            }
            document.setReadOnly(false);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                TabInfo info = tabMap.get(list1.getSelectedValue().toString());
                String fileName = StrUtil.format("{}{}{}", info.getTitle(), info.getSuffix(), ".vm");
                ((EditorEx) templateEditor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileName));
                document.setText(info.getContent());
                document.setReadOnly(isPreviewCode);
            });
        });
    }

    private static @NotNull List<TabInfo> getTabInfos() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        return config.getTabList();
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


    public Editor createEditorWithText(String text, String fileSuffix) {
        Project project = ProjectUtils.getCurrentProject();
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile psiFile = psiFileFactory.createFileFromText(PlainTextLanguage.INSTANCE, text);
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        // 获取EditorFactory实例
        EditorFactory editorFactory = EditorFactory.getInstance();
        // // 创建一个Document实例
        // 创建一个Editor实例
        Editor editor = editorFactory.createEditor(document, project);
        // 设置Editor的一些属性
        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setVirtualSpace(false);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setLineNumbersShown(true);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setGutterIconsShown(true);
        ((EditorEx) editor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, StrUtil.format("demo{}.vm", fileSuffix)));
        editor.putUserData(flexTemplate, true);
        return editor;
    }


    public MybatisFlexConfig getConfigData() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();

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
        config.setEnableDebug(enableDebug.isSelected());
        config.setKtFile(ktFile.isSelected());
        config.setEnableLog(logCheck.isSelected());
        List<TabInfo> tabList = tabMap.values().stream().sorted(Comparator.comparingInt(TabInfo::getSort)).collect(Collectors.toList());
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

    private void checkDefaultTab() {
        if (defaultTempList.contains(list1.getSelectedValue().toString())) {
            Messages.showWarningDialog("默认模板不能额外操作", "提示");
            Assert.isTrue(false, "默认模板不能额外操作");
        }
    }

    private void createUIComponents() {
        edtiorPanel = new JPanel();
        edtiorPanel.setLayout(new GridLayout(1, 1));
        edtiorPanel.setPreferredSize(new Dimension(800, 600));
        List<TabInfo> tabInfos = getTabInfos();
        templateEditor = createEditorWithText(tabInfos.get(0).getContent(), ".java");
        edtiorPanel.add(templateEditor.getComponent());
        // templateList();
        templateEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                String title = list1.getSelectedValue().toString();
                TabInfo tabInfo = tabMap.get(title);
                tabInfo.setContent(event.getDocument().getText());
            }
        });
    }

    private ActionToolbar toolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // 预览
        actionGroup.add(createPreviewAction());
        // 新增操作
        actionGroup.add(createAddAction());
        // 向上移动
        actionGroup.add(createMoveUpAction());
        // 向下移动
        actionGroup.add(createMoveDownAction());
        // 编辑模板
        actionGroup.add(createEditorAction());
        // 重置模板
        actionGroup.add(createResetAction());
        // 删除动作
        actionGroup.add(createRemoveAction());
        return ActionManager.getInstance().createActionToolbar("Item Toolbar", actionGroup, true);

    }

    private AnAction createPreviewAction() {
        return new AnAction("预览", "预览", AllIcons.Actions.ShowCode) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                MybatisFlexConfig configData = getConfigData();
                Document document = templateEditor.getDocument();
                document.setReadOnly(false);
                if (!isPreviewCode) {
                    if (configData.isRemoteInterface()) {
                        try {
                            tabMap = RenderMybatisFlexTemplate.remoteDataPreview(selectedTableInfo.get(0).getName());
                        } catch (Exception ex) {
                        }
                    } else {
                        tabMap = RenderMybatisFlexTemplate.remoteLocalPreview(selectedTableInfo.get(0));
                    }
                    String value = list1.getSelectedValue().toString();
                    TabInfo tabInfo = tabMap.get(value);
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        templateEditor.getDocument().setText(tabInfo.getContent());
                        EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, getFileTypeByExtension(tabInfo.getSuffix().replace(".", "")));
                        ((EditorEx) templateEditor).setHighlighter(highlighter);
                    });
                    list1.setSelectedIndex(list1.getSelectedIndex());
                    isPreviewCode = true;
                    document.setReadOnly(true);

                } else {
                    isPreviewCode = false;
                    tabMap = getTabInfos().stream().collect(Collectors.toMap(TabInfo::getTitle, Function.identity()));
                    String value = list1.getSelectedValue().toString();
                    TabInfo tabInfo = tabMap.get(value);
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        document.setText(tabInfo.getContent());
                        ((EditorEx) templateEditor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, StrUtil.format("demo{}.vm", tabInfo.getSuffix())));

                    });
                    list1.setSelectedIndex(list1.getSelectedIndex());

                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    public static FileType getFileTypeByExtension(String extension) {
        return FileTypeManager.getInstance().getFileTypeByExtension(extension);
    }

    private AnAction createEditorAction() {
        return new AnAction("编辑", "编辑", AllIcons.Actions.EditScheme) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                checkDefaultTab();
                int selectedIndex = list1.getSelectedIndex();
                String title = list1.getSelectedValue().toString();
                TabInfo el = tabMap.get(title);

                CustomTabDialog dialog = new CustomTabDialog(el.getGenPath(), el.getTitle(), el.getSuffix());
                dialog.show();
                String genPath = dialog.getGenPath();
                if (StrUtil.isEmpty(genPath)) {
                    return;
                }
                el.setSuffix(dialog.getFileSuffix());
                el.setTitle(dialog.getTitle());
                el.setGenPath(dialog.getGenPath());
                tabMap.put(el.getTitle(), el);
                resetList(selectedIndex);
            }
        };
    }

    private void resetList(int selectedIndex) {
        DefaultListModel<String> model = new DefaultListModel<>();
        List<String> titleList = tabMap.values().stream().sorted(Comparator.comparingInt(TabInfo::getSort)).map(TabInfo::getTitle).collect(Collectors.toList());
        model.addAll(titleList);
        list1.setModel(model);
        list1.setSelectedIndex(selectedIndex);
    }


/*
    private AnAction createCopyAction() {
        return new AnAction(AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }

            @Override
            public void update(@NotNull AnActionEvent e) {
            }
        };
    }
*/

    private AnAction createAddAction() {
        return new AnAction("新增", "新增", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                CustomTabDialog dialog = new CustomTabDialog();
                dialog.setVisible(true);
                String genPath = dialog.getGenPath();
                String title = dialog.getTitle();
                String fileSuffix = dialog.getFileSuffix();
                if (StrUtil.isEmpty(title) || StrUtil.isEmpty(genPath) || StrUtil.isEmpty(fileSuffix)) {
                    return;
                }
                TabInfo tabInfo = new TabInfo(title, "", genPath, fileSuffix, tabMap.size());
                tabMap.put(title, tabInfo);
                resetList(tabMap.size() - 1);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    private AnAction createRemoveAction() {
        return new AnAction("删除", "删除", AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                checkDefaultTab();
                int flag = Messages.showYesNoDialog("确定要删除该模板吗？", "提示", Messages.getQuestionIcon());
                if (0 == flag) {
                    String title = list1.getSelectedValue().toString();
                    tabMap.remove(title);
                    resetList(0);
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    private AnAction createResetAction() {
        return new AnAction("重置", "重置", AllIcons.General.Reset) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int flag = Messages.showYesNoDialog("确定要恢复自带模板吗？", "提示", Messages.getQuestionIcon());
                if (0 == flag) {
                    MybatisFlexPluginConfigData.clearCode();
                    tabMap.clear();
                    MybatisFlexConfig configData = getConfigData();
                    configData.setTabList(getTabInfos());
                    MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(configData);
                    int selectedIndex = list1.getSelectedIndex();
                    String templateName = list1.getSelectedValue().toString();
                    templateList();
                    resetList(selectedIndex);
                    Messages.showInfoMessage("恢复成功", "提示");
                    templateEditor.getDocument().setReadOnly(false);
                    String suffix = tabMap.get(templateName).getSuffix();
                    isPreviewCode=false;
                    ((EditorEx) templateEditor).setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, StrUtil.format("demo{}.vm",suffix)));

                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {

            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    private AnAction createMoveUpAction() {
        return new AnAction("向上", "向上", Icons.MOVE_UP) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 动作执行的逻辑
                String title = list1.getSelectedValue().toString();
                TabInfo tabInfo = tabMap.get(title);
                tabInfo.setSort(list1.getSelectedIndex() - 1);
                sortIncr(+1, title, tabInfo.getSort());
                resetList(tabInfo.getSort());
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                int idx = list1.getSelectedIndex();
                e.getPresentation().setEnabled(idx > 0);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    private AnAction createMoveDownAction() {
        AnAction action = new AnAction("向下", "向下", Icons.MOVE_DOWN) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 动作执行的逻辑
                String title = list1.getSelectedValue().toString();
                TabInfo tabInfo = tabMap.get(title);
                tabInfo.setSort(list1.getSelectedIndex() + 1);
                sortIncr(-1, title, tabInfo.getSort());
                resetList(tabInfo.getSort());
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                // 更新动作状态的逻辑
                int idx = list1.getSelectedIndex();
                e.getPresentation().setEnabled(idx < tabMap.size() - 1);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        // 设置悬浮提示文字
        return action;
    }

    private void sortIncr(int step, String title, int sort) {
        tabMap.values()
                .stream()
                .filter(el -> !el.getTitle().equals(title) && el.getSort() == sort)
                .forEach(el -> el.setSort(el.getSort() + step));

    }


}
