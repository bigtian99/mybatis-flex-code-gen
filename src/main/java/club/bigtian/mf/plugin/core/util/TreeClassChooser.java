package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.psi.PsiClass;

public class TreeClassChooser {
    /**
     * 树类选择器
     *
     * @return {@code String}
     */
    public static String treeClassNameChooser() {
        TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(ProjectUtils.getCurrentProject());
        com.intellij.ide.util.TreeClassChooser chooser = chooserFactory.createAllProjectScopeChooser("选择类");
        chooser.showDialog();
        PsiClass selected = chooser.getSelected();
        if (ObjectUtil.isNull(selected)) {
            return null;
        }
        String qualifiedName = selected.getQualifiedName();
        return qualifiedName;
    }

    public static PsiClass treeClassChooser() {
        TreeClassChooserFactory chooserFactory = TreeClassChooserFactory.getInstance(ProjectUtils.getCurrentProject());
        com.intellij.ide.util.TreeClassChooser chooser = chooserFactory.createAllProjectScopeChooser("选择类");
        chooser.showDialog();
        PsiClass selected = chooser.getSelected();
        if (ObjectUtil.isNull(selected)) {
            return null;
        }
        return selected;
    }
}
