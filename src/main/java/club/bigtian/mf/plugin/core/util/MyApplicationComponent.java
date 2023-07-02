package club.bigtian.mf.plugin.core.util;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;


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
