package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.DialogUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.*;

public class CustomTabDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private com.intellij.openapi.ui.TextFieldWithBrowseButton genPath;
    private JTextField title;
    private JTextField fileSuffix;

    public CustomTabDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("自定义Tab栏");
        setSize(500, 250);

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
        FileChooserDescriptor chooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        genPath.addBrowseFolderListener("选择生成路径", "选择生成路径", null, chooserDescriptor);


        title.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });
        fileSuffix.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });
        genPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });
    }

    private void isEnableButtonOk() {
        String titleText = title.getText();
        String pathText = genPath.getText();
        String fileSuffixText = fileSuffix.getText();
        if (StrUtil.isNotEmpty(titleText) && StrUtil.isNotEmpty(pathText) && StrUtil.isNotEmpty(fileSuffixText)) {
            buttonOK.setEnabled(true);
            return;
        }
        buttonOK.setEnabled(false);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        title.setText("");
        genPath.setText("");
        fileSuffix.setText("");

        // add your code here if necessary
        dispose();
    }

    public String getGenPath() {
        return genPath.getText();
    }

    @Override
    public String getTitle() {
        return title.getText();
    }

    public String getFileSuffix() {
        return fileSuffix.getText();
    }

    public CustomTabDialog(String genPaths, String titles, String fileSuffixs) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("自定义Tab栏");
        setSize(500, 250);

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
        FileChooserDescriptor chooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        genPath.addBrowseFolderListener("选择生成路径", "选择生成路径", null, chooserDescriptor);

        title.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });
        fileSuffix.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });
        genPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                isEnableButtonOk();
            }
        });

        this.genPath.setText(genPaths);
        this.title.setText(titles);
        this.fileSuffix.setText(fileSuffixs);
    }
}
