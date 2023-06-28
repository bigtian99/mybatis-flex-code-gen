package com.mybatisflex.plugin.core.render;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.icons.AllIcons;

import javax.swing.*;
import java.awt.*;

public class ModuleComBoxRender extends JLabel implements ListCellRenderer<String> {
    @Override
    public Component getListCellRendererComponent(JList list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setIcon(AllIcons.Nodes.Module);
        return this;
    }
}
