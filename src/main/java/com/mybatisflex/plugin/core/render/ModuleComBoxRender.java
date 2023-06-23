package com.mybatisflex.plugin.core.render;

import com.intellij.icons.AllIcons;

import javax.swing.*;
import java.awt.*;

public class ModuleComBoxRender extends JLabel implements ListCellRenderer{
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.toString());
        setIcon(AllIcons.Nodes.Module);
        return this;
    }
}
