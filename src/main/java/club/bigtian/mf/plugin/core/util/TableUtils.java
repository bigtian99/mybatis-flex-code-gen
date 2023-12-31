package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.database.dialects.DatabaseDialectEx;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbDataSourceImpl;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.database.util.JdbcUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TableUtils {


    /**
     * 得到选中表名
     *
     * @param actionEvent 行动事件
     * @return {@code List<TableInfo>}
     */
    public static List<String> getSelectedTableName(AnActionEvent actionEvent) {
        DataKey<Object[]> databaseNodes = DataKey.create("DATABASE_NODES");
        Object[] data = actionEvent.getData(databaseNodes);
        return Arrays.stream(data).map(item -> {
            DasTable dasTable = (DasTable) item;
            return dasTable.getName();
        }).collect(Collectors.toList());

    }

    /**
     * 得到所有表
     *
     * @param event 事件
     * @return {@code List<TableInfo>}
     */
    public static List<TableInfo> getAllTables(AnActionEvent event) {
        DbTableImpl table = (DbTableImpl) event.getData(CommonDataKeys.PSI_ELEMENT);
        DbElement tableParent = table.getParent();
        assert tableParent != null;
        List<DasTable> list = tableParent.getDasChildren(ObjectKind.TABLE).map(el -> (DasTable) el)
                .toList();
        List<TableInfo> tableInfoList = new ArrayList<>();
        getTableInfoList(list, tableInfoList);
        return tableInfoList;
    }


    /**
     * 得到方言
     *
     * @param dasTable das表
     * @return {@code DatabaseDialectEx}
     */
    public static DatabaseDialectEx getDialect(DasTable dasTable) {
        DbTableImpl table = (DbTableImpl) dasTable;
        DbDataSourceImpl dataSource = table.getDataSource();
        return dataSource.getDatabaseDialect();
    }

    /**
     * 得到表信息列表
     *
     * @param selectedTableList 选择表列表
     * @param tableInfoList     表信息列表
     */
    private static void getTableInfoList(List<DasTable> selectedTableList, List<TableInfo> tableInfoList) {
        DasTable dasTable = selectedTableList.get(0);
        DatabaseDialectEx dialect = getDialect(dasTable);
        for (DasTable table : selectedTableList) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setName(table.getName());
            tableInfo.setComment(table.getComment());
            List<ColumnInfo> columnList = new ArrayList<>();
            JBIterable<? extends DasObject> columns = table.getDasChildren(ObjectKind.COLUMN);
            for (DasObject column : columns) {
                ColumnInfo columnInfo = new ColumnInfo();
                DasColumn dasColumn = (DasColumn) column;
                columnInfo.setName(dasColumn.getName());
                columnInfo.setFieldName(StrUtil.toCamelCase(dasColumn.getName()));
                String jdbcTypeStr = getFieldType(dasColumn.getDataType().typeName);
                int jdbc = dialect.getJavaTypeForNativeType(jdbcTypeStr);
                String jdbcTypeName = JdbcUtil.getJdbcTypeName(jdbc);
                String fieldType = getFieldType(jdbc, tableInfo, jdbcTypeName);
                columnInfo.setFieldType(fieldType);
                columnInfo.setNotNull(dasColumn.isNotNull());
                columnInfo.setComment(ObjectUtil.defaultIfNull(dasColumn.getComment(), "").replaceAll("\r\n", ""));
                columnInfo.setMethodName(StrUtil.upperFirst(columnInfo.getFieldName()));
                columnInfo.setType(jdbcTypeName);
                columnInfo.setPrimaryKey(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.PRIMARY_KEY));
                columnInfo.setAutoIncrement(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.AUTO_GENERATED));
                columnList.add(columnInfo);
            }
            tableInfo.setColumnList(columnList);
            tableInfoList.add(tableInfo);
        }
    }

    /**
     * 获取字段类型
     *
     * @param jdbc         jdbc
     * @param jdbcTypeName
     * @return {@code String}
     */
    private static String getFieldType(int jdbc, TableInfo tableInfo, String jdbcTypeName) {
        String className = convert(jdbc).getName();
        if (Object.class.getName().equals(className)) {
            String fieldType = MybatisFlexPluginConfigData.getFieldType(jdbcTypeName);
            if (StrUtil.isNotBlank(fieldType)) {
                className = fieldType;
            }
        }

        boolean flag = className.contains(";");
        if (flag) {
            className = className.replace(";", "").replace("[L", "");
        }
        tableInfo.addImportClassItem(className);
        String fieldType = className.substring(className.lastIndexOf(".") + 1);
        if (flag) {
            fieldType += "[]";
        }
        return fieldType;
    }


    /**
     * 获取字段类型
     *
     * @param sqlType sql类型
     * @return {@code Class<?>}
     */
    public static Class<?> convert(int sqlType) {
        switch (sqlType) {
            case Types.BIT:
                return Boolean.class;
            case Types.TINYINT:
                return Byte.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.FLOAT:
            case Types.REAL:
                return Float.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return java.math.BigDecimal.class;
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
                return String.class;
            case Types.TIME:
//                return java.sql.Time.class;
            case Types.TIMESTAMP:
//                return java.sql.Timestamp.class;
            case Types.DATE:
                return Date.class;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return Byte[].class;
            case Types.CLOB:
            case Types.NCLOB:
            case Types.BLOB:
                return java.sql.Blob.class;
            case Types.ARRAY:
                return java.sql.Array.class;
            case Types.STRUCT:
                return java.sql.Struct.class;
            case Types.REF:
                return java.sql.Ref.class;
            case Types.SQLXML:
                return java.sql.SQLXML.class;
            default:
                return Object.class;
        }
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
