package club.bigtian.mf.plugin.core.editor;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EventObject;

public class ImmediateEditingCellEditor extends DefaultCellEditor {
    JComboBox comboBox;

    public ImmediateEditingCellEditor(JComboBox<Object> comboBox) {
        super(comboBox);
        this.comboBox = comboBox;
        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                comboBox.showPopup();
            }
        });
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }
}
