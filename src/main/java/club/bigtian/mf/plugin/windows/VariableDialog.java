package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.contributor.MybatisFlexTemplateCompletionContributor;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.NotificationUtils;
import club.bigtian.mf.plugin.entity.Variable;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class VariableDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton addBtn;
    private JButton removeBtn;
    private JTable table;
    private JButton reset;
    String[] HEADER = {"变量名", "变量值", "描述"};
    Object[][] TABLE_DATA = {
    };
    List<Variable> variableList;

    public VariableDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("自定义模板变量");
        setSize(1000, 600);
        DialogUtil.centerShow(this);
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
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        variableList = MybatisFlexPluginConfigData.getVariable();


        addBtn.addActionListener(e -> {
            variableList=getTableData();
            variableList.add(new Variable("", "", ""));
            initTableData();
            table.setModel(getDataModel());
            setColumnInput();
        });
        removeBtn.addActionListener(e -> {
            variableList=getTableData();
            int selectedRow = table.getSelectedRow();
            TableModel model = table.getModel();
            if (selectedRow == -1 || ObjectUtil.isNull(model)) {
                return;
            }
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            String key = ObjectUtil.defaultIfNull(model.getValueAt(selectedRow, 0), "").toString();
            variableList.remove(table.getSelectedRow());
            if (StrUtil.isNotEmpty(key)) {
                MybatisFlexTemplateCompletionContributor.removeTemplateMap(key);
            }
            initTableData();
        });
        initTableData();

        reset.addActionListener(e -> {
            int flag = Messages.showYesNoDialog("确定重置吗？", "提示", AllIcons.General.QuestionDialog);
            if (flag == 0) {
                MybatisFlexPluginConfigData.setVarible(new ArrayList<>());
                NotificationUtils.notifySuccess("重置成功");
                variableList = MybatisFlexPluginConfigData.getVariable();
                initTableData();
            }
        });
    }

    private void initTableData() {
        TABLE_DATA = new Object[variableList.size()][];
        int idx = 0;
        for (Variable variable : variableList) {

            TABLE_DATA[idx] = new Object[]{variable.getName(), variable.getScript(), variable.getDescription()};
            idx++;
        }

        table.setModel(getDataModel());
        table.repaint();
        setColumnInput();
    }


    private void setColumnInput() {
        TableColumn comboBoxColumn = table.getColumnModel().getColumn(1);

        ExtendableTextField textField = new ExtendableTextField();
        ExtendableTextComponent.Extension browseExtension =
                ExtendableTextComponent.Extension.create(AllIcons.Actions.Edit, AllIcons.Actions.Edit,
                        "编辑", () -> {
                            String template = "def variable(){ return \"hello word\"};\nvariable()";
                            Variable variable = getTableData(table.getSelectedRow());
                            if (StrUtil.isNotEmpty(variable.getScript())) {
                                template = variable.getScript();
                            }
                            EditDialog dialog = new EditDialog(template);
                            dialog.setVisible(true);

                            String script = dialog.getScript();
                            TableModel model = table.getModel();
                            model.setValueAt(script, table.getSelectedRow()  , 1);
                            textField.setText(script);
                            // variableList = getTableData();
                            // initTableData();
                        });
        textField.addExtension(browseExtension);
        comboBoxColumn.setCellEditor(new DefaultCellEditor(textField));


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
        List<Variable> tableData = getTableData();
        for (Variable el : tableData) {
            if (StrUtil.isBlank(el.getName()) || StrUtil.isBlank(el.getScript())) {
                Messages.showErrorDialog("变量名和变量值，都不能为空", "错误");
                return;
            }
        }
        MybatisFlexPluginConfigData.setVarible(tableData);
        // add your code here
        Messages.showInfoMessage("保存成功", "提示");
        dispose();
    }

    private List<Variable> getTableData() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TableModel model = table.getModel();
        int rowCount = model.getRowCount();
        List<Variable> variables = new ArrayList<>();
        try {
            for (int row = 0; row < rowCount; row++) {
                variables.add(getTableData(row));
            }
        } catch (Exception e) {

        }
        return variables;
    }

    private Variable getTableData(int row) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        TableModel model = table.getModel();

        String key = ObjectUtil.defaultIfNull(model.getValueAt(row, 0), "").toString();
        String val = ObjectUtil.defaultIfNull(model.getValueAt(row, 1), "").toString();
        String desc = ObjectUtil.defaultIfNull(model.getValueAt(row, 2), "").toString();
        return new Variable(key, val, desc);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
