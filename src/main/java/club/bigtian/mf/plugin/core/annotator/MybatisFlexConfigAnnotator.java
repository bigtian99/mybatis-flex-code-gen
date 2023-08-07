package club.bigtian.mf.plugin.core.annotator;

import club.bigtian.mf.plugin.core.function.BigFunction;
import club.bigtian.mf.plugin.core.render.SqlPreviewIconRenderer;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MybatisFlexConfigAnnotator implements Annotator {
    private Map<Integer, String> iconMap = new HashMap<>();

    private static Map<String, Function<String, String>> functionMap = new HashMap<>();
    private static Set<String> removeMethodSet = new HashSet<>();
    private static Set<String> allMethodList = new HashSet<>();
    private static Set<String> allowEndWithMethodSet = new HashSet<>();
    private static Map<String, String> methodVariableMap;
    public static Map<String, BigFunction<String[], String, String, String>> methodMap = new HashMap<>();
    private static PsiElement element;
    private static Integer lineNumber;
    private static PsiJavaFile psiJavaFile;

    static {
        functionMap.put("QueryChain.of", MybatisFlexConfigAnnotator::queryChainHandler);
        functionMap.put("QueryWrapper.create", MybatisFlexConfigAnnotator::queryChainHandler);
        functionMap.put("UpdateChain.of", MybatisFlexConfigAnnotator::updateChainHandler);
        functionMap.put("UpdateChain.create", MybatisFlexConfigAnnotator::updateChainHandler);
        methodMap.put("limit", MybatisFlexConfigAnnotator::limitHandler);
        allowEndWithMethodSet.add("on");
        try {
            initQueryChainMethodHandler();
            analysisMethod();
            initAllowEndWithMethod();
        } catch (Exception e) {

        }
    }

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        try {
            // 获取当前行号
            PsiFile containingFile = element.getContainingFile();
            if (ObjectUtil.isNull(containingFile)) {
                return;
            }
            Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(containingFile);
            if (ObjectUtil.isNull(document)) {
                return;
            }
            psiJavaFile = (PsiJavaFile) VirtualFileUtils.getPsiFile(document);

            if (ObjectUtil.isNull(document) || !document.isWritable()) {
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

            if (StrUtil.containsAny(text, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()", "query()")
                    && text.endsWith(";")) {
                if (text.contains("=")) {
                    text = StrUtil.subAfter(text, "=", false).trim();
                    // 防止用户在自己手写sql的时候，误触发
                    // if (Character.isUpperCase(trim.charAt(0))) {
                    //     text = trim;
                    // }
                }
                MybatisFlexConfigAnnotator.element = element;
                MybatisFlexConfigAnnotator.lineNumber = lineNumber;
                if (checkInBracket(text)) {
                    text = getBracketContent(text);
                }
                String key = StrUtil.subBefore(text, "(", false);
                if (StrUtil.containsAny(text, "query()", "queryChain()")) {
                    key = "QueryWrapper.create";
                }
                Function<String, String> function = functionMap.get(key);
                if (ObjectUtil.isNotNull(function)) {
                    initMethodVariable();
                    text = function.apply(text);
                }
                if (StrUtil.isEmpty(text)) {
                    return;
                }
                text = handlerVariable(text);

                if (allowEndWith(text)) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(element.getTextRange())
                            .create();
                    iconMap.put(lineNumber, StrUtil.subBefore(text, ";", true));
                    // 创建图标注解
                    AnnotationBuilder annotationBuilder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
                    annotationBuilder.gutterIconRenderer(new SqlPreviewIconRenderer(lineNumber, (PsiJavaFile) containingFile, iconMap));
                    annotationBuilder.create();
                }

            }
        } catch (PsiInvalidElementAccessException e) {

        }
    }

    private boolean allowEndWith(String text) {
        for (String methodName : allowEndWithMethodSet) {
            if (!text.contains(StrUtil.format(".{}(", methodName))) {
                continue;
            }
            String[] betweenAll = StrUtil.subBetweenAll(text, StrUtil.format(".{}(", methodName), ")");
            for (String s : betweenAll) {
                String compute = compute(text, s, "(", ")", "");
                String newKey = getKey(methodName, "?");
                String oldKey = getKey(methodName, compute);
                text = text.replace(oldKey, newKey);
            }
        }
        String methodName = StrUtil.subAfter(text, ".", true);

        methodName = StrUtil.subBefore(methodName, "(", false);
        return allowEndWithMethodSet.contains(methodName);
    }

    public static void initAllowEndWithMethod() {
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryConditionBuilder");
        Arrays.stream(psiClass.getMethods())
                .forEach(psiMethod -> {
                    allowEndWithMethodSet.add(psiMethod.getName());
                });
    }

    /**
     * 获取括号内容
     *
     * @param text 文本
     * @return {@code String}
     */
    @Nullable
    private static String getBracketContent(String text) {
        String matchText = StrUtil.sub(text, text.indexOf("(") + 1, text.lastIndexOf(")"));
        // 如果是括号里面的则不显示icon
        if (matchText != null && !StrUtil.startWithAny(text, "QueryWrapper", "QueryChain", "UpdateChain")) {
            if (matchText.startsWith("\"") || text.startsWith("//")) {
                return null;
            }
            if (matchText.contains(",")) {
                for (String key : matchText.split(",")) {
                    if (StrUtil.containsAny(key, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()")) {
                        boolean flag = getCount(key, "(") == getCount(key, ")");
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
                if (getCount(matchText, "(") == getCount(matchText, ")")) {
                    text = matchText;
                }
            }
        }
        return text;
    }

    /**
     * 检查是不是在括号里面
     *
     * @param text 文本
     * @return boolean
     */
    public boolean checkInBracket(String text) {
        String matchText = StrUtil.sub(text, text.indexOf("(") + 1, text.lastIndexOf(")"));
        if (StrUtil.containsAny(matchText, "QueryWrapper", "UpdateChain", "QueryChain", "queryChain()", "query()")) {
            return true;
        }
        return false;
    }

    /**
     * 处理程序变量
     *
     * @param text 文本
     */
    private static String handlerVariable(String text) {
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
                String compute = compute(text, s, "(", ")", "");
                String newKey = "";
                if (compute.contains(",")) {
                    Object symbol = getSymbol(compute.split(",")[0]);
                    newKey = getKey(key, symbol + ",el->true");
                } else {
                    newKey = getKey(key, getSymbol(s).toString());
                }
                String oldKey = getKey(key, compute);
                text = text.replace(oldKey, newKey);

            }
        }

        return commonReplace(text);
    }

    /**
     * 常见替换
     *
     * @param text 文本
     * @return {@code String}
     */
    public static String commonReplace(String text) {
        if (text.startsWith("return")) {
            text = StrUtil.subAfter(text, "return", false);
        }
        return text;
    }

    /**
     * 分析方法
     */
    public static void analysisMethod() {
        PsiClass psiClass = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryColumn");
        PsiClass queryWrapper = PsiJavaFileUtil.getPsiClass("com.mybatisflex.core.query.QueryWrapper");

        Arrays.stream(psiClass.getMethods())
                .forEach(psiMethod -> {
                    String name = psiMethod.getName();
                    if (StrUtil.containsIgnoreCase(name, "Null")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::nullHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "in")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::inHandler);
                    } else if (StrUtil.containsIgnoreCase(name, "between")) {
                        methodMap.put(name, MybatisFlexConfigAnnotator::betweenHandler);
                    }
                    allMethodList.add(name);
                });

        Arrays.stream(queryWrapper.getMethods())
                .forEach(psiMethod -> {
                    String name = psiMethod.getName();
                    PsiType returnType = psiMethod.getReturnType();
                    if (ObjectUtil.isNotNull(returnType)) {
                        if ("com.mybatisflex.core.query.QueryWrapper".equals(returnType.getCanonicalText())) {
                            allowEndWithMethodSet.add(name);
                        }
                    }
                    if (StrUtil.containsAnyIgnoreCase(name, "limit", "join")) {
                        allMethodList.add(name);
                        if (StrUtil.containsAnyIgnoreCase(name, "join")) {
                            methodMap.put(name, MybatisFlexConfigAnnotator::joinHandler);
                        }
                    }
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
        for (String val : betweenAll) {
            String compute = compute(sql, val, "(", ")", "");
            String[] split = compute.split(",");
            String variableValue = methodVariableMap.get(split[0]);
            String newKey = getKey(key, getSymbol(val).toString());
            if (StrUtil.isNotBlank(variableValue)) {
                newKey = getKey(key, compute.replace(split[0], handlerVariable(variableValue)) + ",el->true");
            }
            if (split.length > 1) {
                newKey = getKey(key, StrUtil.format("{},el->true", getSymbol(split[0])));
            }
            newKey = newKey.replace("\"?\"", "new Object[]{\"?\"}");
            String oldKey = getKey(key, compute);
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }


    /**
     * init方法变量
     */
    private static void initMethodVariable() {
        PsiElement parent = element.getParent();
        while (!(parent instanceof PsiMethod)) {
            parent = parent.getParent();
        }
        PsiCodeBlock body = ((PsiMethod) parent).getBody();
        methodVariableMap = Arrays.stream(body.getStatements())
                .filter(el -> {
                    if (el instanceof PsiDeclarationStatement) {
                        PsiDeclarationStatement psiDeclarationStatement = (PsiDeclarationStatement) el;
                        String text = psiDeclarationStatement.getText();
                        return StrUtil.containsAny(text, "QueryWrapper");
                    } else {
                        return false;
                    }
                })
                .map(el -> {
                    PsiDeclarationStatement psiDeclarationStatement = (PsiDeclarationStatement) el;
                    String text = psiDeclarationStatement.getText();
                    String variableName = StrUtil.subBetween(text, " ", "=");
                    String variableValue = StrUtil.subAfter(text, "=", false);
                    HashMap<String, String> valueMap = new HashMap<>();
                    if (StrUtil.isNotBlank(variableValue)) {
                        valueMap.put(variableName.trim(), variableValue.replace(";", "").trim());
                    }
                    return valueMap;
                }).collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }

    static String betweenHandler(String[] betweenAll, String sql, String key) {
        for (String s : betweenAll) {
            String compute = compute(sql, s, "(", ")", key);
            String oldKey = getKey(key, compute);
            String[] split = compute.split(",");
            int count = getCount(compute, ",");
            String newKey = getNewKey(split, count, oldKey);
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }

    static String nullHandler(String[] betweenAll, String sql, String key) {
        if (ArrayUtil.isEmpty(betweenAll)) {
            return sql;
        }
        for (String value : betweenAll) {
            String compute = compute(sql, value, "(", ")", key);
            String oldKey = getKey(key, compute);
            String newKey = StrUtil.format(".{}(el -> true)", key);
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }

    static String limitHandler(String[] betweenAll, String sql, String key) {
        for (String s : betweenAll) {
            String compute = compute(sql, s, "(", ")", key);
            String oldKey = getKey(key, compute);
            String newKey;
            newKey = getKey(key, compute.contains(",") ? "1 , 10" : "10");
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }

    static String joinHandler(String[] betweenAll, String sql, String key) {
        for (String s : betweenAll) {
            String compute = compute(sql, s, "(", ")", key);
            String oldKey = getKey(key, compute);
            String newKey = "";
            if (compute.contains(",")) {
                String s1 = compute.split(",")[0];
                newKey = getJoinNewKey(s1, key, "{}", ",true");
            } else {
                newKey = getJoinNewKey(compute, key, "{}", "");
            }
            sql = sql.replace(oldKey, newKey);
        }
        return sql;
    }

    private static String getJoinNewKey(String compute, String key, String template, String s) {
        String newKey = "";
        if (compute.startsWith("\"")) {
            newKey = getKey(key, StrUtil.format(template, compute + s));
        } else {
            String variableValue = methodVariableMap.get(compute);

            if (StrUtil.isNotBlank(variableValue)) {
                newKey = getKey(key, StrUtil.format(template, handlerVariable(variableValue) + s));
            } else if (compute.endsWith(".class")) {
                newKey = getKey(key, compute);
            } else {
                Set<String> importSet = PsiJavaFileUtil.getImportSet(psiJavaFile);
                newKey = getKey(key, StrUtil.format("\"?\"{}", s));
                for (String impor : importSet) {
                    // 为了兼容 TableDef
                    if (impor.contains(compute)) {
                        newKey = getKey(key, compute);
                        break;
                    }
                }
            }
        }
        return newKey;
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

    static String compute(String sql, String value, String leftSymbol, String rightSymbol, String... args) {
        if (!value.contains(leftSymbol)) {
            if (args.length > 0) {
                return value;
            }
            return getSymbol(value).toString();
        }
        String tmpSql = sql;
        sql = sql.replace(" ", "");
        int leftCount = getCount(value, leftSymbol);
        int rightCount = getCount(value, rightSymbol);
        if (leftCount == rightCount) {
            // 判断时候还有嵌套
            String replace = value.replace(" ", "");
            if (sql.contains(replace + ",") || sql.contains(replace + ">") || sql.contains(replace + ".")) {
                value += StrUtil.subBetween(tmpSql, value, ")");
            }
        }
        leftCount = getCount(value, leftSymbol);
        rightCount = getCount(value, rightSymbol);
        if (leftCount == rightCount) {
            if (tmpSql.contains(value)) {
                if (value.contains("{") && "(".equals(leftSymbol)) {
                    return compute(tmpSql, value, "{", "}", value);
                }
                return value;
            }
        }
        // 补偿括号
        String sqlAfter = StrUtil.subAfter(tmpSql, value, false);
        int idx = sqlAfter.indexOf(rightSymbol);
        value = value + sqlAfter.substring(0, idx + 1);

        return compute(tmpSql, value, leftSymbol, rightSymbol, value);
    }


    private static int getCount(String s, String sp) {
        int length = s.length();
        int count = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == sp.charAt(0)) {
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