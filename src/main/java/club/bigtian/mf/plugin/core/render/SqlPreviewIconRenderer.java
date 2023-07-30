package club.bigtian.mf.plugin.core.render;

import club.bigtian.mf.plugin.action.flex.SQLPreviewAction;
import club.bigtian.mf.plugin.core.icons.Icons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SqlPreviewIconRenderer extends GutterIconRenderer {
    private int lineNumber;
    private PsiJavaFile psiFile;
    // 添加一个标志用于控制按钮的启用状态，防止用户多次点击
    private static boolean isEnabled = true;
    Map<Integer, String> iconMap;

    public SqlPreviewIconRenderer(int lineNumber, PsiJavaFile psiFile, Map<Integer, String> iconMap) {
        this.lineNumber = lineNumber;
        this.psiFile = psiFile;
        this.iconMap = iconMap;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public @Nullable AnAction getClickAction() {
        return new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

                if (isEnabled) { // 检查按钮是否启用
                    String selectedText = iconMap.get(lineNumber);
                    isEnabled = false; // 点击后禁用按钮
                    new SQLPreviewAction().preview(selectedText, psiFile, () -> {
                        // 在SQLPreviewAction完成所有逻辑后，再启用按钮
                        isEnabled = true;
                    });

                }
            }
        };

    }

    @Override
    public boolean isNavigateAction() {
        return true;
    }


    @Override
    public @Nullable String getTooltipText() {
        return "预览 SQL";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull Icon getIcon() {
        return Icons.SQL_PREVIEW;
    }
}