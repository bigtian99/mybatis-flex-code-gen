package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.windows.MybatisFlexCodeGenerateDialog;
import com.intellij.database.model.DasTable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 表动作
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class TableAction extends AnAction {

    // 静态代码将在系统启动时执行

    @Override
    public void actionPerformed(AnActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            MybatisFlexCodeGenerateDialog generateWin = new MybatisFlexCodeGenerateDialog(e);
            generateWin.setVisible(true);
        });
    }


    /**
     * 判断选中的是否是表，是表则显示，否则不显示
     *
     * @param e 事件
     */
    @Override
    public void update(AnActionEvent e) {
        ProjectUtils.setCurrentProject(e.getProject());
        Object selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        boolean isSelectedTable = selectedElement instanceof DasTable;
        e.getPresentation().setVisible(isSelectedTable);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
