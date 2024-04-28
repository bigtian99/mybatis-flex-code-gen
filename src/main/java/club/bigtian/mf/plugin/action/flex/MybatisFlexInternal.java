package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

import static club.bigtian.mf.plugin.core.util.VirtualFileUtils.getAllResourceFiles;

public class MybatisFlexInternal implements IntentionAction, Iconable {


    @Override
    public @IntentionName @NotNull String getText() {
        return "Add @Params";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "MybatisFlexInternal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if(!(file instanceof PsiJavaFile)){
            return false;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);

        Map<String, XmlFile> allResourceFiles = getAllResourceFiles();
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiClass psiClass = javaFile.getClasses()[0];
        String qualifiedName = psiClass.getQualifiedName();
        XmlFile xmlFile = allResourceFiles.get(qualifiedName);
        if (ObjectUtil.isNull(xmlFile)|| ObjectUtil.isNull(psiMethod)) {
            return false;
        }

        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (!PsiJavaFileUtil.getQualifiedNameImportSet(javaFile).contains("org.apache.ibatis.annotations.Param")) {
                javaFile.getImportList().add(PsiJavaFileUtil.createImportStatement(PsiJavaFileUtil.getPsiClass("org.apache.ibatis.annotations.Param")));
            }

            for (PsiParameter parameter : parameters) {
                PsiAnnotation annotation = parameter.getAnnotation("org.apache.ibatis.annotations.Param");
                if (ObjectUtil.isNull(annotation)) {
                    PsiAnnotation paramAnnotation = JavaPsiFacade.getElementFactory(project).createAnnotationFromText("@Param(\"" + parameter.getName() + "\")", null);
                    parameter.addBefore(paramAnnotation, parameter.getFirstChild());
                }
            }

        });
        // for (PsiParameter parameter : parameters) {
        //     PsiAnnotation annotation = parameter.getAnnotation("org.apache.ibatis.annotations.Param");
        //     if (ObjectUtil.isNull(annotation)) {
        //         PsiAnnotation paramAnnotation = JavaPsiFacade.getElementFactory(project).createAnnotationFromText("@Param(\"" + parameter.getName() + "\")", null);
        //         WriteCommandAction.runWriteCommandAction(project, () -> {
        //             if (!PsiJavaFileUtil.getQualifiedNameImportSet(javaFile).contains("org.apache.ibatis.annotations.Param")) {
        //
        //                 javaFile.getImportList().add(PsiJavaFileUtil.createImportStatement(PsiJavaFileUtil.getPsiClass("org.apache.ibatis.annotations.Param")));
        //             }
        //
        //             parameter.addBefore(paramAnnotation, parameter.getFirstChild());
        //         });
        //     }
        // }

    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override

    public Icon getIcon(int flags) {
        return Icons.FLEX; // Replace with your custom icon
    }
}
