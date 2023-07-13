package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.windows.MybatisFlexCodeGenerateDialog;
import com.intellij.database.model.DasTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;

/**
 * 表动作
 *
 * @author daijunxiong
 * @date 2023/06/22
 */
public class TableAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        MybatisFlexCodeGenerateDialog generateWin = new MybatisFlexCodeGenerateDialog(e);
        generateWin.show();
    }


    /**
     * 判断选中的是否是表，是表则显示，否则不显示
     *
     * @param e 事件
     */
    @Override
    public void update(AnActionEvent e) {
        Object selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        boolean isSelectedTable = selectedElement instanceof DasTable;
        e.getPresentation().setVisible(isSelectedTable);
    }
}
