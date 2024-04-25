package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.entity.TableInfo;
import club.bigtian.mf.plugin.windows.MybatisFlexSettingDialog;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ProjectUtils.setCurrentProject(e.getProject());
        List<TableInfo> selectedTableInfo = new ArrayList<>();
        MybatisFlexSettingDialog dialog = new MybatisFlexSettingDialog(e.getProject(), selectedTableInfo, () -> {

        });
        dialog.setVisible(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
