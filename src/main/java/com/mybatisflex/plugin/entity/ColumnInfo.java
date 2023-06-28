package com.mybatisflex.plugin.entity;

/**
 * 列信息
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class ColumnInfo {
    /**
     * 列名
     */
    private String name;

    /**
     * 字段名
     */
    private String fieldName;
    private String fieldType;
    /**
     * 数据类型
     */
    private String type;
    /**
     * 列注释
     */
    private String comment;
    /**
     * 是主键
     */
    private boolean isPrimaryKey;

    /**
     * 是否自动增长
     */
    private boolean isAutoIncrement;

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }


    @Override
    public String toString() {
        return "ColumnInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isAutoIncrement=" + isAutoIncrement +
                '}';
    }
}
