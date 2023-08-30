package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.RenderMybatisFlexTemplate;
import club.bigtian.mf.plugin.core.listener.MybatisFlexListener;
import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.entity.TableInfo;
import com.intellij.util.messages.Topic;

import javax.swing.*;

public class ProgressBarDialog extends JDialog {
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JLabel classLabel;

    private String currentClassName;

    public static final Topic<TableInfo> TableInfoTopic =
            Topic.create("TableInfoTopic", TableInfo.class);

    public ProgressBarDialog(int maxNum) {
        int i = 1;
        setContentPane(contentPane);
        setModal(true);
        setTitle("正在生成代码...");
        setSize(500, 200);
        DialogUtil.centerShow(this);
        progressBar.setMaximum(maxNum);
        setModalityType(ModalityType.APPLICATION_MODAL);

        ProjectUtils.getCurrentProject().getMessageBus().connect().subscribe(RenderMybatisFlexTemplate.VFS_CHANGES,
                new MybatisFlexListener() {
                    @Override
                    public void before() {
                        new Thread(() -> {
                            setVisible(true);
                        }).start();
                    }

                    @Override
                    public void after(String fileName) {
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progressBar.getValue() + 1);
                            classLabel.setText(fileName + progressBar.getValue());
                        });

                        // handle the events
                        System.out.println("进来");
                    }
                });

    }


    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }
}
