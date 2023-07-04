package club.bigtian.mf.plugin.core.render;

import club.bigtian.mf.plugin.entity.TableInfo;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TableListCellRenderer extends JLabel implements ListCellRenderer<String> {
    private JLabel label;
    private JLabel rowEndLabel;
    Map<String, TableInfo> tableInfoMap;
    String searchTableName;

    Map<String, String> highlightKey=new HashMap<>();

    public Map<String, String> getHighlightKey() {
        return highlightKey;
    }

    public void setHighlightKey(Map<String, String> highlightKey) {
        this.highlightKey = highlightKey;
    }

    public String getSearchTableName() {
        return searchTableName;
    }

    public void setSearchTableName(String searchTableName) {
        this.searchTableName = searchTableName;
    }

    public TableListCellRenderer(Map<String, TableInfo> tableInfoMap) {
        setOpaque(true);
        setLayout(new BorderLayout());
        this.tableInfoMap = tableInfoMap;
        label = new JLabel();
        rowEndLabel = new JLabel();
        rowEndLabel.setForeground(Color.GRAY);
        add(label, BorderLayout.WEST);
        add(rowEndLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        label.setText(value);
        if (highlightKey.containsKey(value)) {
            label.setText(highlightKey.get(value));
        }
        rowEndLabel.setText(tableInfoMap.get(value).getComment());
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
