package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.NotificationUtils;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.entity.MatchTypeMapping;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Collectors;

public class CustomMappingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton addBtn;
    private JButton removeBtn;
    private JTable table;
    private JButton reset;
    String[] HEADER = {"Match Type", "Column Type", "Java Type"};
    Object[][] TABLE_DATA = {
            {"Match Type", "Column Type", "Java Type"}
    };
    Map<String, List<MatchTypeMapping>> typeMapping;

    public CustomMappingDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("自定义类型映射");
        setSize(1000, 600);
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
        typeMapping = MybatisFlexPluginConfigData.getTypeMapping();
        TABLE_DATA = new Object[typeMapping.size() > 0 ? typeMapping.size() : 0][];

        if (typeMapping.size() == 0) {
            // TABLE_DATA[0] = new Object[]{"", ""};
        }

        addBtn.addActionListener(e -> {
            typeMapping = getTableData();
            typeMapping.computeIfAbsent("REGEX", k -> new ArrayList<>()).add(new MatchTypeMapping("REGEX", "", ""));
            initTableData();
            table.setModel(getDataModel());
            setColumnInput();
        });
        removeBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            TableModel model = table.getModel();
            if (selectedRow == -1 || ObjectUtil.isNull(model)) {
                return;
            }
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            String type = model.getValueAt(selectedRow, 0).toString();
            String valueAt = model.getValueAt(selectedRow, 1).toString();
            typeMapping = getTableData();
            typeMapping.get(type).removeIf(mapping -> mapping.getColumType().equals(valueAt));
            initTableData();
        });
        initTableData();

        reset.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定重置吗？", "提示", AllIcons.General.QuestionDialog);
            if (flag == 0) {
                MybatisFlexPluginConfigData.setTypeMapping(new HashMap<>());
                NotificationUtils.notifySuccess("重置成功");
                typeMapping = MybatisFlexPluginConfigData.getTypeMapping();
                initTableData();
            }
        });
    }

    private void initTableData() {
        List<MatchTypeMapping> list = typeMapping.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        TABLE_DATA = new Object[list.size() > 0 ? list.size() : 1][];
        int idx = 0;
        for (MatchTypeMapping mapping : list) {
            TABLE_DATA[idx] = new Object[]{mapping.getType(), mapping.getColumType(), mapping.getJavaField()};
            idx++;
        }

        table.setModel(getDataModel());
        setColumnInput();
    }


    private void setColumnInput() {
        TableColumn comboBoxColumn = table.getColumnModel().getColumn(2);
        TableColumn type = table.getColumnModel().getColumn(0);

        ExtendableTextField textField = new ExtendableTextField();
        ExtendableTextComponent.Extension browseExtension =
                ExtendableTextComponent.Extension.create(AllIcons.Actions.Find, AllIcons.Actions.Find,
                        "选择java类型", () -> {
                            TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(ProjectUtils.getCurrentProject());
                            TreeClassChooser chooser = chooserFactory.createAllProjectScopeChooser("选择类");
                            chooser.showDialog();
                            PsiClass selected = chooser.getSelected();
                            if (ObjectUtil.isNull(selected)) {
                                return;
                            }
                            String qualifiedName = selected.getQualifiedName();
                            textField.setText(qualifiedName);
                            // 重新渲染 table 需要重新设置事件
                            setColumnInput();
                        });
        textField.addExtension(browseExtension);
        comboBoxColumn.setCellEditor(new DefaultCellEditor(textField));
        JComboBox<Object> box = new JComboBox<>();
        box.addItem("REGEX");
        box.addItem("ORDINARY");
        type.setCellEditor(new DefaultCellEditor(box));

    }

    @NotNull
    private DefaultTableModel getDataModel() {
        return new DefaultTableModel(TABLE_DATA, HEADER) {
            boolean[] canEdit = {true, true, true};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }

    private void onOK() {
        MybatisFlexPluginConfigData.setTypeMapping(getTableData());
        // add your code here
        Messages.showInfoMessage("保存成功", "提示");
        dispose();
    }

    private Map<String, List<MatchTypeMapping>> getTableData() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TableModel model = table.getModel();
        int rowCount = model.getRowCount();
        Map<String, List<MatchTypeMapping>> typeMappingMap = new HashMap<>();
        try {
            for (int row = 0; row < rowCount; row++) {
                String matchType = model.getValueAt(row, 0).toString();
                String column = model.getValueAt(row, 1).toString().toLowerCase();
                String javaField = model.getValueAt(row, 2).toString();
                MatchTypeMapping mapping = new MatchTypeMapping(matchType, javaField, column);
                // typeMappingMap.put(column, javaField);
                typeMappingMap.computeIfAbsent(matchType, k -> new ArrayList<>()).add(mapping);
            }
        } catch (Exception e) {

        }
        return typeMappingMap;
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
