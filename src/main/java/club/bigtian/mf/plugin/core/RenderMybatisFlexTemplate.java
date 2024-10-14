package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.entity.ColumnInfo;
import club.bigtian.mf.plugin.entity.TabInfo;
import club.bigtian.mf.plugin.entity.TableInfo;
import club.bigtian.mf.plugin.entity.Variable;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import groovy.lang.GroovyShell;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 渲染MybatisFlex模板
 *
 * @author daijunxiong
 * @date 2023/06/27
 */
public class RenderMybatisFlexTemplate {


    public static Map<String, TabInfo> remoteLocalPreview(TableInfo tableInfo) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context = new VelocityContext();

        removeEmptyPackage(config);
        Map<String, TabInfo> templateMap = new HashMap<>();
        Map<String, TabInfo> tabInfoMap = config.getTabList().stream()
                .collect(Collectors.toMap(TabInfo::getTitle, Function.identity()));
        logicDelete(Arrays.asList(tableInfo), config);
        String className = TableCore.getClassName(tableInfo.getName(), config.getTablePrefix());
        context.put("className", className);
        String tableName = TableCore.getTableName(tableInfo.getName(), config.getTablePrefix());
        context.put("requestPath", tableName);
        context.put("author", ObjectUtil.defaultIfEmpty(config.getAuthor(), "mybatis-flex-helper automatic generation"));
        context.put("since", ObjectUtil.defaultIfEmpty(config.getSince(), "1.0"));
        context.put("controllerName", className + ObjectUtil.defaultIfNull(config.getControllerSuffix(), "Controller"));
        context.put("modelName", className + ObjectUtil.defaultIfNull(config.getModelSuffix(), "Entity"));
        context.put("interfaceName", ObjectUtil.defaultIfNull(config.getInterfacePre(), "I")
                + className + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
        context.put("interfaceVariable", tableName + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
        context.put("implName", className + ObjectUtil.defaultIfNull(config.getImplSuffix(), "ServiceImpl"));
        context.put("mapperName", className + ObjectUtil.defaultIfNull(config.getMapperSuffix(), "Mapper"));
        context.put("config", config);
        context.put("importClassList", tableInfo.getImportClassList());
        context.put("table", tableInfo);
        context.put("createTime", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        GroovyShell shell = new GroovyShell();

        List<Variable> list = MybatisFlexPluginConfigData.getVariable();
        for (Variable variable : list) {
            Object result = shell.evaluate(variable.getScript());
            context.put(variable.getName(), result);
        }

        String qualifiedName = config.getQualifiedName();
        if (StrUtil.isNotBlank(qualifiedName)) {
            String methodName = config.getMethodName();
            config.setMethodName(StrUtil.subBefore(methodName, "(", false));
            context.put("resultClass", qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
        }
        context.put("className", className);
        for (TabInfo info : config.getTabList()) {
            StringWriter sw = new StringWriter();
            velocityEngine.evaluate(context, sw, "mybatis-flex", info.getContent());
            TabInfo tabInfo = tabInfoMap.get(info.getTitle());
            tabInfo.setContent(sw.toString());
            templateMap.put(info.getTitle(), tabInfo);
        }

        return templateMap;
    }

    public static void assembleData(List<TableInfo> selectedTableInfo, MybatisFlexConfig config, @Nullable Project project) {

        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        HashMap<PsiDirectory, List<PsiElement>> templateMap = new HashMap<>();
        removeEmptyPackage(config);
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
            context.put("interfaceName", ObjectUtil.defaultIfNull(Template.getMybatisFlexConfig().getInterfacePre(), "I")
                    + className + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
            context.put("interfaceVariable", tableName + ObjectUtil.defaultIfNull(config.getInterfaceSuffix(), "Service"));
            context.put("implName", className + ObjectUtil.defaultIfNull(config.getImplSuffix(), "ServiceImpl"));
            context.put("mapperName", className + ObjectUtil.defaultIfNull(config.getMapperSuffix(), "Mapper"));
            context.put("config", config);
            context.put("importClassList", tableInfo.getImportClassList());
            context.put("table", tableInfo);
            context.put("createTime", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            String qualifiedName = config.getQualifiedName();
            if (StrUtil.isNotBlank(qualifiedName)) {
                String methodName = config.getMethodName();
                config.setMethodName(StrUtil.subBefore(methodName, "(", false));
                context.put("resultClass", qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
            }
            customRender(config, velocityEngine, context, className, templateMap);
        }

        DumbService.getInstance(project).runWhenSmart(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                List<PsiJavaFile> psiJavaFiles = new ArrayList<>();
                for (Map.Entry<PsiDirectory, List<PsiElement>> entry : templateMap.entrySet()) {
                    List<PsiElement> list = entry.getValue();
                    PsiDirectory directory = entry.getKey();
                    remoteOldFile(config, list, directory);

                    for (PsiElement psiFile : list) {
                        try {
                            if (config.isKtFile()) {
                                PsiElement newPsiFile = directory.add(psiFile);
                                if (newPsiFile instanceof PsiJavaFile) {
                                    psiJavaFiles.add((PsiJavaFile) newPsiFile);
                                }
                            } else {
                                directory.add(psiFile);
                            }


                        } catch (IncorrectOperationException e) {
                            if (e.getMessage().contains("already exists")) {
                                PsiFile file = (PsiFile) psiFile;
                                Messages.showErrorDialog("文件已存在：" + file.getName(), "错误");
                            }
                            throw e;
                        } catch (Exception e) {
                            Messages.showErrorDialog("索引未更新", "错误");
                            throw e;
                        }
                    }
                }
                //  转换kt文件
                if (config.isKtFile()) {
                    KtFileUtil.convertKtFile(psiJavaFiles);
                }

            });
        });
        //
        // // 生成代码之后，重新构建
        // CompilerManagerUtil.make(Modules.getModule(config.getModelModule()));
    }

    private static void remoteOldFile(MybatisFlexConfig config, List<PsiElement> list, PsiDirectory directory) {
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
    }

    /**
     * 渲染 自定义模板
     *
     * @param config
     * @param velocityEngine
     * @param context
     * @param className
     * @param templateMap
     * @return
     */
    private static void customRender(MybatisFlexConfig config, VelocityEngine velocityEngine, VelocityContext context, String className, HashMap<PsiDirectory, List<PsiElement>> templateMap) {
        Project project = ProjectUtils.getCurrentProject();
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        List<TabInfo> infoList = config.getTabList();
        Map<String, String> packages = config.getPackages();
        Map<String, String> modules = config.getModules();
        Map<String, String> suffixMap = config.getSuffix();

        if (CollUtil.isNotEmpty(infoList)) {
            for (TabInfo info : infoList) {
                String genPath = info.getGenPath();
                if (StrUtil.isEmpty(genPath)) {
                    StringWriter sw = new StringWriter();
                    context.put("className", className);
                    velocityEngine.evaluate(context, sw, "mybatis-flex", info.getContent());
                    PsiDirectory packageDirectory = getPsiDirectory(packages, modules, info.getTitle());
                    if (ObjectUtil.isNull(packageDirectory)) {
                        continue;
                    }
                    DumbService.getInstance(project).runWhenSmart(() -> {
                        String classPrefix = ObjectUtil.equal(MybatisFlexConstant.SERVICE, info.getTitle())
                                ? ObjectUtil.defaultIfNull(config.getInterfacePre(), "I") : "";
                        String fileName = classPrefix + className + suffixMap.get(info.getTitle()) + info.getSuffix();
                        PsiFile file = factory.createFileFromText(fileName, StrUtil.endWithIgnoreCase(info.getSuffix(), "xml") ? XmlFileType.INSTANCE : JavaFileType.INSTANCE, sw.toString());
                        templateMap.computeIfAbsent(packageDirectory, k -> new ArrayList<>()).add(CodeReformat.reformat(file));
                    });
                    continue;
                }
                StringWriter sw = new StringWriter();
                velocityEngine.evaluate(context, sw, "mybatis-flex", info.getContent());
                PsiDirectory psiDirectory = info.isBusinesFolder() ? VirtualFileUtils.getPsiDirectoryAndCreate(genPath, StrUtil.lowerFirst(className),info.getComponentPath()):VirtualFileUtils.getPsiDirectory(project, genPath);
                String fileName = ObjectUtil.defaultIfBlank(info.getFileName(), className) + info.getSuffix();
                PsiFile file = factory.createFileFromText(fileName, getFileTypeByExtension(info.getSuffix().replace(".", "")), sw.toString());
                templateMap.computeIfAbsent(psiDirectory, k -> new ArrayList<>()).add(CodeReformat.reformat(file));
            }
        }
    }

    public static FileType getFileTypeByExtension(String extension) {
        return FileTypeManager.getInstance().getFileTypeByExtension(extension);
    }

    private static void logicDelete(List<TableInfo> selectedTableInfo, MybatisFlexConfig config) {

        Set<String> fieldSet = Arrays.stream(ObjectUtil.defaultIfNull(config.getLogicDeleteField(), "").split(";"))
                .collect(Collectors.toSet());
        Set<String> tenantSet = Arrays.stream(ObjectUtil.defaultIfNull(config.getTenant(), "").split(";"))
                .collect(Collectors.toSet());
        Set<String> versionSet = Arrays.stream(ObjectUtil.defaultIfNull(config.getVersion(), "").split(";"))
                .collect(Collectors.toSet());
        Map<String, String> insertMap = new HashMap<>();
        if (StrUtil.isNotBlank(config.getInsertValue())) {
            insertMap = Arrays.stream(config.getInsertValue().split(";"))
                    .map(el -> el.split(":"))
                    .collect(Collectors.toMap(
                            split -> split[0],
                            split -> split[1],
                            (existingValue, newValue) -> newValue
                    ));
        }

        Map<String, String> updateMap = new HashMap<>();
        if (StrUtil.isNotBlank(config.getUpdateValue())) {
            updateMap = Arrays.stream(config.getUpdateValue().split(";"))
                    .map(el -> el.split(":"))
                    .collect(Collectors.toMap(
                            split -> split[0],
                            split -> split[1],
                            (existingValue, newValue) -> newValue
                    ));
        }

        List<String> superFieldList = new ArrayList<>();
        String modelSuperClass = config.getModelSuperClass();
        if (StrUtil.isNotBlank(modelSuperClass)) {
            PsiClass superClass = PsiJavaFileUtil.getPsiClass(modelSuperClass);
            if (ObjectUtil.isNotNull(superClass)) {
                Arrays.stream(superClass.getFields())
                        .forEach(field -> superFieldList.add(field.getName()));
            }
        }
        for (TableInfo info : selectedTableInfo) {
            if (StrUtil.isNotBlank(config.getModelSuperClass())) {
                info.setSuperClass(StrUtil.subAfter(config.getModelSuperClass(), ".", true));
                info.getImportClassList().add(config.getModelSuperClass());
            }
            if (StrUtil.isNotBlank(config.getOnInsert())) {
                info.setOnInsert(StrUtil.subAfter(config.getOnInsert(), ".", true));
                info.getImportClassList().add(config.getOnInsert());
            }
            if (StrUtil.isNotBlank(config.getOnUpdate())) {
                info.setOnUpdate(StrUtil.subAfter(config.getOnUpdate(), ".", true));
                info.getImportClassList().add(config.getOnUpdate());
            }
            if (StrUtil.isNotBlank(config.getOnSet())) {
                info.setOnSet(StrUtil.subAfter(config.getOnSet(), ".", true));
                info.getImportClassList().add(config.getOnSet());
            }

            for (ColumnInfo columnInfo : info.getColumnList()) {
                if (superFieldList.contains(columnInfo.getFieldName())) {
                    info.getColumnList().remove(columnInfo);
                    continue;
                }
                columnInfo.setLogicDelete(fieldSet.contains(columnInfo.getName()));
                columnInfo.setTenant(tenantSet.contains(columnInfo.getName()));
                columnInfo.setVersion(versionSet.contains(columnInfo.getName()));
                if (insertMap.containsKey(columnInfo.getName())) {
                    columnInfo.setInsertValue(insertMap.get(columnInfo.getName()));
                }
                if (updateMap.containsKey(columnInfo.getName())) {
                    columnInfo.setUpdateValue(updateMap.get(columnInfo.getName()));
                }
            }
        }
    }

    private static void removeEmptyPackage(MybatisFlexConfig config) {
        Map<String, String> templates = new ConcurrentHashMap<>(config.getTemplates());
        Map<String, String> packages = new ConcurrentHashMap<>( config.getPackages());
        for (Map.Entry<String, String> entry : packages.entrySet()) {
            if (StrUtil.isEmpty(entry.getValue())) {
                packages.remove(entry.getKey());
                templates.remove(entry.getKey());
            }
        }
    }


    private static PsiDirectory getPsiDirectory(Map<String, String> packages, Map<String, String> modules, String key) {
        return VirtualFileUtils.getPsiDirectory(Modules.getModule(modules.get(key)), packages.get(ObjectUtil.defaultIfBlank(key, MybatisFlexConstant.XML)), key);
    }


    /**
     * 获取远程数据并渲染
     * {
     * "data": "data（返回的数据列表）",
     * "code": "code（状态码）",
     * "msg": "msg(当返回编码不是正常的时候会提示)",
     * "num": "200(和code匹配使用，表示响应正茬会给你)"
     * }
     *
     * @param selectedTabeList
     */
    public static boolean remoteDataGen(List<String> selectedTabeList) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        HashMap<PsiDirectory, List<PsiElement>> templateMap = new HashMap<>();
        List<JSONObject> list = null;
        try {
            list = getRemoteData(selectedTabeList);
        } catch (Exception e) {
            return false;
        }

        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context;
        for (JSONObject tableInfo : list) {
            context = new VelocityContext(tableInfo);
            customRender(config, velocityEngine, context, context.get("ClassName").toString(), templateMap);
        }
        List<PsiJavaFile> psiJavaFiles = new ArrayList<>();
        DumbService.getInstance(ProjectUtils.getCurrentProject()).runWhenSmart(() -> {
            WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
                for (Map.Entry<PsiDirectory, List<PsiElement>> entry : templateMap.entrySet()) {
                    List<PsiElement> list1 = entry.getValue();
                    PsiDirectory directory = entry.getKey();
                    remoteOldFile(config, list1, directory);

                    for (PsiElement psiFile : list1) {
                        PsiElement newPsiFile = null;
                        try {
                            newPsiFile = directory.add(psiFile);
                        } catch (IncorrectOperationException e) {
                            if (e.getMessage().contains("already exists")) {
                                PsiFile file = (PsiFile) psiFile;
                                Messages.showErrorDialog("文件已存在：" + file.getName(), "错误");
                            }
                            throw e;
                        }
                        if (newPsiFile instanceof PsiJavaFile) {
                            psiJavaFiles.add((PsiJavaFile) newPsiFile);
                        }
                    }
                }
            });
        });
        //  转换kt文件
        if (config.isKtFile()) {
            KtFileUtil.convertKtFile(psiJavaFiles);
        }
        return true;
    }

    public static Map<String, TabInfo> remoteDataPreview(String name) {
        List<JSONObject> list = getRemoteData(Arrays.asList(name));
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        Map<String, TabInfo> templateMap = new HashMap<>();
        VelocityEngine velocityEngine = new VelocityEngine();
        VelocityContext context;
        Map<String, String> templates = new ConcurrentHashMap<>(config.getTemplates());
        Map<String, TabInfo> tabInfoMap = config.getTabList().stream()
                .collect(Collectors.toMap(TabInfo::getTitle, Function.identity()));
        for (JSONObject tableInfo : list) {
            context = new VelocityContext(tableInfo);
            for (Map.Entry<String, String> entry : templates.entrySet()) {
                StringWriter sw = new StringWriter();
                velocityEngine.evaluate(context, sw, "mybatis-flex", entry.getValue());
                TabInfo tabInfo = tabInfoMap.get(ObjectUtil.defaultIfBlank(entry.getKey(), "Xml"));
                tabInfo.setContent(sw.toString());
                templateMap.put(ObjectUtil.defaultIfBlank(entry.getKey(), "Xml"), tabInfo);
            }
        }
        return templateMap;
    }

    private static @Nullable List<JSONObject> getRemoteData(List<String> tableNames) {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();

        String remoteDataUrl = config.getRemoteDataUrl();
        if (StrUtil.isEmpty(remoteDataUrl)) {
            NotificationUtils.notifyError("请配置远程数据地址", "错误", ProjectUtils.getCurrentProject());
            Assert.isTrue(false);
        }

        HttpRequest request = HttpRequest.post(remoteDataUrl)
                .body(JSON.toJSONString(tableNames));
        if (StrUtil.isNotEmpty(config.getRemoteDataToken()) && StrUtil.isNotEmpty(config.getRemoteHeader())) {
            request.header(config.getRemoteHeader(), config.getRemoteDataToken());
        }
        JSONObject resultField = JSON.parseObject(config.getResultField());

        JSONObject resultJson = null;
        try {
            String result = request.execute().body();
            resultJson = JSON.parseObject(result);
        } catch (IORuntimeException e) {
            NotificationUtils.notifyError("远程数据请求失败", "错误", ProjectUtils.getCurrentProject());
            Assert.isTrue(false);
        } catch (Exception e) {
            NotificationUtils.notifyError("返回数据格式错误", "错误", ProjectUtils.getCurrentProject());
            Assert.isTrue(false);
        }
        if (!resultJson.getInteger(resultField.getString("code")).equals(resultField.getInteger("num"))) {
            NotificationUtils.notifyError(resultJson.getString(resultField.getString("msg")), "错误", ProjectUtils.getCurrentProject());
            Assert.isTrue(false);
        }
        List<JSONObject> list = resultJson.getList(resultField.getString("data"), JSONObject.class);
        return list;
    }
}
