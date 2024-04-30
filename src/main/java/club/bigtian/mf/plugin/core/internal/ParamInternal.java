package club.bigtian.mf.plugin.core.internal;

import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.constant.QualifiedNameConstant;
import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.util.PluginUtil;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.StringJoiner;

import static club.bigtian.mf.plugin.core.util.PsiJavaFileUtil.methodHasAnnotation;
import static club.bigtian.mf.plugin.core.util.VirtualFileUtils.getAllResourceFiles;

/**
 * 添加 @Params 注解
 */
public class ParamInternal implements IntentionAction, Iconable {

    @Override
    public @IntentionName @NotNull String getText() {
        return "Add @Param";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "ParamInternal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof PsiJavaFile)|| PluginUtil.isConflictPluginInstalled(MybatisFlexConstant.MYBATIS_PLUGIN_ID)) {
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
        if (ObjectUtil.isNull(xmlFile) || ObjectUtil.isNull(psiMethod) || methodHasAnnotation(psiMethod, QualifiedNameConstant.MYBATIS_PARAM)) {
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
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
        WriteCommandAction.runWriteCommandAction(project, () -> {
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

    private static boolean hasAnnotation(PsiJavaFile javaFile) {
        return PsiJavaFileUtil.getQualifiedNameImportSet(javaFile).contains(QualifiedNameConstant.MYBATIS_PARAM);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override

    public Icon getIcon(int flags) {
        return Icons.FLEX;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {

        // 创建一个描述你的意图动作将会做什么的字符串
        String description = "This action will add a @Select annotation to the selected method.";
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        StringJoiner previewText = new StringJoiner(",");
        for (PsiParameter parameter : parameters) {
            StringBuilder builder = new StringBuilder().append("@Param(\"").append(parameter.getName())
                    .append("\") ").append(parameter.getType().getPresentableText())
                    .append(" " + parameter.getName());
            previewText.add(builder.toString());
        }
        String result = ReUtil.replaceAll(psiMethod.getText(), "\\(.*?\\)", StrUtil.format("({})", previewText.toString()));

        IntentionPreviewInfo diff = new IntentionPreviewInfo.CustomDiff(JavaFileType.INSTANCE,
                description,
                result);
        return diff;
    }

}
