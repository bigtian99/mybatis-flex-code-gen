package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.function.SimpleFunction;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfirmTableInfoDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable dataTable;
    private JComboBox tableComBox;
    private JCheckBox syncCheckBox;
    Map<String, TableInfo> tableInfoMap;
    Map<String, List<ColumnInfo>> columnInfoMap;
    SimpleFunction simpleFunction;
    /**
     * 创建一个表头
     */
    String[] HEADER = {"Column Name", "Field Name", "Column Type", "Field Type", "Comment"};

    /**
     * 创建数据便于演示
     */
    Object[][] TABLE_DATA;

    public ConfirmTableInfoDialog(List<TableInfo> list, SimpleFunction simpleFunction) {
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);
        setSize(1000, 600);
        DialogUtil.centerShow(this);
        setTitle("确认表信息");
        tableInfoMap = list.stream()
                .collect(Collectors.toMap(TableInfo::getName, Function.identity()));

        columnInfoMap = list.parallelStream()
                .flatMap(el -> el.getColumnList().stream())
                .filter(el -> Object.class.getSimpleName().equals(el.getFieldType()))
                .collect(Collectors.groupingBy(ColumnInfo::getType));

        this.simpleFunction = simpleFunction;

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

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
        createData(list.get(0));

        addComBoxItem();
        tableComBox.addActionListener((e) -> {
            Object selectedItem = tableComBox.getSelectedItem();
            if (ObjectUtil.isNull(selectedItem)) {
                return;
            }
            String item = selectedItem.toString();
            TableInfo tableInfo = tableInfoMap.get(item);
            createData(tableInfo);
        });
        setColumnInput();
    }

    private void setColumnInput() {
        TableColumn comboBoxColumn = dataTable.getColumnModel().getColumn(3);

        JTextField textField = new JTextField();
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(ProjectUtils.getCurrentProject());
                TreeClassChooser chooser = chooserFactory.createAllProjectScopeChooser("选择类");
                chooser.showDialog();
                PsiClass selected = chooser.getSelected();
                if (ObjectUtil.isNull(selected)) {
                    return;
                }
                String qualifiedName = selected.getQualifiedName();
                textField.setText(qualifiedName);

                tableInfoMap.values().forEach(el -> {
                    el.addImportClassItem(qualifiedName);
                });
                String columnType = dataTable.getValueAt(dataTable.getSelectedRow(), 2).toString().toUpperCase();
                // 为当前类型添加持久化配置，下次同种类型的字段不需要再次选择
                MybatisFlexPluginConfigData.setFieldType(columnType, qualifiedName);
                if (syncCheckBox.isSelected()) {
                    syncTypeVal(columnType, qualifiedName);
                    // 添加导入包
                }
                // 重新渲染 table 需要重新设置事件
                setColumnInput();
            }
        });
        comboBoxColumn.setCellEditor(new DefaultCellEditor(textField));
    }

    private void addComBoxItem() {
        for (String itemName : tableInfoMap.keySet()) {
            tableComBox.addItem(itemName);
        }
        tableComBox.revalidate();
        tableComBox.repaint();
    }

    /**
     * 同步同一个类型数据
     *
     * @param row 行
     */
    public void syncTypeVal(String columnType, String typeName) {

        List<ColumnInfo> columnInfos = columnInfoMap.get(columnType);
        for (ColumnInfo info : columnInfos) {
            info.setFieldType(typeName.substring(typeName.lastIndexOf(".") + 1));
        }
        columnInfoMap.remove(columnType);
        createData(tableInfoMap.get(tableComBox.getSelectedItem().toString()));
        Collection<TableInfo> tableInfos = tableInfoMap.values();

        tableInfoMap = tableInfos.stream()
                .filter(el -> el.getColumnList().stream()
                        .anyMatch(el2 -> Object.class.getSimpleName().equals(el2.getFieldType())))
                .collect(Collectors.toMap(TableInfo::getName, Function.identity()));
        tableComBox.removeAllItems();
        addComBoxItem();

    }

    @NotNull
    private DefaultTableModel getDataModel() {
        return new DefaultTableModel(TABLE_DATA, HEADER) {
            boolean[] canEdit = {false, false, false, true, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }


    private void createData(TableInfo tableInfo) {
        if (ObjectUtil.isNull(tableInfo)) {
            return;
        }
        List<ColumnInfo> columnList = tableInfo.getColumnList().stream()
                .filter(el -> Object.class.getSimpleName().equalsIgnoreCase(el.getFieldType()))
                .collect(Collectors.toList());
        TABLE_DATA = new Object[columnList.size()][];
        for (int i = 0; i < columnList.size(); i++) {
            ColumnInfo columnInfo = columnList.get(i);
            TABLE_DATA[i] = new Object[]{
                    columnInfo.getName(),
                    columnInfo.getFieldName(),
                    columnInfo.getType().toLowerCase(),
                    columnInfo.getFieldType(),
                    columnInfo.getComment()
            };
        }
        dataTable.setModel(getDataModel());
    }


    private void onOK() {
        if (CollUtil.isNotEmpty(columnInfoMap)) {
            int i = Messages.showYesNoDialog(ProjectUtils.getCurrentProject(), "还有类型为 Object 的字段没有修改，确定生成吗？", "警告", Messages.getWarningIcon());
            if (i == Messages.NO) {
                return;
            }
        }
        simpleFunction.apply();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


}

