package com.mybatisflex.plugin.entity;

import java.util.List;

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
