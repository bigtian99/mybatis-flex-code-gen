package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import club.bigtian.mf.plugin.utils.DDLUtils;
import cn.hutool.core.util.StrUtil;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.util.containers.JBIterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.database.view.DatabaseView.DATABASE_NODES;

public class TableUtils {
    /**
     * 得到选中表信息
     *
     * @param event 行动事件
     * @return {@code List<TableInfo>}
     */
    public static List<TableInfo> getSelectedTableInfo(AnActionEvent event) {
        List<TableInfo> tableInfoList = new ArrayList<>();
        List<DasTable> selectedTableList = Arrays.stream(event.getData(DATABASE_NODES)).map(el -> (DasTable) el).collect(Collectors.toList());
        getTableInfoList(selectedTableList, tableInfoList);
        return tableInfoList;
    }

    /**
     * 得到选中表名
     *
     * @param actionEvent 行动事件
     * @return {@code List<TableInfo>}
     */
    public static List<String> getSelectedTableName(AnActionEvent actionEvent) {
        return Arrays.stream(actionEvent.getData(DATABASE_NODES))
                .map(el -> ((DasObject) el).getName()).collect(Collectors.toList());
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
        List<DasTable> list = tableParent.getDasChildren(ObjectKind.TABLE).map(el -> (DasTable) el)
                .toList();
        List<TableInfo> tableInfoList = new ArrayList<>();
        getTableInfoList(list, tableInfoList);
        return tableInfoList;
    }


    /**
     * 获取数据库方言
     *
     * @param e 事件
     * @return {@code String} 数据库方言
     */
    public static String getDialect(AnActionEvent e) {
        DbTableImpl table = (DbTableImpl) e.getData(CommonDataKeys.PSI_ELEMENT);
        return table.getDataSource().getDatabaseDialect().getDisplayName();
    }

    private static void getTableInfoList(List<DasTable> selectedTableList, List<TableInfo> tableInfoList) {
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
                columnInfo.setFieldType(DDLUtils.mapFieldType(dasColumn.getDataType().typeName));
                columnInfo.setComment(dasColumn.getComment());
                columnInfo.setMethodName(StrUtil.upperFirst(columnInfo.getFieldName()));
                columnInfo.setType(DDLUtils.mapToMyBatisJdbcType(dasColumn.getDataType().typeName).toUpperCase());
                columnInfo.setPrimaryKey(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.PRIMARY_KEY));
                columnInfo.setAutoIncrement(table.getColumnAttrs(dasColumn).contains(DasColumn.Attribute.AUTO_GENERATED));
                columnList.add(columnInfo);
            }
            tableInfo.setColumnList(columnList);
            tableInfoList.add(tableInfo);
        }
    }
}
