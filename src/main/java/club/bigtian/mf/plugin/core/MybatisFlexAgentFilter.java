package club.bigtian.mf.plugin.core;

import cn.hutool.core.util.StrUtil;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.project.Project;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

public class MybatisFlexAgentFilter implements Filter {

    private final Project project;

    public MybatisFlexAgentFilter(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public Result applyFilter(@NotNull String line, int entireLength) {
        if (line.contains("Mybatis-flex-Helper:")) {
            int startIndex = entireLength - line.length();
            HyperlinkInfo hyperlinkInfo = new MybatisFlexAgentHyperlinkInfo(project, StrUtil.subAfter(line, "Mybatis-flex-Helper:", false));
            return new Result(startIndex+line.indexOf("Mybatis-flex-Helper"), startIndex+line.indexOf("Mybatis-flex-Helper")+"Mybatis-flex-Helper".length(), hyperlinkInfo);
        }
        return null;
    }
}
