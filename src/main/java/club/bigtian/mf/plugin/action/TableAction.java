package club.bigtian.mf.plugin.action;

import club.bigtian.mf.plugin.windows.MybatisFlexCodeGenerateWin;
import club.bigtian.mf.plugin.windows.ProgressBarDialog;
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
//        VirtualFile virtualFile = VirtualFileUtils.transToJavaFile("/Users/daijunxiong/code/MybatisFlex-Hepler/src/main/resources/club/mappingsxml");
//        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
//            PsiDirectoryFactory.getInstance(e.getProject()).createDirectory(virtualFile);
//        });



        // 在对话框显示后开始更新进度
        MybatisFlexCodeGenerateWin generateWin = new MybatisFlexCodeGenerateWin(e);
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
