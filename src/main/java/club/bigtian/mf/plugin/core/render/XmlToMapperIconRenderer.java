package club.bigtian.mf.plugin.core.render;

import club.bigtian.mf.plugin.core.icons.Icons;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XmlToMapperIconRenderer extends GutterIconRenderer {

    private PsiElement element;

    public XmlToMapperIconRenderer(PsiElement element) {
        this.element = element;
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
                NavigationUtil.activateFileWithPsiElement(element);

            }
        };

    }

    @Override
    public boolean isNavigateAction() {
        return true;
    }


    @Override
    public @Nullable String getTooltipText() {
        return "跳转到Mapper文件";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull Icon getIcon() {
        return Icons.FLEX_XML;
    }
}