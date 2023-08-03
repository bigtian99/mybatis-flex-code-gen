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
        try {
            initQueryChainMethodHandler();
            analysisMethod();
        } catch (Exception e) {

        }
    }

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        try {
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
                                boolean flag = getCount(key, '(') == getCount(key, ')');
                                if (flag) {
                                    if (key.endsWith(")")) {
                                        text = key;
                                    } else {
                                        text = matchText;
                                    }
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
                if (text.contains("queryChain()")) {
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
        } catch (PsiInvalidElementAccessException e) {

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
                int count = 0;
                count = getCount(s, '(');
                if (count > 0) {
                    s = getString(text, s, count);
                }

                s = compensate(text, s);
                if (text.contains(s + ",")) {
                    String last = StrUtil.subAfter(text, s, false);
                    s += last.substring(0, last.indexOf(")"));
                }
                String oldKey = getOldKey(key, s);
                Object symbol;
                if (!s.startsWith("\"")) {
                    try {
                        if(s.contains(",")){
                            symbol = Long.valueOf(s.split(",")[0]);
                        }else{
                            symbol = Long.valueOf(s);
                        }
                    } catch (NumberFormatException e) {
                        symbol = "?";
                    }
                } else {
                    if(s.contains(",")){
                        s =s.split(",")[0];
                    }
                    symbol = s.replace("\"", "");

                }
                String newKey = StrUtil.format(".{}(\"{}\")", key, symbol);
                // if (s.contains(".")) {
                //     newKey += ")";
                // }
                if (oldKey.contains("(")) {
                    // text = text.replace(oldKey+")", StrUtil.format(".{}(\"{})\")", key, s));
                    if (!(symbol instanceof Long)) {
                        text = text.replace(oldKey, newKey);
                    }
                    continue;
                }
                // text = text.replace(oldKey, StrUtil.format(".{}(\"{}\")", key, s));
                text = text.replace(oldKey, newKey);
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
        String tempSql=sql;
        sql = sql.replace(" ", "");
        for (String s : betweenAll) {
            String replace = "";
            int count = 0;
            count = getCount(s, '(');
            if (count > 0) {
                s = getString(sql, s, count);
            }

            s = compensate(sql, s);
            if (sql.contains(s + ",")) {
                String last = StrUtil.subAfter(sql, s, false);
                s += last.substring(0, last.indexOf(")"));
            }

            if (s.startsWith("(") && s.endsWith(")")) {
                int endIndex = s.lastIndexOf(")");
                int beginIndex = s.indexOf("(");
                s = s.substring(beginIndex + 1, endIndex);

            }
            if (s.contains(",")) {
                String[] split = s.split("\\),");

                String s1 = split[0] + ")";
                if(s1.contains(",")){
                    s1 = s.split(",")[0];
                }
                if (!s1.startsWith("\"")) {
                    replace += StrUtil.format("\"{}\"", s.startsWith("\"")?s1:"?");
                } else {
                    replace = s1;
                }
                replace = replace + " , el -> true";
            } else {
                if (!s.startsWith("\"")) {
                    replace = StrUtil.format("\"?\"", s);
                } else {
                    replace = s;
                }
            }
            if (StrUtil.isNotBlank(s) && StrUtil.isNotBlank(replace)) {
                tempSql = tempSql.replace(s, replace);
            }

        }
        return tempSql;
    }


    private static String compensate(String sql, String s) {
        while (true) {
            int count1 = getCount(s, '(');
            int count2 = getCount(s, ')');
            if (count1 != count2) {
                s = getString(sql, s, count1 - count2);
            } else {
                break;
            }
        }
        return s;
    }

    private static String getString(String sql, String s, int count) {
        String last = StrUtil.subAfter(sql, s, false);
        for (int i = 0; i < count; i++) {
            int i1 = last.indexOf(")");
            if (i1 != -1) {
                s += last.substring(0, i1 + 1);
                last = last.substring(i1 + 1);
            }
        }
        return s;
    }

    private static int getCount(String s, char sp) {
        int length = s.length();
        int count = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == sp) {
                count++;
            }
        }
        return count;
    }

    /**
     * 更新处理程序链
     *
     * @param text 文本
     * @return {@code String}
     */
    private static String updateChainHandler(String text) {
        boolean flag = StrUtil.containsAny(text, "update()", "remove()", "toSQL()");
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