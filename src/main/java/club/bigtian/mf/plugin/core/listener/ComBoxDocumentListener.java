package club.bigtian.mf.plugin.core.listener;

import club.bigtian.mf.plugin.core.render.ModuleComBoxRender;
import club.bigtian.mf.plugin.core.search.InvertedIndexSearch;
import cn.hutool.core.util.StrUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Map;

public class ComBoxDocumentListener implements DocumentListener {
    ModuleComBoxRender render;
    JTextField textField;
    JComboBox comboBox;

    public ComBoxDocumentListener(JComboBox comboBox) {
        render = (ModuleComBoxRender) comboBox.getRenderer();
        this.textField =(JTextField) comboBox.getEditor().getEditorComponent();
        this.comboBox = comboBox;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        test(e);

    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        test(e);

    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    String preText = "";

    public void test(DocumentEvent event) {
        try {
            SwingUtilities.invokeLater(() -> {
                String text = textField.getText();
                if (StrUtil.equals(text, "\n") || StrUtil.equals(preText, text)) {
                    return;
                }
                Map<String, String> highlightKey = InvertedIndexSearch.highlightKey(text, "module");
                render.setHighlightKey(highlightKey);
                DefaultComboBoxModel boxModel = new DefaultComboBoxModel();
                boxModel.addAll(highlightKey.keySet());
                comboBox.setModel(boxModel);
                if (!highlightKey.containsKey(text)) {
                    comboBox.showPopup();
                }
                textField.setText(text);
                preText = text;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
