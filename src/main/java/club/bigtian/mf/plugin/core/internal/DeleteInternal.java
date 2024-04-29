package club.bigtian.mf.plugin.core.internal;

import club.bigtian.mf.plugin.core.icons.Icons;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static club.bigtian.mf.plugin.core.util.VirtualFileUtils.getAllResourceFiles;

/**
 * 添加 @Params 注解
 */
public class DeleteInternal implements IntentionAction, Iconable {
    XmlFile xmlFile;
    private static final String CODE = """
             	<delete id="{}" {}>
             \t
             	</delete>
            \s""";

    @Override
    public @IntentionName @NotNull String getText() {
        return "Add Delete";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "DeleteInternal";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
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
        xmlFile = allResourceFiles.get(qualifiedName);
        if (ObjectUtil.isNull(xmlFile) || ObjectUtil.isNull(psiMethod) ) {
            return false;
        }
        Map<String, PsiElement> collect = Arrays.stream(xmlFile.getRootTag().getChildren())
                .filter(el -> el instanceof XmlTag)
                .map(el -> (XmlTag) el)
                .filter(el -> StrUtil.equalsAnyIgnoreCase(el.getName(), "select", "insert", "update", "delete"))
                .collect(Collectors.toMap(el -> el.getAttributeValue("id"), el -> el.getAttribute("id")));
        return !collect.containsKey(psiMethod.getName());
    }


    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
        if (ObjectUtil.isNull(xmlFile)) {
            return;
        }
        XmlTag delete = xmlFile.getRootTag().createChildTag("delete", null, "\n\n\t", false);
        delete.setAttribute("id", psiMethod.getName());
        // 获取返回值的全限定类名
        String paramsText = psiMethod.getParameterList().getParameter(0).getType().getCanonicalText();
        if (StrUtil.isNotEmpty(paramsText)) {
            delete.setAttribute("parameterType", paramsText);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElement element = xmlFile.getRootTag().add(delete);
            PsiElement psiElement = Arrays.stream(element.getChildren())
                    .filter(el -> el instanceof XmlText)
                    .findFirst()
                    .get();
            NavigationUtil.activateFileWithPsiElement(psiElement);
        });


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
        if (ObjectUtil.isNull(xmlFile)) {
            return IntentionPreviewInfo.EMPTY;
        }
        // 创建一个描述你的意图动作将会做什么的字符串
        String description = "This action will add a @Select annotation to the selected method.";
        SelectionModel selectionModel = editor.getSelectionModel();
        int offset = selectionModel.getSelectionStart();
        PsiElement elementAt = file.findElementAt(offset);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
        String paramsText = psiMethod.getParameterList().getParameter(0).getType().getCanonicalText();
        if (StrUtil.isNotEmpty(paramsText)) {
            paramsText = "parameterType=\"" + paramsText + "\"";
        }

        IntentionPreviewInfo diff = new IntentionPreviewInfo.CustomDiff(XmlFileType.INSTANCE,
                description,
                StrUtil.format(CODE, psiMethod.getName(), paramsText));
        return diff;
    }


}
