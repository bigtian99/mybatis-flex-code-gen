package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;

import static club.bigtian.mf.plugin.core.log.MyBatisFlexLogConsoleFilter.parseParams;
import static club.bigtian.mf.plugin.core.log.MyBatisFlexLogConsoleFilter.parseSql;

public class MybatisFlexAgentFilter implements Filter {

    private String sql = null;

    public MybatisFlexAgentFilter() {

    }

    @NotNull
    @Override
    public Result applyFilter(@NotNull String line, int entireLength) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        final String preparing = ObjectUtil.defaultIfNull(config.getPreparing(), "Preparing:");
        String parameters = ObjectUtil.defaultIfNull(config.getParameters(), "Parameters:");
        if (line.contains(preparing)) {
            sql = line;
            return null;
        }
        if ((StringUtils.isNotBlank(sql) && !line.contains(parameters)) || StringUtils.isBlank(sql)) {
            return null;
        }
        final String wholeSql = parseSql(StringUtils.substringAfter(sql, preparing), parseParams(StringUtils.substringAfter(line, parameters))).toString();
        int startIndex = entireLength - line.length();
        HyperlinkInfo hyperlinkInfo = new MybatisFlexAgentHyperlinkInfo(wholeSql);
        sql = null;
        return new Result(startIndex + line.indexOf(parameters), startIndex + line.indexOf(parameters) + parameters.length() - 1, hyperlinkInfo);
    }
}
