package club.bigtian.mf.plugin.core.annotator;

import club.bigtian.mf.plugin.core.render.SqlPreviewIconRenderer;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;

import java.util.HashMap;
import java.util.Map;

public class MybatisFlexConfigAnnotator implements Annotator {
    private Map<Integer, String> iconMap = new HashMap<>();

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        // 获取当前行号
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        int offset = element.getTextOffset();
        int lineNumber = document.getLineNumber(offset) + 1;
        //只有是
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryWrapper");
        if (lineNumber == 0 || ObjectUtil.isNull(psiClass)) {
            return;
        }

        String text = element.getText();
        if (StrUtil.containsAny(text, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()")
                && text.endsWith(";") && !text.startsWith("import") && !iconMap.containsKey(lineNumber)) {
            String matchText = StrUtil.sub(text, text.indexOf("(") + 1, text.lastIndexOf(")"));
            //如果是括号里面的则不显示icon
            if (matchText != null) {
                if (matchText.startsWith("\"") || text.startsWith("//")) {
                    return;
                }
                if (matchText.contains(",")) {
                    for (String key : matchText.split(",")) {
                        if (StrUtil.containsAny(key, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()")) {
                            text = key;
                            break;
                        }
                    }

                } else {
                    text = matchText;
                }

            }

            iconMap.put(lineNumber, text);
            // 创建图标注解
            AnnotationBuilder annotationBuilder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            annotationBuilder.gutterIconRenderer(new SqlPreviewIconRenderer(lineNumber, (PsiJavaFile) element.getContainingFile(), iconMap));
            annotationBuilder.create();

        }
    }


}