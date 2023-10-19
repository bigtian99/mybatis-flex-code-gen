package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.ui.components.fields.ExpandableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class CommonSettingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table;
    /**
     * 创建一个表头
     */
    String[] HEADER = {"字段名", "值"};
    Map<String, String> fieldMap = new HashMap() {{
        put("逻辑删除", "logicDeleteField");
        put("多租户", "tenant");
        put("乐观锁", "version");
        put("数据源", "dataSource");
        put("InsertValue", "insertValue");
        put("UpdateValue", "updateValue");
        put("父类", "modelSuperClass");
        put("onInsert", "onInsert");
        put("onUpdate", "onUpdate");
        put("onSet", "onSet");

    }};
    /**
     * 创建数据便于演示
     */
    Object[][] TABLE_DATA = new Object[][]{
            new Object[]{"逻辑删除", ""},
            new Object[]{"多租户", ""},
            new Object[]{"乐观锁", ""},
            new Object[]{"数据源", ""},
            new Object[]{"InsertValue", ""},
            new Object[]{"UpdateValue", ""},
            new Object[]{"父类", ""},
            new Object[]{"onInsert", ""},
            new Object[]{"onUpdate", ""},
            new Object[]{"onSet", ""},

    };
    MybatisFlexConfig config;

    public CommonSettingDialog() {
        config = Template.getMybatisFlexConfig();
        if (ObjectUtil.isNotNull(config)) {
            TABLE_DATA[0][1] = config.getLogicDeleteField();
            TABLE_DATA[1][1] = config.getTenant();
            TABLE_DATA[2][1] = config.getVersion();
            TABLE_DATA[3][1] = config.getDataSource();
            TABLE_DATA[4][1] = config.getInsertValue();
            TABLE_DATA[5][1] = config.getUpdateValue();
            TABLE_DATA[6][1] = config.getModelSuperClass();
            TABLE_DATA[7][1] = config.getOnInsert();
            TABLE_DATA[8][1] = config.getOnUpdate();
            TABLE_DATA[9][1] = config.getOnSet();

        }
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(700, 600);
        DialogUtil.centerShow(this);
        setTitle("实体类设置");
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
        table.setModel(getDataModel());
        setColumnInput();
    }

    @NotNull
    private DefaultTableModel getDataModel() {
        return new DefaultTableModel(TABLE_DATA, HEADER) {
            boolean[] canEdit = {false, true};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }

    private void setColumnInput() {
        TableColumn comboBoxColumn = table.getColumnModel().getColumn(1);
        ExpandableTextField textField = new ExpandableTextField();
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 6 || StrUtil.isNotBlank(textField.getText())) {
                    return;
                }
                TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(ProjectUtils.getCurrentProject());
                TreeClassChooser chooser = chooserFactory.createAllProjectScopeChooser("选择类");
                chooser.showDialog();
                PsiClass selected = chooser.getSelected();
                if (ObjectUtil.isNull(selected)) {
                    return;
                }
                String qualifiedName = selected.getQualifiedName();
                textField.setText(qualifiedName);
            }
        });
        comboBoxColumn.setCellEditor(new DefaultCellEditor(textField));
    }

    private void onOK() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < table.getRowCount(); i++) {
            String key = table.getValueAt(i, 0).toString();
            String filed = fieldMap.get(key);
            ReflectUtil.setFieldValue(config, filed, table.getValueAt(i, 1));
        }
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(config);
        Messages.showInfoMessage("保存成功", "提示");
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
