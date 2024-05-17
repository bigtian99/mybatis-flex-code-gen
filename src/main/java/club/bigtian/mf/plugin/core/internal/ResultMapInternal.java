package club.bigtian.mf.plugin.core.internal;

import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.constant.QualifiedNameConstant;
import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.util.PluginUtil;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.core.util.XmlFileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 添加 @Params 注解
 */
public class ResultMapInternal implements IntentionAction, Iconable {
    XmlFile xmlFile;

    @Override
    public @IntentionName @NotNull String getText() {
        return "FieldSynchronizationToResultMap";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "字段同步到ResultMap";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof PsiJavaFile) || PluginUtil.isConflictPluginInstalled(MybatisFlexConstant.MYBATIS_PLUGIN_ID)) {
            return false;
        }
        PsiJavaFile javaFile = (PsiJavaFile) file;
        ReferencesSearch.SearchParameters searchParameters = new ReferencesSearch.SearchParameters(javaFile.getClasses()[0], GlobalSearchScope.allScope(project), false);
        PsiReference[] references = ReferencesSearch.search(searchParameters).findAll().toArray(PsiReference.EMPTY_ARRAY);
        Optional<PsiFile> xmlFile = Arrays.stream(references)
                .map(PsiReference::getElement)
                .filter(element -> element.getText().contains("."))
                .map(PsiElement::getContainingFile)
                .filter(Objects::nonNull)
                .filter(el -> el.getFileType() instanceof XmlFileType)
                .findFirst();
        xmlFile.ifPresent(el -> this.xmlFile = (XmlFile) el);
        return xmlFile.isPresent();
    }


    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiClass psiClass = javaFile.getClasses()[0];
        String qualifiedName = psiClass.getQualifiedName();
        Map<String, PsiField> fieldMap = Arrays.stream(psiClass.getFields())
                .filter(el -> !el.hasModifierProperty(PsiModifier.STATIC))
                .collect(Collectors.toMap(PsiField::getName, Function.identity()));


        Map<XmlTag, List<String>> resultMapFieldMap = XmlFileUtil.getResultMaps(xmlFile).stream()
                .filter(el -> el.getAttributeValue("type").equals(qualifiedName) || el.getAttributeValue("type").equals(psiClass.getName()))
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.flatMapping(tag -> Arrays.stream(tag.getSubTags())
                                        .map(el -> el.getAttributeValue("property")),
                                Collectors.toList())));

        for (Map.Entry<String, PsiField> entry : fieldMap.entrySet()) {
            resultMapFieldMap.forEach((key, value) -> {
                if (!value.contains(entry.getKey())) {
                    XmlTag result = xmlFile.getRootTag().createChildTag("result", null, null, false);
                    PsiField psiField = entry.getValue();
                    PsiAnnotation annotation = psiField.getAnnotation("com.baomidou.mybatisplus.annotation.TableField");
                    String column = "";
                    if (ObjectUtil.isNotNull(annotation)) {
                        PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue("value");
                        column = (String) ((PsiLiteralExpression) attributeValue).getValue();
                    } else {
                        column = StrUtil.toUnderlineCase(entry.getKey());
                    }
                    result.setAttribute("column", column);
                    result.setAttribute("property", entry.getKey());
                    System.out.println(result.getText());
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
        return Icons.FLEX;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {

        // 创建一个描述你的意图动作将会做什么的字符串
        String description = "This action will add a @Select annotation to the selected method.";

        IntentionPreviewInfo diff = new IntentionPreviewInfo.CustomDiff(JavaFileType.INSTANCE,
                description,
                "");
        return diff;
    }

}
