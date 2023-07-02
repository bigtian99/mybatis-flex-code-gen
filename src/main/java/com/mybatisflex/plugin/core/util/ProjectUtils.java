package com.mybatisflex.plugin.core.util;

import cn.hutool.core.io.FileUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.project.ProjectUtilCore;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

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
        currentProject = project;
    }
}
