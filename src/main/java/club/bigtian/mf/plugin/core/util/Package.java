package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiPackage;

public class Package {

    /**
     * 选择包路径
     *
     * @param module 模块
     * @return {@code String}
     */
    public static String selectPackage(Module module, String... packagePath) {
        PackageChooserDialog chooser = new PackageChooserDialog("Select Package", module);
        if (packagePath.length > 0) {
            chooser.selectPackage(packagePath[0]);
        }
        // 显示对话框并等待用户选择
        chooser.show();
        PsiPackage selectedPackage = chooser.getSelectedPackage();
        if (ObjectUtil.isNull(selectedPackage)) {
            return packagePath[0];
        }
        return selectedPackage.getQualifiedName();
    }

    /**
     * 选择包+resources下面的路径
     *
     * @param module 模块
     * @return {@code String}
     */
    public static String selectPackageResources(Module module, String... packagePath) {
        PackageChooserDialogCustom chooser = new PackageChooserDialogCustom("Select Package", module);
        if (packagePath.length > 0) {
            chooser.selectPackage(packagePath[0]);
        }
        // 显示对话框并等待用户选择
        chooser.show();
        PsiPackage selectedPackage = chooser.getSelectedPackage();
        if (ObjectUtil.isNull(selectedPackage)) {
            return packagePath[0];
        }
        return selectedPackage.getQualifiedName();
    }
}
