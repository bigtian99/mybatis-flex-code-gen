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
import org.jetbrains.annotations.NotNull;

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
                if (StrUtil.isEmpty(text)) {
                    return;
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
                String compute = compute(text, s, key);
                String newKey = "";
                if (compute.contains(",")) {
                    Object symbol = getSymbol(compute.split(",")[0]);
                    newKey = getKey(key,  symbol + ",el->true");
                }
                else {
                    newKey =getKey( key, getSymbol(s).toString());
                }
                String oldKey = getKey(key,compute);
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
                        // methodMap.put(name, MybatisFlexConfigAnnotator::likeHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "Null")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::nullHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "in")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::inHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "between")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::betweenHandler);
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

    static String betweenHandler(String[] betweenAll, String sql, String key) {
        for (String s : betweenAll) {
            String compute = compute(sql, s, key);
            String oldKey = getKey(key,compute);
            String[] split = compute.split(",");
            int count = getCount(compute, ',');
            String newKey = getNewKey(split, count, oldKey);
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }

    private static String getNewKey(String[] split, int count, String oldKey) {
        for (int i = 0; i < split.length; i++) {
            String string = split[i].trim();
            boolean flag = string.startsWith("\"");
            if (!flag) {
                Object symbol = getSymbol(string);
                if (i != split.length - 1 || count < 2) {
                    oldKey = oldKey.replace(string, symbol.toString());
                    continue;
                }
                oldKey = oldKey.replace(string, "el -> true");
            }
        }
        return oldKey;
    }

    @NotNull
    private static Object getSymbol(String string) {
        Object symbol;
        if (string.startsWith("\"")) {
            return string;
        }
        try {
            Long value = Long.valueOf(string);
            symbol = value;
        } catch (NumberFormatException e) {
            symbol = "\"?\"";
        }
        return symbol;
    }

    static String compute(String sql, String value, String key) {
        if (!value.contains("(")) {
            // return StrUtil.format(".{}({})", key, getSymbol(value));
            return getSymbol(value).toString();
        }
        String tmpSql = sql;
        sql = sql.replace(" ", "");
        int leftCount = getCount(value, '(');
        int rightCount = getCount(value, ')');
        if (leftCount == rightCount) {
            // 判断时候还有嵌套
            if (sql.contains(value + ",")) {
                value += StrUtil.subBetween(tmpSql, value, ")");
            }
        }
        leftCount = getCount(value, '(');
        rightCount = getCount(value, ')');
        if (leftCount == rightCount) {
            if (tmpSql.contains(value)) {
                return value;
            }
        }
        // 补偿括号
        String sqlAfter = StrUtil.subAfter(sql, value, false);
        int idx = sqlAfter.indexOf(")");
        value = value + sqlAfter.substring(0, idx + 1);

        return compute(tmpSql, value, value);
    }

    static String nullHandler(String[] betweenAll, String sql, String key) {
        if (ArrayUtil.isEmpty(betweenAll)) {
            return sql;
        }
        for (String value : betweenAll) {
            String oldKey = getKey(key, value);
            sql = sql.replace(oldKey, StrUtil.format(".{}(el -> true)", key));
        }
        return sql;
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

    private static String getKey(String key, String s) {
        return StrUtil.format(".{}({})", key, s);
    }
}