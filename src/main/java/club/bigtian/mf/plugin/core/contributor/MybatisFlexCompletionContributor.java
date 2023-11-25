package club.bigtian.mf.plugin.core.contributor;

import club.bigtian.mf.plugin.core.MybatisFlexDocumentChangeHandler;
import club.bigtian.mf.plugin.core.config.CustomConfig;
import club.bigtian.mf.plugin.core.util.KtFileUtil;
import club.bigtian.mf.plugin.core.util.Modules;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * tabDef补全提示
 *
 * @author BigTian
 */
public class MybatisFlexCompletionContributor extends CompletionContributor {

    PsiElementFactory elementFactory;
    JavaPsiFacade psiFacade;
    PsiManager psiManager;
    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        try {
            Project project = parameters.getPosition().getProject();
            if (ObjectUtil.isNull(elementFactory)) {
                elementFactory = JavaPsiFacade.getElementFactory(project);
                psiFacade = JavaPsiFacade.getInstance(project);
                psiManager = PsiManager.getInstance(project);
            }
            Map<String, String> tableDefMap = new ConcurrentHashMap<>(new TreeMap<>());
            // 获取当前编辑的文件
            Document document = parameters.getEditor().getDocument();
            VirtualFile currentFile = fileDocumentManager.getFile(document);
            assert currentFile != null;
            getDependenciesTableDef(currentFile, project, tableDefMap);
            // 移除重复的元素
            removeRepetitionElement(currentFile, tableDefMap, result);
            if (CollUtil.isEmpty(tableDefMap)) {
                return;
            }
            // 添加代码提示
            addCodeTip(result, tableDefMap, currentFile, project, document);
        } catch (PsiInvalidElementAccessException e) {

        }
    }


    /**
     * 获取模块依赖关系的所有 tableDef文件
     *
     * @param currentFile 当前文件
     * @param project     项目
     * @param tableDefMap 表def地图
     */
    private void getDependenciesTableDef(VirtualFile currentFile, Project project, Map<String, String> tableDefMap) {
        Module module = ModuleUtil.findModuleForFile(currentFile, project);
        if (ObjectUtil.isNull(module)) {
            return;
        }
        // 获取当前模块的依赖模块
        List<Module> moduleList = Arrays.stream(ModuleRootManager.getInstance(module).getDependencies()).collect(Collectors.toList());
        moduleList.add(module);
        CustomConfig config = new CustomConfig();
        // 获取当前模块以及所依赖的模块的TableDef文件
        for (Module dependency : moduleList) {
            VirtualFile[] contentRoots = ModuleRootManager.getInstance(dependency).getContentRoots();
            VirtualFile virtualFile = null;
            for (VirtualFile contentRoot : contentRoots) {
                config = Modules.moduleConfig(dependency);
                virtualFile = VirtualFileUtils.getVirtualFile(contentRoot, config);
                if (ObjectUtil.isNotNull(virtualFile)) {
                    break;
                }
            }
            if (ObjectUtil.isNull(virtualFile)) {
                return;
            }
            getTableDef(virtualFile, tableDefMap, config);
        }
    }

    /**
     * 添加代码提示
     *
     * @param result      结果
     * @param tableDefMap 表def地图
     * @param currentFile 当前文件
     * @param project     项目
     */
    private void addCodeTip(@NotNull CompletionResultSet result, Map<String, String> tableDefMap, VirtualFile currentFile, Project project, Document document) {
        // 获取忽略大小写的结果集
        CompletionResultSet completionResultSet = result.caseInsensitive();
        for (Map.Entry<String, String> entry : tableDefMap.entrySet()) {
            // 添加补全提示
            LookupElement lookupElement = LookupElementBuilder.create(entry.getKey()).withTypeText(StrUtil.subAfter(entry.getValue(), ".", true) + "(MybatisFlex-Helpler)", true).withInsertHandler((context, item) -> {
                // 选中后的处理事件
                PsiClass psiClass = psiFacade.findClass(entry.getValue(), GlobalSearchScope.projectScope(project));
                // 创建静态导入
                assert psiClass != null;
                // 获取导入的import
                PsiFile file = psiManager.findFile(currentFile);
                // 导入import
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    if (file instanceof PsiJavaFile) {
                        PsiJavaFile psiJavaFile = (PsiJavaFile) file;
                        javaImport(psiJavaFile, psiClass, entry.getKey());
                    } else if (file instanceof KtFile) {
                        KtFile ktFile = (KtFile) file;
                        ktImport(ktFile, psiClass, entry.getKey(), document);
                    }

                });
            }).withIcon(Nodes.Field);
            completionResultSet.addElement(lookupElement);
        }
    }

    /**
     * java导入
     *
     * @param psiJavaFile psi java文件
     * @param psiClass    psi类
     * @param finalImport 最后导入
     */
    public void javaImport(PsiJavaFile psiJavaFile, PsiClass psiClass, String finalImport) {
        Set<String> importSet = PsiJavaFileUtil.getImportSet(psiJavaFile);
        PsiImportStaticStatement importStaticStatement = elementFactory.createImportStaticStatement(psiClass, finalImport);
        // 如果已经导入了，就不再导入
        if (importSet.contains(importStaticStatement.getText())) {
            return;
        }
        psiJavaFile.getImportList().add(importStaticStatement);
    }

    /**
     * kt导入
     *
     * @param ktFile   kt文件
     * @param psiClass psi类
     */
    public void ktImport(KtFile ktFile, PsiClass psiClass, String finalImport, Document document) {
        Set<String> importSet = KtFileUtil.getImportSet(ktFile);
        String importText = Objects.requireNonNull(psiClass.getQualifiedName());
        PsiImportStatement importStatementOnDemand = elementFactory.createImportStatementOnDemand(importText);
        // 如果已经导入了，就不再导入
        if (importSet.contains(importStatementOnDemand.getText())) {
            return;
        }
        ktFile.getImportList().add(importStatementOnDemand);
        // 为什么要这样写，因为kotlin 不支持静态导入，要么就是.*导入，但是.*又会导致上面代码提示重复，所以只能这样写
        String text = ktFile.getText().replace("import " + importText + ".*", "\nimport " + importText + "." + finalImport);
        document.setText(text);
    }

    /**
     * 移除重复的元素
     *
     * @param currentFile 当前编辑的文件
     * @param tableDefMap 补全提示的map
     * @param result
     */
    private void removeRepetitionElement(VirtualFile currentFile, Map<String, String> tableDefMap, @NotNull CompletionResultSet result) {
        // 当文件导入过一次后，就不再提示，因为 idea 自带的就会提示了；
        Set<String> importSet = getFileImport(currentFile);
        String prefix = result.getPrefixMatcher().getPrefix();
        for (String key : tableDefMap.keySet()) {
            if (!StrUtil.startWithIgnoreCase(key, prefix) || StrUtil.isBlank(prefix)) {
                tableDefMap.remove(key);
            }
        }
        for (String importExp : importSet) {
            tableDefMap.remove(importExp);
        }
    }

    private Set<String> getFileImport(VirtualFile currentFile) {
        Set<String> importSet = new HashSet<>();
        try {
            if (!psiManager.getProject().isDisposed()) {
                return importSet;
            }
            PsiFile file = psiManager.findFile(currentFile);
            if (file instanceof PsiJavaFile) {
                PsiJavaFile javaFile = (PsiJavaFile) file;
                importSet = PsiJavaFileUtil.getImportSet(javaFile);
            } else if (file instanceof KtFile) {
                KtFile ktFile = (KtFile) file;
                importSet = KtFileUtil.getImportSet(ktFile);
            }
        } catch (Exception e) {

        }
        return importSet.stream().map(el -> {
            el = StrUtil.subAfter(el, ".", true);
            return StrUtil.subBefore(el, ";", true);
        }).collect(Collectors.toSet());
    }

    /**
     * 获取相关的TableDef文件
     *
     * @param file
     * @param tableDefMap
     */
    private void getTableDef(VirtualFile file, Map<String, String> tableDefMap, CustomConfig config) {
        try {
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                boolean directory = child.isDirectory();
                if (directory) {
                    getTableDef(child, tableDefMap, config);
                } else {
                    String name = child.getName();
                    String tableDefConf = ObjectUtil.defaultIfNull(config.getTableDefClassSuffix(), "TableDef");
                    if (name.contains(tableDefConf) && !psiManager.getProject().isDisposed()) {
                        PsiClassOwner psiJavaFile = (PsiClassOwner) psiManager.findFile(child);
                        assert psiJavaFile != null;
                        String packageName = psiJavaFile.getPackageName();
                        String path = StrUtil.subBefore(child.getPath(), ".", true);
                        String tableDef = StrUtil.subAfter(path, "/", true);
                        String tableName = MybatisFlexDocumentChangeHandler.getDefInstanceName(config, StrUtil.subBefore(tableDef, tableDefConf, false), true);
                        tableDefMap.put(tableName, packageName + "." + tableDef);
                    }
                }
            }
        } catch (Exception e) {

        }
    }


}
