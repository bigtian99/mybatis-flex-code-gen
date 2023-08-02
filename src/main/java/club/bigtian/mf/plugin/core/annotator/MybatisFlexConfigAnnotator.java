package club.bigtian.mf.plugin.core.annotator;

import club.bigtian.mf.plugin.core.function.BigFunction;
import club.bigtian.mf.plugin.core.render.SqlPreviewIconRenderer;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import cn.hutool.core.util.ArrayUtil;
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
    private static Set<String> allMethodList = new HashSet<>();
    public static Map<String, BigFunction<String[], String, String, String>> methodMap = new HashMap<>();


    static {
        functionMap.put("QueryChain.of", MybatisFlexConfigAnnotator::queryChainHandler);
        functionMap.put("QueryWrapper.create", MybatisFlexConfigAnnotator::queryChainHandler);
        functionMap.put("UpdateChain.of", MybatisFlexConfigAnnotator::updateChainHandler);
        functionMap.put("UpdateChain.create", MybatisFlexConfigAnnotator::updateChainHandler);
        initQueryChainMethodHandler();
        analysisMethod();
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
                String trim = StrUtil.subAfter(text, "=", false).trim();
                // 防止用户在自己手写sql的时候，误触发
                if (Character.isUpperCase(trim.charAt(0))) {
                    text = trim;
                }
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
                            if (key.endsWith(")")) {
                                text = key;
                            } else {
                                text = matchText;
                            }
                            break;
                        }
                    }
                } else {
                    text = matchText;
                }
            }
            String key = StrUtil.subBefore(text, "(", false);
            if(text.contains("queryChain()")){
                key = "QueryWrapper.create";
            }
            Function<String, String> function = functionMap.get(key);
            if (ObjectUtil.isNotNull(function)) {
                text = function.apply(text);
            }
            text = handlerVariable(text);
            iconMap.put(lineNumber, StrUtil.subBefore(text, ";", true));
            // 创建图标注解
            AnnotationBuilder annotationBuilder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            annotationBuilder.gutterIconRenderer(new SqlPreviewIconRenderer(lineNumber, (PsiJavaFile) element.getContainingFile(), iconMap));
            annotationBuilder.create();

        }
    }

    /**
     * 处理程序变量
     *
     * @param text 文本
     */
    private String handlerVariable(String text) {
        for (String key : allMethodList) {
            if (!text.contains(StrUtil.format(".{}(", key))) {
                continue;
            }
            BigFunction<String[], String, String, String> function = methodMap.get(key);
            String[] betweenAll = StrUtil.subBetweenAll(text, StrUtil.format(".{}(", key), ")");

            if (ObjectUtil.isNotNull(function)) {
                // 如果有单独的方法来处理，就不走通用的处理逻辑
                text = function.apply(betweenAll, text, key);
                continue;
            }
            for (String s : betweenAll) {
                String oldKey = getOldKey(key, s);
                if (oldKey.contains("(")) {
                    // text = text.replace(oldKey+")", StrUtil.format(".{}(\"{})\")", key, s));
                    text = text.replace(oldKey + ")", StrUtil.format(".{}(\"{}\"))", key, "?"));
                    continue;
                }
                // text = text.replace(oldKey, StrUtil.format(".{}(\"{}\")", key, s));
                text = text.replace(oldKey, StrUtil.format(".{}(\"{}\"))", key, "?"));
            }
        }
        return text;
    }

    /**
     * 分析方法
     */
    public static void analysisMethod() {
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryColumn");
        if (ObjectUtil.isNull(psiClass)) {
            return;
        }
        Arrays.stream(psiClass.getMethods())
                .forEach(psiMethod -> {
                    String name = psiMethod.getName();
                    if (StrUtil.containsIgnoreCase(name, "like")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::likeHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "Null")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::nullHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "in")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::inHandler);
                    }

                    allMethodList.add(name);
                });

    }

    /**
     * 初始化查询处理程序链方法
     */
    public static void initQueryChainMethodHandler() {
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryChain");
        Arrays.stream(psiClass.getMethods())
                .forEach(psiMethod -> {
                    PsiType returnType = psiMethod.getReturnType();
                    if (ObjectUtil.isNotNull(returnType)) {
                        if (!returnType.getCanonicalText().startsWith("com.mybatisflex.core")) {
                            removeMethodSet.add(psiMethod.getName() + "(");
                        }
                    }
                });
    }

    static String inHandler(String[] betweenAll, String sql, String key) {
        return null;
    }

    static String nullHandler(String[] betweenAll, String sql, String key) {
        if (ArrayUtil.isEmpty(betweenAll)) {
            return sql;
        }
        for (String value : betweenAll) {
            String oldKey = getOldKey(key, value);
            sql = sql.replace(oldKey, StrUtil.format(".{}(el -> true)", key));
        }
        return sql;
    }

    static String likeHandler(String[] betweenAll, String sql, String key) {
        for (String s : betweenAll) {
            String replace = "";
            if (s.contains(",")) {
                String[] split = s.split(",");
                String s1 = split[0];
                if (!s1.startsWith("\"")) {
                    replace += "\"?\"";
                } else {
                    replace = s1;
                }
                replace += " , el -> true";
            } else {
                if (!s.startsWith("\"")) {
                    replace = StrUtil.format("\"{}\"", s);
                } else {
                    replace = s;
                }
            }
            sql = sql.replace(s, replace);

        }
        return sql;
    }

    /**
     * 更新处理程序链
     *
     * @param text 文本
     * @return {@code String}
     */
    private static String updateChainHandler(String text) {
        boolean flag = StrUtil.containsAny(text, "update()", "remove()","toSQL()");
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

    private static String getOldKey(String key, String s) {
        String oldKey = StrUtil.format(".{}({})", key, s);
        return oldKey;
    }
}