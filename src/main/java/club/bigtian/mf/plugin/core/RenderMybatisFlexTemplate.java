package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 渲染MybatisFlex模板
 *
 * @author daijunxiong
 * @date 2023/06/27
 */
public class RenderMybatisFlexTemplate {
    private static final Logger LOG = Logger.getInstance(RenderMybatisFlexTemplate.class);

    public static void assembleData(List<TableInfo> selectedTableInfo, MybatisFlexConfig config, @Nullable Project project) {

        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        HashMap<PsiDirectory, List<PsiElement>> templateMap = new HashMap<>();
        Map<String, String> templates = new ConcurrentHashMap<>(config.getTemplates());
        Map<String, String> suffixMap = config.getSuffix();
        Map<String, String> packages = new ConcurrentHashMap<>(config.getPackages());
        removeEmptyPackage(packages, templates);
        Map<String, String> modules = config.getModules();
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        logicDelete(selectedTableInfo, config);
        for (TableInfo tableInfo : selectedTableInfo) {
            String className = TableCore.getClassName(tableInfo.getName(), config.getTablePrefix());
            context.put("className", className);
            String tableName = TableCore.getTableName(tableInfo.getName(), config.getTablePrefix());
            context.put("requestPath", tableName);
            context.put("author", ObjectUtil.defaultIfEmpty(config.getAuthor(), "mybatis-flex-helper automatic generation"));
            context.put("since", ObjectUtil.defaultIfEmpty(config.getSince(), "1.0"));
            context.put("controllerName", className + ObjectUtil.defaultIfNull(config.getControllerSuffix(), "Controller"));
            context.put("modelName", className + ObjectUtil.defaultIfNull(config.getModelSuffix(), "Entity"));
            context.put("interfaceName", "I" + className + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
            context.put("interfaceVariable", tableName + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
            context.put("implName", className + ObjectUtil.defaultIfNull(config.getImplSuffix(), "ServiceImpl"));
            context.put("mapperName", className + ObjectUtil.defaultIfNull(config.getMapperSuffix(), "Mapper"));
            context.put("config", config);
            context.put("importClassList", tableInfo.getImportClassList());
            context.put("table", tableInfo);
            String qualifiedName = config.getQualifiedName();
            if (StrUtil.isNotBlank(qualifiedName)) {
                String methodName = config.getMethodName();
                config.setMethodName(methodName.substring(0, methodName.indexOf("(")));
                context.put("resutlClass", qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
            }
            renderTemplate(templates, context, className, velocityEngine, templateMap, packages, suffixMap, modules, factory);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (Map.Entry<PsiDirectory, List<PsiElement>> entry : templateMap.entrySet()) {
                List<PsiElement> list = entry.getValue();
                PsiDirectory directory = entry.getKey();
                DumbService.getInstance(project).runWithAlternativeResolveEnabled(() -> {
                    // 如果勾选了覆盖，则删除原有文件
                    if (config.isOverrideCheckBox()) {
                        for (PsiElement psiFile : list) {
                            if (psiFile instanceof PsiFile) {
                                PsiFile file = (PsiFile) psiFile;
                                PsiFile directoryFile = directory.findFile(file.getName());
                                if (ObjectUtil.isNotNull(directoryFile)) {
                                    directoryFile.delete();
                                }
                            }
                        }
                    }
//                // 提交所有待处理的文档,防止出现索引未更新的情况
                    PsiDocumentManager.getInstance(project).commitAllDocuments();
                    for (PsiElement psiFile : list) {
                        try {
                            directory.add(psiFile);
                        } catch (IncorrectOperationException e) {
                            if (e.getMessage().contains("already exists")) {
                                PsiFile file = (PsiFile) psiFile;
                                Messages.showErrorDialog("文件已存在：" + file.getName(), "错误");
                            }
                            throw e;
                        } catch (Exception e) {
                            LOG.error("添加文件失败", e);
                            Messages.showErrorDialog("索引未更新", "错误");
                            throw e;
                        }
                    }
                });
            }
        });
        //生成代码之后，重新构建
        CompilerManagerUtil.make(Modules.getModule(config.getModelModule()));
    }

    private static void logicDelete(List<TableInfo> selectedTableInfo, MybatisFlexConfig config) {
        String logicDeleteField = config.getLogicDeleteField();
        if (StrUtil.isBlank(logicDeleteField)) {
            return;
        }
        Set<String> fieldSet = Arrays.stream(logicDeleteField.split(";"))
                .collect(Collectors.toSet());
        for (TableInfo info : selectedTableInfo) {
            for (ColumnInfo columnInfo : info.getColumnList()) {
                columnInfo.setLogicDelete(fieldSet.contains(columnInfo.getName()));
            }
        }
    }

    private static void removeEmptyPackage(Map<String, String> packages, Map<String, String> templates) {
        for (Map.Entry<String, String> entry : packages.entrySet()) {
            if (StrUtil.isEmpty(entry.getValue())) {
                packages.remove(entry.getKey());
                templates.remove(entry.getKey());
            }
        }
    }


    /**
     * 渲染模板
     *
     * @param templates      模板
     * @param context        上下文
     * @param className      类名
     * @param velocityEngine 速度引擎
     * @param templateMap    模板映射
     * @param packages
     * @param suffixMap
     * @param modules
     * @param factory
     */
    private static void renderTemplate(
            Map<String, String> templates,
            VelocityContext context,
            String className,
            VelocityEngine velocityEngine,
            HashMap<PsiDirectory, List<PsiElement>> templateMap,
            Map<String, String> packages,
            Map<String, String> suffixMap,
            Map<String, String> modules,
            PsiFileFactory factory) {

        for (Map.Entry<String, String> entry : templates.entrySet()) {
            StringWriter sw = new StringWriter();
            context.put("className", className);
            velocityEngine.evaluate(context, sw, "mybatis-flex", entry.getValue());
            Module module = Modules.getModule(modules.get(entry.getKey()));
            PsiDirectory packageDirectory = VirtualFileUtils.getPsiDirectory(module, packages.get(entry.getKey()), entry.getKey());
            String classPrefix = ObjectUtil.equal("Service", entry.getKey()) ? "I" : "";
            String fileName = classPrefix + className + suffixMap.get(entry.getKey()) + (StrUtil.isEmpty(entry.getKey()) ? ".xml" : ".java");
            PsiFile file = factory.createFileFromText(fileName, StrUtil.isEmpty(entry.getKey()) ? XmlFileType.INSTANCE : JavaFileType.INSTANCE, sw.toString());
            templateMap.computeIfAbsent(packageDirectory, k -> new ArrayList<>()).add(CodeReformat.reformat(file));
        }
    }


}
