package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.spring.boot.run.SpringBootApplicationRunConfiguration;

import java.util.Map;

/**
 * 这个类继承了JavaProgramPatcher，用于修改RunProfile的JavaParameters。
 * 它专门用于向JVM参数中添加Java代理。
 */
public class PerRun extends JavaProgramPatcher {
    /**
     * 这个方法被调用来修改RunProfile的JavaParameters。
     * 它向JVM参数中添加一个Java代理。
     *
     * @param executor       将运行配置文件的执行器。
     * @param runProfile     要运行的配置文件。
     * @param javaParameters 要修改的Java参数。
     */
    @Override
    public void patchJavaParameters(Executor executor, RunProfile runProfile, JavaParameters javaParameters) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        if (ObjectUtil.defaultIfNull(config.getEnableDebug(), true)) {
            setLogLevel(runProfile, javaParameters);
        }
        if (runProfile instanceof SpringBootApplicationRunConfiguration springBootApplicationRunConfiguration) {
            Project project = springBootApplicationRunConfiguration.getProject();
            ConsoleView consoleView = new ConsoleViewImpl(project, true);
            consoleView.addMessageFilter(new MybatisFlexAgentFilter());
        }

    }

    /**
     * 设置日志级别(只有在spring boot项目生效)
     *
     * @param runProfile
     * @param javaParameters
     */

    private static void setLogLevel(RunProfile runProfile, JavaParameters javaParameters) {
        if (runProfile instanceof SpringBootApplicationRunConfiguration) {
            SpringBootApplicationRunConfiguration springBootApplicationRunConfiguration = (SpringBootApplicationRunConfiguration) runProfile;
            PsiClass mainClass = springBootApplicationRunConfiguration.getMainClass();
            Map<String, String> env = javaParameters.getEnv();
            String packageName = PsiJavaFileUtil.getPackageName(mainClass);
            env.put(StrUtil.format("logging.level.{}", packageName), "debug");
        }

    }
}