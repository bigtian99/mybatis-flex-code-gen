package club.bigtian.mf.plugin.core.contributor;

import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
import club.bigtian.mf.plugin.entity.MybatisFlexConfgInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mybatis-flex.config 补全提示
 * Author: BigTian
 */
public class MybatisFlexConfigCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(MybatisFlexConfigCompletionContributor.class);
    PsiElementFactory elementFactory;
    JavaPsiFacade psiFacade;
    PsiManager psiManager;
    public static Map<String, MybatisFlexConfgInfo> CONFIG_MAP = new ConcurrentHashMap<>();

    static {
        CONFIG_MAP.put("processor.enable=", new MybatisFlexConfgInfo(Arrays.asList("true", "false"), "全局启用apt开关"));
        CONFIG_MAP.put("processor.stopBubbling=", new MybatisFlexConfgInfo(Arrays.asList("true", "false"), "是否停止向上级合并配"));
        CONFIG_MAP.put("processor.genPath=", new MybatisFlexConfgInfo(Arrays.asList(""), "APT 代码生成路径"));
        CONFIG_MAP.put("processor.charset=", new MybatisFlexConfgInfo(Arrays.asList(""), "APT 代码生成文件字符集"));
        CONFIG_MAP.put("processor.allInTables.enable=", new MybatisFlexConfgInfo(Arrays.asList("true", "false"), "是否所有的类都生成在 Tables 类里"));
        CONFIG_MAP.put("processor.allInTables.package=", new MybatisFlexConfgInfo(Arrays.asList(""), "Tables 包名"));
        CONFIG_MAP.put("processor.allInTables.className=", new MybatisFlexConfgInfo(Arrays.asList(""), "Tables 类名"));
        CONFIG_MAP.put("processor.mapper.generateEnable=", new MybatisFlexConfgInfo(Arrays.asList("true", "false"), "开启 Mapper 自动生成"));
        CONFIG_MAP.put("processor.mapper.annotation=", new MybatisFlexConfgInfo(Arrays.asList("true", "false"), "开启 @Mapper 注解"));
        CONFIG_MAP.put("processor.mapper.baseClass=", new MybatisFlexConfgInfo(Arrays.asList(""), "自定义 Mapper 的父类"));
        CONFIG_MAP.put("processor.mapper.package=", new MybatisFlexConfgInfo(Arrays.asList(""), "自定义 Mapper 生成的包名"));
        CONFIG_MAP.put("processor.tableDef.propertiesNameStyle=", new MybatisFlexConfgInfo(Arrays.asList("upperCase", "lowerCase", "upperCamelCase", "lowerCamelCase"), "生成辅助类的字段风格"));
        CONFIG_MAP.put("processor.tableDef.instanceSuffix=", new MybatisFlexConfgInfo(Arrays.asList(""), "生成的表对应的变量后缀"));
        CONFIG_MAP.put("processor.tableDef.classSuffix=", new MybatisFlexConfgInfo(Arrays.asList(""), "生成的 TableDef 类的后缀"));
        CONFIG_MAP.put("processor.tableDef.ignoreEntitySuffixes=", new MybatisFlexConfgInfo(Arrays.asList(""), "过滤 Entity 后缀"));
    }


    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        try {
            Editor editor = parameters.getEditor();
            Document document = editor.getDocument();
            VirtualFile file = VirtualFileUtils.getVirtualFile(document);
            boolean flag = file.getName().equals("mybatis-flex.config");
            // 如果不是 mybatis-flex.config 文件，直接返回
            if (!flag) {
                return;
            }
            Project project = parameters.getPosition().getProject();
            if (ObjectUtil.isNull(elementFactory)) {
                elementFactory = JavaPsiFacade.getElementFactory(project);
                psiFacade = JavaPsiFacade.getInstance(project);
                psiManager = PsiManager.getInstance(project);
            }

            // 获取已经存在的配置,存在的配置不再提示
            List<String> existConfigList = getExistConfig(document);
            // 获取当前光标位置
            LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();
            // 获取当前光标位置的文本信息
            int line = logicalPosition.line;
            String text = editor.getDocument().getText(new TextRange(line, document.getLineEndOffset(line)));
            // 如果包含 = ，则提示 value
            if (text.contains("=")) {
                text = text.substring(0, text.lastIndexOf("=") + 1);
                MybatisFlexConfgInfo confgInfo = CONFIG_MAP.get(text);
                if (ObjectUtil.isNotNull(confgInfo)) {
                    addCodeTip(result, confgInfo.getValue());
                    return;
                }
            }
            // 添加代码提示
            addCodeTip(result, existConfigList, text);
        } catch (PsiInvalidElementAccessException e) {
        }
    }

    /**
     * 获取已经存在的配置
     *
     * @param document
     * @return
     */
    private List<String> getExistConfig(Document document) {
        List<String> existConfigList = new ArrayList<>();
        String text = document.getText();
        for (String configKey : CONFIG_MAP.keySet()) {
            if (text.contains(configKey)) {
                existConfigList.add(configKey);
            }
        }
        return existConfigList;
    }

    /**
     * 添加代码提示(值)
     *
     * @param result
     * @param list
     */
    private void addCodeTip(@NotNull CompletionResultSet result, List<String> list) {
        if (list.size() == 1) {
            return;
        }
        for (String key : list) {
            // 添加补全提示
            LookupElement lookupElement = LookupElementBuilder.create(key)
                    .withIcon(Icons.FLEX);
            result.addElement(lookupElement);
        }


    }

    /**
     * 添加代码提示
     *
     * @param result          结果
     * @param existConfigList
     * @param text
     */
    private void addCodeTip(@NotNull CompletionResultSet result, List<String> existConfigList, String text) {
        // 获取忽略大小写的结果集
        CompletionResultSet completionResultSet = result.caseInsensitive();
        String prefix = completionResultSet.getPrefixMatcher().getPrefix();
        if (StrUtil.isEmpty(prefix)) {
            return;
        }
        for (Map.Entry<String, MybatisFlexConfgInfo> entry : CONFIG_MAP.entrySet()) {
            String key = entry.getKey();
            if (existConfigList.contains(key)) {
                continue;
            }
            MybatisFlexConfgInfo confgInfo = entry.getValue();
            confgInfo.getValue().forEach(el -> {
                // 添加补全提示
                LookupElement lookupElement = LookupElementBuilder.create(key + el)
                        .withTypeText(confgInfo.getDescription())
                        .withInsertHandler((context, item) -> {
                            int tailOffset = context.getTailOffset();
                            // 如果不包含 #，则添加注释
                            if (!text.contains("#")) {
                                context.getDocument().insertString(tailOffset, " # " + confgInfo.getDescription());
                            }
                            if (confgInfo.getValue().size() > 1) {
                                context.getEditor().getCaretModel().moveToOffset(tailOffset + key.length() + 3);
                            }
                        })
                        .withIcon(Icons.FLEX);
                completionResultSet.addElement(lookupElement);
            });

        }
    }
}
