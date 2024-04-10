package club.bigtian.mf.plugin.core.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class CodeReformat {

    /**
     * 重新格式化
     *
     * @param psiElement psi元素
     * @return {@code PsiElement}
     */
    public static PsiElement reformat(PsiElement psiElement) {
        return CodeStyleManager.getInstance(ProjectUtils.getCurrentProject()).reformat(psiElement);
    }
}
