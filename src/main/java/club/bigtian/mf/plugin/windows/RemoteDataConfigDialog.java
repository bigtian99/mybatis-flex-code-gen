package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.DialogUtil;

import javax.swing.*;
import java.awt.event.*;

public class RemoteDataConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField url;
    private JTextField headerKey;
    private JTextArea token;
    private JTextField resultField;
    private JCheckBox remoteInterface;

    public RemoteDataConfigDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("远程数据配置");
        setSize(500, 300);
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
        init();
    }

    private void init() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        url.setText(config.getRemoteDataUrl());
        headerKey.setText(config.getRemoteHeader());
        token.setText(config.getRemoteDataToken());
        remoteInterface.setSelected(config.isRemoteInterface());
        resultField.setText(config.getResultField());
    }

    private void onOK() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        config.setRemoteDataToken(token.getText());
        config.setRemoteDataUrl(url.getText());
        config.setRemoteHeader(headerKey.getText());
        config.setResultField(resultField.getText());
        config.setRemoteInterface(remoteInterface.isSelected());
        MybatisFlexPluginConfigData.setCurrentMybatisFlexConfig(config);
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
