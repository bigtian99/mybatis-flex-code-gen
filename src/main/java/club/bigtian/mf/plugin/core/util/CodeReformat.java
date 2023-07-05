package club.bigtian.mf.plugin.core.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class CodeReformat {

    /**
     * 重新格式化
     *
     * @param psiElement psi元素
     * @return {@code PsiElement}
     */
    public static PsiElement reformat(PsiElement psiElement) {
        PsiFile file = (PsiFile) psiElement;

//        if (file.getName().contains("xml")) {
//            return psiElement;
//        }
        Project project = psiElement.getProject();
        return CodeStyleManager.getInstance(project).reformat(psiElement);
    }
}
