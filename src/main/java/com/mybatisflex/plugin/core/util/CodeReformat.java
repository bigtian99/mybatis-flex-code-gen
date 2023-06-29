package com.mybatisflex.plugin.core.util;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.psi.xml.XmlFile;

public class CodeReformat {

    /**
     * 重新格式化
     *
     * @param psiElement psi元素
     * @return {@code PsiElement}
     */
    public static PsiElement reformat(PsiElement psiElement) {
        PsiFile file = (PsiFile) psiElement;

        if (file.getName().contains("xml")) {
            return psiElement;
        }
        Project project = psiElement.getProject();
        return CodeStyleManager.getInstance(project).reformat(psiElement);
    }
}
