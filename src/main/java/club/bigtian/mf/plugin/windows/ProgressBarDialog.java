package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.DialogUtil;

import javax.swing.*;

public class ProgressBarDialog extends JDialog {
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JLabel classLabel;

    private String currentClassName;

    public ProgressBarDialog() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("正在生成代码...");
        setSize(500, 200);
        DialogUtil.centerShow(this);
        progressBar.setMaximum(100);

    }

    @Override
    public void show() {
        new Thread(() -> {
            for (int i = 0; i <=100; i++) {
                progressBar.setValue(i);
                classLabel.setText("正在生成" + currentClassName + i + "...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        super.show();
    }


    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }
}
