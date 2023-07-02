package com.mybatisflex.plugin.core.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectUtils {

    /**
     * 开放项目
     *
     * @return {@code Project[]}
     */
    public static Project[] getOpenProjects() {
        return ProjectManager.getInstance().getOpenProjects();
    }

    public static List<String> getProject() {
        return Arrays.stream(getOpenProjects())
                .map(Project::getName)
                .collect(Collectors.toList());
    }
}
