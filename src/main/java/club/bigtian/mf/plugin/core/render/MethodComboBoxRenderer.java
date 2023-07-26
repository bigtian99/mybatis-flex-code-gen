package club.bigtian.mf.plugin.core.render;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MethodComboBoxRenderer extends JLabel implements ListCellRenderer {
    private JLabel rowEndLabel;
    private JLabel label;

    public MethodComboBoxRenderer() {
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