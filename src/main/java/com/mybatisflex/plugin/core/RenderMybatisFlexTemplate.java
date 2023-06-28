package com.mybatisflex.plugin.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import com.mybatisflex.plugin.core.util.CodeReformat;
import com.mybatisflex.plugin.core.util.Modules;
import com.mybatisflex.plugin.core.util.VirtualFileUtils;
import com.mybatisflex.plugin.entity.TableInfo;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渲染MybatisFlex模板
 *
 * @author daijunxiong
 * @date 2023/06/27
 */
public class RenderMybatisFlexTemplate {

    public static void assembleData(List<TableInfo> selectedTableInfo, MybatisFlexConfig config, @Nullable Project project) {
        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        String className = "";
        HashMap<PsiDirectory, List<PsiElement>> templateMap = new HashMap<>();
        Map<String, String> templates = config.getTemplates();
        Map<String, String> packages = config.getPackages();
        for (TableInfo tableInfo : selectedTableInfo) {
            className = TableCore.getClassName(tableInfo.getName(), config.getTablePrefix());
            context.put("className", className);
            context.put("requestPath", TableCore.getTableName(tableInfo.getName(), config.getTablePrefix()));
            context.put("author", ObjectUtil.defaultIfEmpty(config.getAuthor(), "mybatis-flex-helper automatic generation"));
            context.put("since", ObjectUtil.defaultIfEmpty(config.getSince(), "1.0"));
            context.put("tableComment", tableInfo.getComment());
            context.put("tableName", tableInfo.getName());
            context.put("controllerPackage", config.getControllerPackage());
            context.put("modelPackage", config.getModelPackage());
            context.put("interfacePackage", config.getInterfacePackage());
            context.put("implPackage", config.getImplPackage());
            context.put("mapperPackage", config.getMapperPackage());
            context.put("config", config);
            context.put("isCacheExample", false);
            context.put("table", tableInfo);
            renderTemplate(config, project, templates, context, className, velocityEngine, templateMap,packages);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (Map.Entry<PsiDirectory, List<PsiElement>> entry : templateMap.entrySet()) {
                List<PsiElement> list = entry.getValue();
                for (PsiElement psiFile : list) {
                    entry.getKey().add(psiFile);
                }
            }
        });
    }

    /**
     * 渲染模板
     *
     * @param config         配置
     * @param project        项目
     * @param templates      模板
     * @param context        上下文
     * @param className      类名
     * @param velocityEngine 速度引擎
     * @param templateMap    模板映射
     * @param packages
     */
    private static void renderTemplate(MybatisFlexConfig config,
                                       @Nullable Project project,
                                       Map<String, String> templates,
                                       VelocityContext context,
                                       String className,
                                       VelocityEngine velocityEngine,
                                       HashMap<PsiDirectory, List<PsiElement>> templateMap, Map<String, String> packages) {
        for (Map.Entry<String, String> entry : templates.entrySet()) {
            StringWriter sw = new StringWriter();
            context.put("className", className);
            velocityEngine.evaluate(context, sw, "mybatis-flex", entry.getValue());
            Module module = Modules.getModule(config.getControllerModule());
            PsiDirectory packageDirectory = VirtualFileUtils.getPsiDirectory(module, packages.get(entry.getKey()),entry.getKey());
            String fileName = className + entry.getKey() + (StrUtil.isEmpty(entry.getKey()) ? ".xml" : ".java");
            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            PsiFile file = factory.createFileFromText(fileName, JavaFileType.INSTANCE, sw.toString());
            templateMap.computeIfAbsent(packageDirectory, k -> new ArrayList<>()).add( CodeReformat.reformat(file));
        }
    }


}
