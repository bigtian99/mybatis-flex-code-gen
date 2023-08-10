package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
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
import java.util.HashMap;
import java.util.Map;

public class CustomMappingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton addBtn;
    private JButton removeBtn;
    private JTable table;
    String[] HEADER = {"Column Type", "Java Type"};
    Object[][] TABLE_DATA;
    Map<String, String> typeMapping;

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
            typeMapping.put("", "");
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
            String valueAt = model.getValueAt(selectedRow, 0).toString();
            typeMapping = getTableData();
            typeMapping.remove(valueAt);
            initTableData();
        });
        initTableData();

    }

    private void initTableData() {
        TABLE_DATA = new Object[typeMapping.size() > 0 ? typeMapping.size() : 1][];
        int idx = 0;
        for (Map.Entry<String, String> entry : typeMapping.entrySet()) {
            TABLE_DATA[idx] = new Object[]{entry.getKey(), entry.getValue()};
            idx++;
        }
        table.setModel(getDataModel());

        setColumnInput();
    }


    private void setColumnInput() {
        TableColumn comboBoxColumn = table.getColumnModel().getColumn(1);

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
    }

    @NotNull
    private DefaultTableModel getDataModel() {
        return new DefaultTableModel(TABLE_DATA, HEADER) {
            boolean[] canEdit = {true, true};

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

    private Map<String, String> getTableData() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TableModel model = table.getModel();
        int rowCount = model.getRowCount();
        Map<String, String> typeMappingMap = new HashMap<>();
        try {
            for (int row = 0; row < rowCount; row++) {
                String column = model.getValueAt(row, 0).toString().toLowerCase();
                String javaField = model.getValueAt(row, 1).toString();
                typeMappingMap.put(column, javaField);
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
