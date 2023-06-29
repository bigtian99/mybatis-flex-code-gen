package com.mybatisflex.plugin.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author daijunxiong
 * @date 2023/06/22
 */
public class DDLUtils {
    private static Set<String> importClassList = new HashSet<>();

    /**
     * @return {@code Set<String>}
     */
    public static Set<String> getImportClassList() {
        return importClassList;
    }

    public static void clear() {
        importClassList.clear();
    }

    public static String mapFieldType(String fieldType) {

        fieldType = getFieldType(fieldType);
        fieldType = fieldType.toUpperCase();
        String javaType = null;
        String packagePath = null;
        // 自定义字段类型映射
        if (fieldType.equalsIgnoreCase("DECIMAL")) {
            javaType = "BigDecimal";
            packagePath = "java.math.BigDecimal";
        } else if (fieldType.equalsIgnoreCase("TINYINT")) {
            javaType = "byte";
        } else if (fieldType.equalsIgnoreCase("SMALLINT")) {
            javaType = "short";
        } else if (fieldType.equalsIgnoreCase("MEDIUMINT") ||
                fieldType.equalsIgnoreCase("INT")) {
            javaType = "int";
        } else if (fieldType.equalsIgnoreCase("BIGINT")) {
            javaType = "long";
        } else if (fieldType.equalsIgnoreCase("FLOAT")) {
            javaType = "float";
        } else if (fieldType.equalsIgnoreCase("DOUBLE")) {
            javaType = "double";
        } else if (fieldType.equalsIgnoreCase("DATE") ||
                fieldType.equalsIgnoreCase("TIME") ||
                fieldType.equalsIgnoreCase("DATETIME") ||
                fieldType.equalsIgnoreCase("TIMESTAMP") ||
                fieldType.equalsIgnoreCase("YEAR")) {
            javaType = "Date";
            packagePath = "java.util.Date";
        } else if (fieldType.equalsIgnoreCase("CHAR") ||
                fieldType.equalsIgnoreCase("VARCHAR") ||
                fieldType.equalsIgnoreCase("TINYTEXT") ||
                fieldType.equalsIgnoreCase("TEXT") ||
                fieldType.equalsIgnoreCase("MEDIUMTEXT") ||
                fieldType.equalsIgnoreCase("LONGTEXT") ||
                fieldType.equalsIgnoreCase("ENUM") ||
                fieldType.equalsIgnoreCase("SET") ||
                fieldType.equalsIgnoreCase("JSON")) {
            javaType = "String";
        } else if (fieldType.equalsIgnoreCase("BINARY") ||
                fieldType.equalsIgnoreCase("VARBINARY") ||
                fieldType.equalsIgnoreCase("TINYBLOB") ||
                fieldType.equalsIgnoreCase("BLOB") ||
                fieldType.equalsIgnoreCase("MEDIUMBLOB") ||
                fieldType.equalsIgnoreCase("LONGBLOB")) {
            javaType = "byte[]";
        } else if (fieldType.equalsIgnoreCase("BOOLEAN") || fieldType.equals("BOOL")) {
            javaType = "boolean";
        }

        if (javaType != null) {
            // 设置包路径
            if (packagePath == null) {
                return javaType;
            }
            importClassList.add(packagePath);
            return javaType;
        }

        // 其他类型保持不变
        return null;
    }

    @NotNull
    public static String getFieldType(String fieldType) {
        if (fieldType.contains("(") || fieldType.contains(" ")) {
            fieldType = fieldType.replace("(", " ");
            int idx = fieldType.indexOf(" ");
            fieldType = fieldType.substring(0, idx);
        }
        return fieldType;
    }
}
