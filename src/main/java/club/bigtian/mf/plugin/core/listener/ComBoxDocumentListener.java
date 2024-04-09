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
        this.textField = (JTextField) comboBox.getEditor().getEditorComponent();
        this.comboBox = comboBox;
        // 检测comboBox点击事件
        comboBox.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String text = textField.getText();
                // 获取所有的选项
                Map<String, String> allOptions = InvertedIndexSearch.highlightKey("", "module");
                render.setHighlightKey(allOptions);
                DefaultComboBoxModel boxModel = new DefaultComboBoxModel();
                boxModel.addAll(allOptions.keySet());
                boxModel.setSelectedItem(text);
                comboBox.setModel(boxModel);

                flag = true;
                textField.setText(text.toString());
            });
        });
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        flag = false;
        searchMoudle(e);

    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        flag = false;
        searchMoudle(e);

    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    String preText = "";
    boolean flag = false;

    public void searchMoudle(DocumentEvent event) {
        try {
            SwingUtilities.invokeLater(() -> {
                String text = textField.getText();
                if (StrUtil.equals(text, "\n") || StrUtil.equals(preText, text) || flag) {
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
