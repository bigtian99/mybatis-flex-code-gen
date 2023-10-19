package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.enums.DbType;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MybatisFlexUtil {
    private static Map<String, String> SQL_DIALECT_MAP = new HashMap<>();

    /**
     * 初始化数据库方言 map
     */
    private static void initSqlDialectMap() {

        SQL_DIALECT_MAP = Arrays.stream(DbType.values())
                .collect(Collectors.toMap(el -> {
                    if (el.getName().contains("2005")) {
                        return "SQLServer_2005 数据库";
                    }
                    String text = el.getRemarks();
                    if (text.contains(",")) {
                        return StrUtil.subBetween(text, ", \"", "\"");
                    }
                    return text;
                }, DbType::getName));
    }

    /**
     * 获取数据库方言（中文描述）
     *
     * @return
     */
    public static Collection<String> getTargetClassFieldRemark() {
        if (CollUtil.isEmpty(SQL_DIALECT_MAP)) {
            initSqlDialectMap();
        }
        return SQL_DIALECT_MAP.keySet();
    }

    /**
     * 根据 dbType 的中文描述获取类型
     *
     * @param sqlName
     * @return
     */

    public static String getDialectType(String sqlName) {
        if (CollUtil.isEmpty(SQL_DIALECT_MAP)) {
            initSqlDialectMap();
        }
        return SQL_DIALECT_MAP.getOrDefault(sqlName, "MYSQL");
    }

    /**
     * 根据类型获取数据库方言类型
     *
     * @param type
     * @return {@code String}
     */
    public static String getDialectChinese(String type) {
        if (CollUtil.isEmpty(SQL_DIALECT_MAP)) {
            initSqlDialectMap();
        }
        Map<String, String> chineseMap = SQL_DIALECT_MAP.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue, // 使用原始值作为新的键
                        Map.Entry::getKey    // 使用原始键作为新的值
                ));
        return chineseMap.get(type);
    }

    /**
     * 判断是不是 flex 项目
     *
     * @return {@code Boolean}
     */
    // public static Boolean isFlexProject() {
    //     PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.dialect.DbType");
    //     return ObjectUtil.isNull(psiClass);
    // }
    public static String getSelectedText(AnActionEvent event) {
        // 根据AnActionEvent获取选择的编辑器选中文本
        Editor editor = getEditor(event);
        SelectionModel selectionModel = editor.getSelectionModel();
        return selectionModel.getSelectedText();
    }

    @Nullable
    public static Editor getEditor(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        return editor;
    }

    public static int getLine(AnActionEvent event) {
        Editor editor = getEditor(event);
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        return  editor.logicalPositionToOffset(new LogicalPosition(logicalPosition.line+1, logicalPosition.column));
    }
}
