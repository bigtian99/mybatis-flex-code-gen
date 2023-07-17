package club.bigtian.mf.plugin.action.log;

import club.bigtian.mf.plugin.core.log.MyBatisLogManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * MyBatisLogAction
 *
 * @author huangxingguang
 */
public class MyBatisLogAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        rerun(project);
    }

    public void rerun(final Project project) {
        MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
        if (Objects.nonNull(manager)) {
            Disposer.dispose(manager);
        }
        MyBatisLogManager.createInstance(project).run();
        manager = MyBatisLogManager.getInstance(project);
        manager.println("日志功能搬运于Github 开源项目 mybatis-log-free 项目地址：https://github.com/starxg/mybatis-log-plugin-free.git");
    }
}
