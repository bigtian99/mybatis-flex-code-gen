package club.bigtian.mf.plugin.core.console;

import club.bigtian.mf.plugin.core.MybatisFlexAgentFilter;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MyBatisFlexAgentConsoleFilterProvider implements ConsoleFilterProvider {
    private final Key<MybatisFlexAgentFilter> key = Key.create(MybatisFlexAgentFilter.class.getName());

    @Override
    public Filter @NotNull [] getDefaultFilters(@NotNull Project project) {
        MybatisFlexAgentFilter filter = project.getUserData(key);
        if (Objects.isNull(filter)) {
            filter = new MybatisFlexAgentFilter(project);
            project.putUserData(key, filter);
        }
        return new Filter[] { filter };
    }
}
