package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.RenderMybatisFlexTemplate;
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



    }


    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }
}
