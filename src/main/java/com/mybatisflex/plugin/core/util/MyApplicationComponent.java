package com.mybatisflex.plugin.core.util;

import cn.hutool.core.util.ReflectUtil;
import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.ide.ApplicationLoadListener;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.ProjectFrameHelper;

import javax.swing.*;


public class MyApplicationComponent implements ApplicationInitializedListener {


    @Override
    public void componentsInitialized() {
        ProjectManager.getInstance().addProjectManagerListener(new MyApplicationComponent.MyProjectManagerListener());

    }

    public static class MyProjectManagerListener implements ProjectManagerListener {
        @Override
        public void projectOpened(Project project) {
            ProjectUtils.setCurrentProject(project);
        }
    }
}
