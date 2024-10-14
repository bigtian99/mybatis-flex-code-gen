package club.bigtian.mf.plugin.core.contributor;

import club.bigtian.mf.plugin.core.icons.Icons;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.entity.Variable;
import club.bigtian.mf.plugin.windows.MybatisFlexSettingDialog;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * tabDef补全提示
 *
 * @author BigTian
 */
public class MybatisFlexTemplateCompletionContributor extends CompletionContributor {

    public static Map<String, String> TEMPLATE_MAP = new ConcurrentHashMap<>();

    public static void   removeTemplateMap(String key){
        TEMPLATE_MAP.remove(key);
    }

    static {
        TEMPLATE_MAP.put("$config.controllerPackage", "controller 生成包名");
        TEMPLATE_MAP.put("$config.interfacePackage", "service 接口生成包名");
        TEMPLATE_MAP.put("$interfaceName", "service 文件名");
        TEMPLATE_MAP.put("$config.modelPackage", "实体类生成包名");
        TEMPLATE_MAP.put("$modelName", "实体类名");
        TEMPLATE_MAP.put("$qualifiedName", "统一返回对象全路径");
        TEMPLATE_MAP.put("$config.swagger", "开启 swagger支持");
        TEMPLATE_MAP.put("$config.swagger3", "开启 swagger3支持");
        TEMPLATE_MAP.put("$table.comment", "表备注");
        TEMPLATE_MAP.put("$author", "作者");
        TEMPLATE_MAP.put("$since", "版本");
        TEMPLATE_MAP.put("$requestPath", "请求地址");
        TEMPLATE_MAP.put("$controllerName", "controller类名");
        TEMPLATE_MAP.put("$interfaceVariable", "service 变量名");
        TEMPLATE_MAP.put("$table.columnList", "表字段列表");
        TEMPLATE_MAP.put("$column.fieldName", "java 字段名");
        TEMPLATE_MAP.put("$column.comment", "表字段备注");
        TEMPLATE_MAP.put("$column.notNull", "表字段是否为空");
        TEMPLATE_MAP.put("$resultClass", "统一返回类");
        TEMPLATE_MAP.put("$config.genericity", "是否泛型");
        TEMPLATE_MAP.put("$column.primaryKey", "是否主键");
        TEMPLATE_MAP.put("$config.methodName", "统一返回的方法名");
        TEMPLATE_MAP.put("$config.mapperPackage", "mapper生成包路径");
        TEMPLATE_MAP.put("$config.implPackage", "service实现生成包路径");
        TEMPLATE_MAP.put("$config.cache", "是否开启缓存方法生成");
        TEMPLATE_MAP.put("$mapperName", "mapper类名");
        TEMPLATE_MAP.put("$config.data", "@Data注解");
        TEMPLATE_MAP.put("$config.activeRecord", "是否开启ar模式");
        TEMPLATE_MAP.put("$config.allArgsConstructor", "@AllArgsConstructor注解");
        TEMPLATE_MAP.put("$config.noArgsConstructor", "@NoArgsConstructor注解");
        TEMPLATE_MAP.put("$config.requiredArgsConstructor", "@RequiredArgsConstructor注解");
        TEMPLATE_MAP.put("$config.builder", "@Builder注解");
        TEMPLATE_MAP.put("$config.accessors", "@Accessors注解");
        TEMPLATE_MAP.put("$importClassList", "实体需要导入的类集合");
        TEMPLATE_MAP.put("$config.dataSource", "数据源配置");
        TEMPLATE_MAP.put("$table.onInsert", "实体类插入监听");
        TEMPLATE_MAP.put("$table.onUpdate", "实体类修改监听");
        TEMPLATE_MAP.put("$table.onSet", "实体类set监听");
        TEMPLATE_MAP.put("$config.idType", "ID生成类型");
        TEMPLATE_MAP.put("$column.name", "表字段名");
        TEMPLATE_MAP.put("$column.logicDelete", "是否逻辑删除");
        TEMPLATE_MAP.put("$column.tenant", "租户");
        TEMPLATE_MAP.put("$column.version", "乐观锁");
        TEMPLATE_MAP.put("$column.insertValue", "数据插入时，字段默认值");
        TEMPLATE_MAP.put("$column.updateValue", "数据修改时，字段默认值");
        TEMPLATE_MAP.put("$column.fieldType", "java字段类型");
        TEMPLATE_MAP.put("$column.type", "jdbc类型");
        TEMPLATE_MAP.put("$table.schema", "数据库名");
        TEMPLATE_MAP.put("$createTime", "代码生成时间");

    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        for (Variable variable : MybatisFlexPluginConfigData.getVariable()) {
            TEMPLATE_MAP.put("$"+variable.getName(), variable.getDescription());
        }
        Editor editor = parameters.getEditor();
        String prefix = result.getPrefixMatcher().getPrefix();
        if (!ObjectUtil.defaultIfNull(editor.getUserData(MybatisFlexSettingDialog.flexTemplate),false) || !prefix.startsWith("$")) {
            return ;
        }
        addCodeTip(result, parameters.getPosition().getProject());
    }

    /**
     * 添加代码提示
     *
     * @param result  结果
     * @param project 项目
     */
    private void addCodeTip(@NotNull CompletionResultSet result, Project project) {
        // 获取忽略大小写的结果集
        CompletionResultSet completionResultSet = result.caseInsensitive();
        TEMPLATE_MAP.forEach((key, value) -> {
            // 添加补全提示
            LookupElement lookupElement = LookupElementBuilder.create(key)
                    .withTypeText(value)
                    .withInsertHandler((context, item) -> {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            Editor editor = context.getEditor();
                            Document document = editor.getDocument();
                            int startOffset = context.getStartOffset();
                            int endOffset = context.getTailOffset();
                            String text = StrUtil.format("${{}}", item.getLookupString().replace("$", ""));
                            document.replaceString(startOffset, endOffset, text);
                            editor.getCaretModel().moveToOffset(startOffset + text.length());
                        });
                    })
                    .withIcon(Icons.FLEX);
            completionResultSet.addElement(lookupElement);
        });
    }


}
