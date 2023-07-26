package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.core.util.TreeClassChooser;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ReturnInfoDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField classField;
    private com.intellij.openapi.ui.FixedSizeButton buttonFixedSizeButton;
    private JComboBox methodComBox;
    private JRadioButton newRadio;
    private JRadioButton staticRadio;
    private JCheckBox genericityCheckBox;

    private Map<String, Boolean> staticMethodMap;

    public ReturnInfoDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(500, 250);
        DialogUtil.centerShow(this);
        setTitle("统一返回信息配置");

        staticMethodMap = new HashMap<>();
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
        methodComBox.setRenderer(new CustomComboBoxRenderer());
        methodComBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Boolean flag = staticMethodMap.get(methodComBox.getSelectedItem());
                if (ObjectUtil.defaultIfNull(flag, false)) {
                    staticRadio.setSelected(true);
                    newRadio.setSelected(false);
                } else {
                    newRadio.setSelected(true);
                    staticRadio.setSelected(false);
                }
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
        buttonFixedSizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PsiClass psiClass = TreeClassChooser.treeClassChooser();
                if (ObjectUtil.isNull(psiClass)) {
                    return;
                }
                String qualifiedName = psiClass.getQualifiedName();
                classField.setText(qualifiedName);
                addMethodComBoxItem(psiClass);
            }
        });
        staticRadio.addActionListener(e -> {
            newRadio.setSelected(false);
        });
        newRadio.addActionListener(e -> {
            staticRadio.setSelected(false);
        });
        loadConfig();
    }

    private void addMethodComBoxItem(PsiClass psiClass) {
        methodComBox.removeAllItems();
        Arrays.stream(psiClass.getMethods())
                .filter(el -> !el.getName().startsWith("set")&&!el.getName().startsWith("get"))
                .forEach(method -> {
                    String name = method.getName();
                    StringJoiner joiner = new StringJoiner(",");
                    PsiParameter[] parameters = method.getParameterList().getParameters();
                    if (parameters.length != 1) {
                        return;
                    }
                    for (PsiParameter parameter : parameters) {
                        String canonicalText = parameter.getType().getCanonicalText();
                        int idx = canonicalText.lastIndexOf(".");
                        if (idx > -1) {
                            canonicalText = canonicalText.substring(idx + 1);
                        }
                        joiner.add(canonicalText + " " + parameter.getName());
                    }
                    name = name + "(" + joiner.toString() + ")";
                    methodComBox.addItem(name);
                    boolean flag = method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
                    staticMethodMap.put(name, flag);
                });
        methodComBox.revalidate();
        methodComBox.repaint();
    }

    private void loadConfig() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        String qualifiedName = config.getQualifiedName();
        classField.setText(qualifiedName);
        Project project = ProjectUtils.getCurrentProject();
        if (StrUtil.isNotBlank(qualifiedName)) {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project));
            addMethodComBoxItem(psiClass);
            methodComBox.setSelectedItem(config.getMethodName());
        }
        genericityCheckBox.setSelected(config.isGenericity());
        if (MybatisFlexConstant.STRING.equals(config.getResultType())) {
            newRadio.setSelected(true);
            staticRadio.setSelected(false);
        } else {
            staticRadio.setSelected(true);
            newRadio.setSelected(false);
        }
    }

    private void onOK() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        config.setQualifiedName(classField.getText());
        config.setMethodName(methodComBox.getSelectedItem().toString());
        config.setResultType(staticRadio.isSelected() ? MybatisFlexConstant.STRING : MybatisFlexConstant.NEW);
        config.setGenericity(genericityCheckBox.isSelected());
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(config);
        Messages.showInfoMessage("保存成功", "提示");
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}

class CustomComboBoxRenderer extends JLabel implements ListCellRenderer {
    private JLabel rowEndLabel;
    private JLabel label;

    public CustomComboBoxRenderer() {
        setOpaque(true);
        setLayout(new BorderLayout());
        label = new JLabel();
        label.setPreferredSize(new Dimension(130, label.getHeight()));
        rowEndLabel = new JLabel();
        rowEndLabel.setForeground(Color.GRAY);
        add(label, BorderLayout.WEST);
        add(rowEndLabel, BorderLayout.EAST);
        rowEndLabel.setBorder(new EmptyBorder(0, 0, 0, 50));
        rowEndLabel.setPreferredSize(new Dimension(130, rowEndLabel.getHeight()));
    }


    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (ObjectUtil.isNull(value)) {
            return this;
        }
        String valueString = value.toString();
        label.setText(StrUtil.subBefore(valueString, "(", false));
        rowEndLabel.setText(StrUtil.subBetween(valueString, "(", ")"));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}