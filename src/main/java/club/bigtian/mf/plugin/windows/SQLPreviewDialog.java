package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.BasicFormatter;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.NotificationUtils;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

import javax.swing.*;
import java.awt.event.*;

/**
 * sql预览窗口
 */
public class SQLPreviewDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea sqlPreview;
    private static final BasicFormatter FORMATTER = new BasicFormatter();
    private String sql;

    public SQLPreviewDialog(String sql, PsiJavaFile psiFile) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("SQL Preview");
        setSize(500, 500);
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
        this.sql = FORMATTER.format(sql);
        sqlPreview.setText(this.sql);
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

        WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
            PsiClass[] classes = psiFile.getClasses();
            for (PsiClass psiClass : classes) {
                if (psiClass.getName().contains("MybatisFlexSqlPreview")) {
                    psiClass.delete();
                }
            }
        });
    }

    private void onOK() {
        ClipboardUtil.setStr(sql);
        NotificationUtils.notifySuccess("🎉SQL copied to clipboard.🎉", ProjectUtils.getCurrentProject());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
