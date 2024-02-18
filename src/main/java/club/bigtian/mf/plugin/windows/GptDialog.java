package club.bigtian.mf.plugin.windows;

import javax.swing.*;

public class GptDialog extends JDialog {
    private JPanel contentPane;
    private com.intellij.ui.components.fields.ExpandableTextField question;
    private JButton buttonOK;

    public GptDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
       question.getEmptyText().setText("Your placeholder text");

    }

    public static void main(String[] args) {
        GptDialog dialog = new GptDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
