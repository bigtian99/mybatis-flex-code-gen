package club.bigtian.mf.plugin.utils;

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

    public static String mapToMyBatisJdbcType(String dbDataType) {
        dbDataType=getFieldType(dbDataType);
        if (dbDataType.equalsIgnoreCase("INTEGER") || dbDataType.equalsIgnoreCase("INT")) {
            return "INTEGER";
        } else if (dbDataType.equalsIgnoreCase("BIGINT")) {
            return "BIGINT";
        } else if (dbDataType.equalsIgnoreCase("SMALLINT")) {
            return "SMALLINT";
        } else if (dbDataType.equalsIgnoreCase("TINYINT")) {
            return "TINYINT";
        } else if (dbDataType.equalsIgnoreCase("FLOAT")) {
            return "FLOAT";
        } else if (dbDataType.equalsIgnoreCase("DOUBLE")) {
            return "DOUBLE";
        } else if (dbDataType.equalsIgnoreCase("DECIMAL") || dbDataType.equalsIgnoreCase("NUMERIC")) {
            return "DECIMAL";
        } else if (dbDataType.equalsIgnoreCase("CHAR")) {
            return "CHAR";
        } else if (dbDataType.equalsIgnoreCase("VARCHAR")) {
            return "VARCHAR";
        } else if (dbDataType.equalsIgnoreCase("LONGVARCHAR")) {
            return "LONGVARCHAR";
        } else if (dbDataType.equalsIgnoreCase("CLOB")) {
            return "CLOB";
        } else if (dbDataType.equalsIgnoreCase("NCHAR")) {
            return "NCHAR";
        } else if (dbDataType.equalsIgnoreCase("NVARCHAR")) {
            return "NVARCHAR";
        } else if (dbDataType.equalsIgnoreCase("LONGNVARCHAR")) {
            return "LONGNVARCHAR";
        } else if (dbDataType.equalsIgnoreCase("NCLOB")) {
            return "NCLOB";
        } else if (dbDataType.equalsIgnoreCase("DATE")) {
            return "DATE";
        } else if (dbDataType.equalsIgnoreCase("TIME")) {
            return "TIME";
        } else if (dbDataType.equalsIgnoreCase("TIMESTAMP")) {
            return "TIMESTAMP";
        } else if (dbDataType.equalsIgnoreCase("BIT")) {
            return "BIT";
        } else if (dbDataType.equalsIgnoreCase("BOOLEAN")) {
            return "BOOLEAN";
        } else if (dbDataType.equalsIgnoreCase("BINARY")) {
            return "BINARY";
        } else if (dbDataType.equalsIgnoreCase("VARBINARY")) {
            return "VARBINARY";
        } else if (dbDataType.equalsIgnoreCase("LONGVARBINARY")) {
            return "LONGVARBINARY";
        } else if (dbDataType.equalsIgnoreCase("BLOB")) {
            return "BLOB";
        } else if (dbDataType.equalsIgnoreCase("NULL")) {
            return "NULL";
        } else if (dbDataType.equalsIgnoreCase("ARRAY")) {
            return "ARRAY";
        } else if (dbDataType.equalsIgnoreCase("DISTINCT")) {
            return "DISTINCT";
        } else if (dbDataType.equalsIgnoreCase("REF")) {
            return "REF";
        } else if (dbDataType.equalsIgnoreCase("STRUCT")) {
            return "STRUCT";
        } else if (dbDataType.equalsIgnoreCase("SQLXML")) {
            return "SQLXML";
        } else {
            // 默认情况下，返回VARCHAR
            return "VARCHAR";
        }
    }
}
