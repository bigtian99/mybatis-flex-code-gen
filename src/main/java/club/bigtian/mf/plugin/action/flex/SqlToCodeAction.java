package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.windows.SqlToCodeDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * 根据 sql 生成 flex 代码
 */
public class SqlToCodeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        new SqlToCodeDialog(e).show();
    }
}
