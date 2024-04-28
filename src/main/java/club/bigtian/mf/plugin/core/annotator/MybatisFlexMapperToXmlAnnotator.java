package club.bigtian.mf.plugin.core.annotator;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.render.MapperlToXmlIconRenderer;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
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
import java.util.Map;
import java.util.stream.Collectors;

import static club.bigtian.mf.plugin.core.util.VirtualFileUtils.getAllResourceFiles;

public class MybatisFlexMapperToXmlAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        if (!config.isNavigationMapper()) {
            return;
        }

        Project project = element.getProject();
        // 获取当前行号
        PsiFile containingFile = element.getContainingFile();
        if (ObjectUtil.isNull(containingFile) || element instanceof PsiComment || containingFile instanceof LightVirtualFile) {
            return;
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
        PsiFile psiFile = VirtualFileUtils.getPsiFile(document);
        if (ObjectUtil.isNull(document) || !document.isWritable() || ObjectUtil.isNull(psiFile) || !(psiFile instanceof PsiJavaFile)) {
            return;
        }
        Map<String, XmlFile> allResourceFiles = getAllResourceFiles();
        PsiJavaFile javaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = javaFile.getClasses()[0];
        String qualifiedName = psiClass.getQualifiedName();
        XmlFile xmlFile = allResourceFiles.get(qualifiedName);
        if (ObjectUtil.isNull(xmlFile) || !(element instanceof PsiNameIdentifierOwner)) {
            return;
        }
        Map<String, PsiElement> collect = Arrays.stream(xmlFile.getRootTag().getChildren())
                .filter(el -> el instanceof XmlTag)
                .map(el -> (XmlTag) el)
                .filter(el -> StrUtil.equalsAnyIgnoreCase(el.getName(), "select", "insert", "update", "delete"))
                .collect(Collectors.toMap(el -> el.getAttributeValue("id"), el -> el.getAttribute("id")));
        collect.put(psiClass.getName(), xmlFile.getRootTag().getAttribute("namespace"));
        PsiElement psiElement = collect.get(((PsiNameIdentifierOwner) element).getIdentifyingElement().getText());
        if (ObjectUtil.isNull(psiElement)) {
            return;
        }
        if (element instanceof PsiMethod psiMethod) {
            element = psiMethod.getIdentifyingElement();
        }else{
            element = psiClass.getIdentifyingElement();
        }
        // 创建图标注解
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .gutterIconRenderer(new MapperlToXmlIconRenderer(psiElement))
                .create();

    }





}
