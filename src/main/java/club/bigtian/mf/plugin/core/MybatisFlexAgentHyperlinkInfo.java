package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.util.DbUtil;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.project.Project;

public class MybatisFlexAgentHyperlinkInfo implements HyperlinkInfo {
    private String sql;

    public MybatisFlexAgentHyperlinkInfo(String sql) {
        this.sql = sql;
    }

    @Override
    public void navigate(Project project) {
        DbUtil.openDbConsole(sql,null);
    }


}