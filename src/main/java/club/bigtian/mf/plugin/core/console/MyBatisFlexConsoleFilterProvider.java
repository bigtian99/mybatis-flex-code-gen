package club.bigtian.mf.plugin.core.console;

import club.bigtian.mf.plugin.core.log.MyBatisFlexLogConsoleFilter;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MyBatisFlexConsoleFilterProvider implements ConsoleFilterProvider {
    private final Key<MyBatisFlexLogConsoleFilter> key = Key.create(MyBatisFlexLogConsoleFilter.class.getName());

    @Override
    public Filter @NotNull [] getDefaultFilters(@NotNull Project project) {
        MyBatisFlexLogConsoleFilter filter = project.getUserData(key);
        if (Objects.isNull(filter)) {
            filter = new MyBatisFlexLogConsoleFilter(project);
            project.putUserData(key, filter);
        }
        return new Filter[] { filter };
    }
}
