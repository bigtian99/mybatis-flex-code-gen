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
import com.intellij.psi.*;

import java.util.*;
import java.util.function.Function;

public class MybatisFlexConfigAnnotator implements Annotator {
    private Map<Integer, String> iconMap = new HashMap<>();

    private static Map<String, Function<String, String>> functionMap = new HashMap<>();
    private static Set<String> removeMethodSet = new HashSet<>();

    static {
        functionMap.put("QueryChain.of", MybatisFlexConfigAnnotator::queryChainHandler);
        functionMap.put("UpdateChain.of", MybatisFlexConfigAnnotator::updateChainHandler);
        initQueryChainMethodHandler();
    }

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        // 获取当前行号
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());
        if (ObjectUtil.isNull(document)) {
            return;
        }
        int offset = element.getTextOffset();
        int lineNumber = document.getLineNumber(offset) + 1;
        // 判断是不是 mybatis-flex 的项目
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryWrapper");
        String text = element.getText();
        if (lineNumber == 0 || ObjectUtil.isNull(psiClass) || text.startsWith("import") || iconMap.containsKey(lineNumber)) {
            return;
        }

        if (StrUtil.containsAny(text, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()")
                && text.endsWith(";")) {
            if (text.contains("=")) {
                text = StrUtil.subAfter(text, "=", false).trim();
            }
            String matchText = StrUtil.sub(text, text.indexOf("(") + 1, text.lastIndexOf(")"));
            // 如果是括号里面的则不显示icon
            if (matchText != null && !StrUtil.startWithAny(text, "QueryWrapper", "QueryChain", "UpdateChain")) {
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
            String key = StrUtil.subBefore(text, "(", false);
            Function<String, String> function = functionMap.get(key);
            if (ObjectUtil.isNotNull(function)) {
                text = function.apply(text);
            }
            iconMap.put(lineNumber, StrUtil.subBefore(text, ";", true));
            // 创建图标注解
            AnnotationBuilder annotationBuilder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            annotationBuilder.gutterIconRenderer(new SqlPreviewIconRenderer(lineNumber, (PsiJavaFile) element.getContainingFile(), iconMap));
            annotationBuilder.create();

        }
    }

    public static void initQueryChainMethodHandler() {
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryChain");
        Arrays.stream(psiClass.getMethods())
                .forEach(psiMethod -> {
                    PsiType returnType = psiMethod.getReturnType();
                    if (ObjectUtil.isNotNull(returnType)) {
                        if (!returnType.getCanonicalText().startsWith("com.mybatisflex.core")) {
                            removeMethodSet.add(psiMethod.getName()+"(");
                        }
                    }
                });
    }

    /**
     * 更新处理程序链
     *
     * @param text 文本
     * @return {@code String}
     */
    private static String updateChainHandler(String text) {
        boolean flag = StrUtil.containsAny(text,"update()", "remove()");
        if (flag) {
            return StrUtil.subBefore(text, ".", true);
        }
        return text;
    }

    /**
     * 查询处理程序链
     *
     * @param text 文本
     * @return {@code String}
     */
    private static String queryChainHandler(String text) {
        for (String method : removeMethodSet) {
            if (text.contains(method)) {
                return StrUtil.subBefore(text, "." + method, true);
            }
        }

        return text;
    }


}