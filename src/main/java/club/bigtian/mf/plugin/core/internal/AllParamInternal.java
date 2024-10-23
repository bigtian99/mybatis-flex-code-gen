package club.bigtian.mf.plugin.core.internal;

import club.bigtian.mf.plugin.core.constant.QualifiedNameConstant;
import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;

import static club.bigtian.mf.plugin.core.util.PsiJavaFileUtil.methodHasAnnotation;
import static club.bigtian.mf.plugin.core.util.VirtualFileUtils.getAllResourceFiles;

/**
 * 添加 @Params 注解
 */
public class AllParamInternal implements IntentionAction, Iconable {


    @Override
    public @IntentionName @NotNull String getText() {
        return "Add the @ Param annotation to all methods of the current class";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "ParamInternal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        Map<String, XmlFile> allResourceFiles = getAllResourceFiles();
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiClass psiClass = javaFile.getClasses()[0];
        String qualifiedName = psiClass.getQualifiedName();
        XmlFile xmlFile = allResourceFiles.get(qualifiedName);
        boolean flag = Arrays.stream(javaFile.getClasses()[0].getMethods())
                .allMatch(el -> methodHasAnnotation(el, QualifiedNameConstant.MYBATIS_PARAM));
        PsiElement identifyingElement = psiClass.getIdentifyingElement();
        if (ObjectUtil.isNull(identifyingElement)) {
            return false;
        }
        TextRange range = identifyingElement.getTextRange();
        if (ObjectUtil.isNull(xmlFile) || !(elementAt instanceof PsiIdentifier) || flag || !range.contains(offset)) {
            return false;
        }
        return true;
    }


    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiMethod[] allMethods = javaFile.getClasses()[0].getMethods();
        PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
        for (PsiMethod psiMethod : allMethods) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
                // 检查是否需要导入 @Param 注解
                if (!hasAnnotation(javaFile)) {
                    javaFile.getImportList().add(PsiJavaFileUtil.createImportStatement(PsiJavaFileUtil.getPsiClass(QualifiedNameConstant.MYBATIS_PARAM)));
                }
                for (PsiParameter parameter : parameters) {
                    // 检查参数是否已经有 @Param 注解
                    PsiAnnotation annotation = parameter.getAnnotation(QualifiedNameConstant.MYBATIS_PARAM);
                    if (annotation == null) {
                        // 创建新的 @Param 注解
                        PsiAnnotation paramAnnotation = psiElementFactory.createAnnotationFromText("@Param(\"" + parameter.getName() + "\")", null);
                        // 在参数前添加注解
                        parameter.getModifierList().addBefore(paramAnnotation, null);
                    }
                }
            });
        }

    }

    private static boolean hasAnnotation(PsiJavaFile javaFile) {
        return PsiJavaFileUtil.getQualifiedNameImportSet(javaFile).contains(QualifiedNameConstant.MYBATIS_PARAM);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override

    public Icon getIcon(int flags) {
        return Icons.FLEX; // Replace with your custom icon
    }


    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PsiJavaFile javaFile = (PsiJavaFile) file;
        String description = "This action will add a @Select annotation to the selected method.";
        String classText = javaFile.getText();
        PsiMethod[] allMethods = javaFile.getClasses()[0].getMethods();
        for (PsiMethod psiMethod : allMethods) {
            String methodText = psiMethod.getText();
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            for (PsiParameter parameter : parameters) {
                PsiAnnotation annotation = parameter.getAnnotation(QualifiedNameConstant.MYBATIS_PARAM);
                if (ObjectUtil.isNull(annotation)) {
                    methodText = methodText.replace(parameter.getText(), "@Param(\"" + parameter.getName() + "\") " + parameter.getText());
                }
            }
            if(psiMethod.getText().equals(methodText)){
                methodText = "";
            }
            classText = classText.replace(psiMethod.getText(),methodText);
        }

        IntentionPreviewInfo diff = new IntentionPreviewInfo.CustomDiff(JavaFileType.INSTANCE,
                description,
                classText);
        return diff;
    }

}
