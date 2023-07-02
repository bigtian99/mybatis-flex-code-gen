package com.mybatisflex.plugin.core.util;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectUtils {


    /**
     * 获取当前项目
     *
     * @return {@code Project}
     */
    public static Project getCurrentProject() {
        Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
        if (project == null) {
            project = Arrays.stream(ProjectManager.getInstance().getOpenProjects())
                    .filter(Project::isOpen)
                    .map(el->{
                        System.out.println(el.getName());
                        return el;
                    })
                    .findFirst()
                    .orElse(null);
        }
        return project;
    }


    /**
     * 得到当前项目名称
     *
     * @return {@code String}
     */
    public static String getCurrentProjectName() {
        return getCurrentProject().getName();
    }
}
