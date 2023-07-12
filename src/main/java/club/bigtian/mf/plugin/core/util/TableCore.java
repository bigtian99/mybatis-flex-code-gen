package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

public class TableCore {

    /**
     * 得到类名
     *
     * @param tableName   表名
     * @param tablePrefix 表前缀
     * @return {@code String}
     */
    public static String getClassName(String tableName, String tablePrefix) {
        tableName = getTableName(tableName, tablePrefix);
        return StrUtil.upperFirst(tableName);
    }

    /**
     * 得到表名
     *
     * @param tableName   表名
     * @param tablePrefix 表前缀
     * @return {@code String}
     */
    public static String getTableName(String tableName, String tablePrefix) {
        tablePrefix = ObjectUtil.defaultIfNull(tablePrefix, "");
        String[] tablePrefixArr = tablePrefix.split(";");
        for (String prefix : tablePrefixArr) {
            if (tableName.startsWith(prefix)) {
                tableName = tableName.replaceFirst(prefix, "");
                break;
            }
        }
        return StrUtil.toCamelCase(tableName);
    }
}
