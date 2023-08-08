package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SupportDialog extends JDialog {
    private JPanel contentPane;
    private JLabel alipayLabel;
    private JLabel wechatLabel;

    public SupportDialog() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("感谢大佬赞赏！");
        setMinimumSize(new Dimension(600, 500));
        DialogUtil.centerShow(this);

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


    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private void createUIComponents() {
        contentPane = new JPanel();
        ImageIcon alipayIcon = new ImageIcon(this.getClass().getResource("/alipay.png"));
        double scale = 400d / alipayIcon.getIconHeight();
        alipayIcon.setImage(alipayIcon.getImage().getScaledInstance((int) (alipayIcon.getIconWidth() * scale), 400, Image.SCALE_DEFAULT));
        alipayLabel = new JLabel(alipayIcon);
        alipayLabel.setVisible(true);

        ImageIcon wechatIcon = new ImageIcon(this.getClass().getResource("/wechat.png"));
        scale = 400d / wechatIcon.getIconHeight();
        wechatIcon.setImage(wechatIcon.getImage().getScaledInstance((int) (wechatIcon.getIconWidth() * scale), 400, Image.SCALE_DEFAULT));
        wechatLabel = new JLabel(wechatIcon);
        wechatLabel.setVisible(true);
        // TODO: place custom component creation code here
    }
}
