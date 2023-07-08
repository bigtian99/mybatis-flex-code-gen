package club.bigtian.mf.plugin.entity;

import cn.hutool.core.collection.CollUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 表信息
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class TableInfo {
    /**
     * 表名
     */
    private String name;

    /**
     * 表注释
     */
    private String comment;

    /**
     * 列信息集合
     */
    private List<ColumnInfo> columnList;

    /**
     * 导入的类集合
     */
    private Set<String> importClassList;

    public Set<String> getImportClassList() {
        return importClassList;
    }

    public void addImportClassItem(String importClassItem) {
        if (CollUtil.isEmpty(importClassList)) {
            importClassList = new HashSet<>();
        }
        this.importClassList.add(importClassItem);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ColumnInfo> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ColumnInfo> columnList) {
        this.columnList = columnList;
    }


    @Override
    public String toString() {
        return "TableInfo{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", columnList=" + columnList +
                '}';
    }
}
