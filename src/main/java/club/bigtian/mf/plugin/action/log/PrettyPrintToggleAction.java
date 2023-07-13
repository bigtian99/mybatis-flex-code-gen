package club.bigtian.mf.plugin.action.log;

import club.bigtian.mf.plugin.core.icons.Icons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * PrettyPrintToggleAction
 * @author huangxingguang
 */
public class PrettyPrintToggleAction extends ToggleAction {

    public PrettyPrintToggleAction() {
        super("Pretty Print", "Pretty Print", Icons.PRETTY_PRINT);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return false;
        }
        return PropertiesComponent.getInstance(e.getProject()).getBoolean(PrettyPrintToggleAction.class.getName());
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (Objects.isNull(e.getProject())) {
            return;
        }

        PropertiesComponent.getInstance(e.getProject()).setValue(PrettyPrintToggleAction.class.getName(), state);

    }

}