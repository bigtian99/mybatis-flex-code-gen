package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.project.Project;

public class ProjectUtils {

    private static Project currentProject;

    /**
     * 获取当前项目
     *
     * @return {@code Project}
     */
    public static Project getCurrentProject() {

        return currentProject;
    }


    /**
     * 得到当前项目名称
     *
     * @return {@code String}
     */
    public static String getCurrentProjectName() {
        return getCurrentProject().getName();
    }

    public static void setCurrentProject(Project project) {
        if (ObjectUtil.isNotNull(project)) {
            currentProject = project;
        }
    }
}
