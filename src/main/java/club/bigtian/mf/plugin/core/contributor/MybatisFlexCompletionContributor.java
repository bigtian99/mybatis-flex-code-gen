package club.bigtian.mf.plugin.core.contributor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.codeInsight.completion.*;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * tabDef补全提示
 * Author: BigTian
 */
public class MybatisFlexCompletionContributor extends CompletionContributor {
    PsiElementFactory elementFactory;
    JavaPsiFacade psiFacade;
    PsiManager psiManager;
    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        Project project = parameters.getPosition().getProject();
        if (ObjectUtil.isNull(elementFactory)) {
            elementFactory = JavaPsiFacade.getElementFactory(project);
            psiFacade = JavaPsiFacade.getInstance(project);
            psiManager = PsiManager.getInstance(project);
        }
        TreeMap<String, String> tableDefMap = new TreeMap<>();
        // 获取当前编辑的文件
        Document document = parameters.getEditor().getDocument();

        VirtualFile currentFile = fileDocumentManager.getFile(document);
        Module module = ModuleUtil.findModuleForFile(currentFile, project);
        // 获取当前模块的依赖模块
        List<Module> moduleList = Arrays.stream(ModuleRootManager.getInstance(module).getDependencies())
                .collect(Collectors.toList());
        moduleList.add(module);
        // 获取当前模块以及所依赖的模块的TableDef文件
        for (Module dependency : moduleList) {
            VirtualFile[] contentRoots = ModuleRootManager.getInstance(dependency).getContentRoots();
            getTableDef(getVirtualFile(contentRoots[0]), tableDefMap, project);
        }
        // 获取忽略大小写的结果集
        CompletionResultSet completionResultSet = result.caseInsensitive();

        for (Map.Entry<String, String> entry : tableDefMap.entrySet()) {
            // 添加补全提示
            LookupElement lookupElement = LookupElementBuilder.create(entry.getKey())
                    .withTypeText(StrUtil.subAfter(entry.getValue(), ".", true) + "(MybatisFlex-Hepler)", true)
                    .withInsertHandler((context, item) -> {
                        // 选中后的处理事件
                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiManager.findFile(currentFile);
                        // 搜索类
                        PsiClass psiClass = psiFacade.findClass(entry.getValue(), GlobalSearchScope.projectScope(project));
                        // 创建静态导入
                        PsiImportStaticStatement importStaticStatement = elementFactory.createImportStaticStatement(psiClass, entry.getKey());
                        // 获取导入的import
                        List<String> list = Arrays.stream(Objects.requireNonNull(psiJavaFile.getImportList()).getAllImportStatements())
                                .map(PsiElement::getText)
                                .toList();
                        // 如果已经导入了，就不再导入
                        if (list.contains(importStaticStatement.getText())) {
                            return;
                        }
                        // 导入import
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            psiJavaFile.getImportList().add(importStaticStatement);
                        });
                    })
                    .withIcon(Nodes.Field);
            completionResultSet.addElement(lookupElement);
        }

    }

    /**
     * 获取相关的TableDef文件
     *
     * @param file
     * @param tableDefMap
     * @param project
     */
    private void getTableDef(VirtualFile file, Map<String, String> tableDefMap, Project project) {
        VirtualFile[] children = file.getChildren();
        for (VirtualFile child : children) {
            boolean directory = child.isDirectory();
            if (directory) {
                getTableDef(child, tableDefMap, project);
            } else {
                String name = child.getName();
                if (name.endsWith("TableDef.java")) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiManager.findFile(child);
                    String packageName = psiJavaFile.getPackageName();
                    String path = child.getPath().replace(".java", "");
                    String tableDef = StrUtil.subAfter(path, "/", true);
                    String tableName = StrUtil.toUnderlineCase(StrUtil.subBefore(tableDef, "TableDef", false)).toUpperCase();
                    tableDefMap.put(tableName, packageName + "." + tableDef);
                }
            }
        }
    }

    /**
     * 只获取target/build目录下的文件
     *
     * @param baseDir
     * @return
     */
    @Nullable
    private static VirtualFile getVirtualFile(VirtualFile baseDir) {
        VirtualFile file = baseDir.findChild("target");
        if (ObjectUtil.isNull(file)) {
            file = baseDir.findChild("build");
        }
        VirtualFile[] children = file.getChildren();
        for (VirtualFile child : children) {
            if (child.getName().startsWith("generated")) {
                return child;
            }
        }
        return file;
    }

}
