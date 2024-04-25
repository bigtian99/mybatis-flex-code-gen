package club.bigtian.mf.plugin.core.annotator;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.render.XmlToMapperIconRenderer;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MybatisFlexXmlToMapperAnnotator implements Annotator {
    Map<String, PsiElement> elementMap = new HashMap<>();

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        if (!config.isNavigationMapper()) {
            return;
        }

        Project project = element.getProject();
        // 获取当前行号
        PsiFile containingFile = element.getContainingFile();
        if (ObjectUtil.isNull(containingFile) || containingFile instanceof LightVirtualFile) {
            return;
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
        PsiFile psiFile = VirtualFileUtils.getPsiFile(document);
        if (ObjectUtil.isNull(document) || !(element instanceof XmlTag) || !document.isWritable() || ObjectUtil.isNull(psiFile) || !(psiFile instanceof XmlFile)) {
            return;
        }
        XmlFile xmlFile = (XmlFile) psiFile;
        XmlTag rootTag = xmlFile.getDocument().getRootTag();
        String namespace = rootTag.getAttributeValue("namespace");


        if (StrUtil.isBlank(namespace)) {
            return;
        }
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass(namespace);
        elementMap.put(namespace + "." + null, psiClass.getIdentifyingElement());
        @NotNull PsiElement[] children = rootTag.getChildren();
        Arrays.stream(children)
                .filter(el -> el instanceof XmlTag)
                .map(el -> (XmlTag) el)
                .filter(el -> StrUtil.equalsAnyIgnoreCase(el.getName(), "select", "insert", "update", "delete"))
                .forEach(el -> {
                    String id = el.getAttributeValue("id");
                    PsiMethod[] methods = psiClass.findMethodsByName(id, false);
                    if (ArrayUtil.isNotEmpty(methods)) {
                        elementMap.put(namespace + "." + id, methods[0]);
                    }
                });
        XmlTag tag = (XmlTag) element;
        PsiElement psiElement = elementMap.get(namespace + "." + tag.getAttributeValue("id"));
        if (ObjectUtil.isNull(psiElement) || !ArrayUtil.containsAny(children, element) && !tag.getName().equals("mapper")) {
            return;
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .gutterIconRenderer(new XmlToMapperIconRenderer(psiElement))
                .create();


    }

    public int getLineNumber(XmlTag xmlTag) {
        Project project = xmlTag.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(xmlTag.getContainingFile());

        if (document != null) {
            int textOffset = xmlTag.getTextOffset();
            return document.getLineNumber(textOffset) + 1; // Line numbers are 0-based, so we add 1
        }

        return -1; // Return -1 or throw an exception if the document is null
    }
}
