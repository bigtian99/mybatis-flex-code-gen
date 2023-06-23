package com.mybatisflex.plugin.core.filter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FilterComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
    private List<String> items;
    private List<String> filteredItems;
    private String selectedItem;

    public FilterComboBoxModel(List<String> items) {
        this.items = items;
        this.filteredItems = new ArrayList<>(items);
    }

    public void filterItems(String filterText) {
        filteredItems.clear();
        for (String item : items) {
            if (item.toLowerCase().contains(filterText.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        fireContentsChanged(this, 0, getSize());
    }

    @Override
    public int getSize() {
        return filteredItems.size();
    }

    @Override
    public String getElementAt(int index) {
        return filteredItems.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = (String) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }
}