package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.MybatisFlexUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

public class SqlToCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField variable;
    private JTextArea sql;

    private AnActionEvent event;

    public SqlToCodeDialog(AnActionEvent event) {
        this.event = event;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("SQL转 flex 代码（Beta）");
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

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        variable.setText(MybatisFlexUtil.getSelectedText(event));
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(250);
                sql.requestFocus();
            } catch (InterruptedException e) {

            }
        }).start();
    }

    private void onOK() {
        createCode();
        dispose();
    }

    private void createCode() {
        String text = sql.getText();
        int line = MybatisFlexUtil.getLine(event);
        Editor editor = MybatisFlexUtil.getEditor(event);
        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            editor.getDocument().insertString(line, text);
        });
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
