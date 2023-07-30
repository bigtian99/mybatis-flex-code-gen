package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.function.SimpleFunction;
import club.bigtian.mf.plugin.core.log.MyBatisLogExecutor;
import club.bigtian.mf.plugin.core.util.CodeReformat;
import club.bigtian.mf.plugin.core.util.CompilerManagerUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.windows.SQLPreviewDialog;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.execution.*;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class SQLPreviewAction extends AnAction {

    private static final List<String> IMPORT_LIST = Arrays.asList("com.mybatisflex.core.FlexGlobalConfig",
            "com.mybatisflex.core.mybatis.FlexConfiguration",
            "com.zaxxer.hikari.HikariDataSource",
            "org.apache.ibatis.mapping.Environment",
            "org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory");

    private static final String COMMON_CODE =
            "    Environment environment = new Environment(\"mybatisFlex\", new JdbcTransactionFactory(), new HikariDataSource());\n" +
                    "        FlexConfiguration configuration = new FlexConfiguration(environment);\n" +
                    "        FlexGlobalConfig globalConfig = FlexGlobalConfig.getDefaultConfig();\n" +
                    "        globalConfig.setConfiguration(configuration);\n" +
                    "        FlexGlobalConfig.setConfig(\"mybatisFlex\", globalConfig, true);\n" +
                    "        configuration.addMapper({}.class);\n";
    private static final Logger LOG = Logger.getInstance(SQLPreviewAction.class);
    public static final String SYSTEM_OUT_PRINTLN_TO_SQL = "\nSystem.out.println({}.toSQL());";
    public static final String CLASS_TEMPLATE = "public class MybatisFlexSqlPreview {\n    public static void main(String[] args) { {}\n}\n}";


    public void preview(String selectedText, PsiJavaFile psiFile, SimpleFunction function) {
        try {
            boolean flag = StrUtil.containsAny(selectedText, "QueryChain.create", "UpdateChain.create");
            if (selectedText.contains("QueryWrapper") || flag) {
                if (flag) {
                    selectedText = StrUtil.format(SYSTEM_OUT_PRINTLN_TO_SQL, StrUtil.subBefore(selectedText, ";", false));
                } else {
                    String queryWrapper = ObjectUtil.defaultIfNull(StrUtil.subBetween(selectedText, "QueryWrapper", "="), StrUtil.subBefore(selectedText, ";", true)).trim();
                    selectedText += StrUtil.format(SYSTEM_OUT_PRINTLN_TO_SQL, queryWrapper);
                }
            } else {
                // 处理链式调用
                selectedText = chain(StrUtil.subBefore(selectedText, ";", true), psiFile);
            }

            createFile(psiFile, StrUtil.format(CLASS_TEMPLATE, selectedText), psiFile.getPackageName());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            function.apply();
        }
    }

    /**
     * 创建文件
     *
     * @param psiFile
     * @param text
     * @param packageName
     */
    private void createFile(PsiJavaFile psiFile, String text, String packageName) {
        Project project = ProjectUtils.getCurrentProject();
        PsiJavaFile psiJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText("MybatisFlexSqlPreview.java", JavaFileType.INSTANCE, text);
        psiJavaFile.setPackageName(psiFile.getPackageName());
        PsiImportList importList = psiFile.getImportList();
        PsiDirectory containingDirectory = psiFile.getContainingDirectory();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (ObjectUtil.isNotNull(importList)) {
                psiJavaFile.getImportList().add(importList);
            }
            try {
                PsiFile file = containingDirectory.findFile(psiJavaFile.getName());
                if (ObjectUtil.isNotNull(file)) {
                    file.delete();
                }
                PsiElement element = containingDirectory.add(CodeReformat.reformat(psiJavaFile));
                VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
                showSql(project, packageName, virtualFile);
            } catch (Exception e) {
                LOG.error("SQLPreviewAction error", e);
            }
        });
    }

    /**
     * 处理链式调用
     * 约定：默认最后一个方法是执行数据库操作的方法
     *
     * @param text
     * @return
     */
    public String chain(String text, PsiJavaFile psiJavaFile) {
        if (text.contains("=")) {
            text = StrUtil.subAfter(text, "=", false);
        }
        //update/remove就截取舍弃
        if (StrUtil.containsAny(text, "update()", "remove()")) {
            text = StrUtil.subBefore(text, ".", true);
        }
        text = StrUtil.format(SYSTEM_OUT_PRINTLN_TO_SQL, text);

        if (text.contains("queryChain()")) {
            return getAutowiredField(psiJavaFile, text, true);
        }
        String val = StrUtil.subBetween(text, "of(", ")");
//        //获取类型
        if (val.startsWith("Mappers")) {
            val = StrUtil.subBetween(val, "(", ".class");
        } else {
            val = StrUtil.subBefore(val, ".class", false);
        }

        Collection<PsiClass> implementors =
                PsiJavaFileUtil.getSonPsiClass(MybatisFlexConstant.MYBATISFLEX_CORE_BASE_MAPPER,
                        GlobalSearchScope.allScope(ProjectUtils.getCurrentProject()));
        Iterator<PsiClass> iterator = implementors.iterator();
        String qualifiedName = "";
        //判断是不是对应的mapper
        while (iterator.hasNext()) {
            PsiClass next = iterator.next();
            for (PsiClassType superType : next.getSuperTypes()) {
                for (PsiType parameter : superType.getParameters()) {
                    if (parameter.getCanonicalText().endsWith(val)) {
                        val = next.getName();
                        qualifiedName = next.getQualifiedName();
                        break;
                    }
                }
            }
        }
        PsiElementFactory instance = PsiElementFactory.getInstance(ProjectUtils.getCurrentProject());

        ArrayList<String> list = new ArrayList<>(IMPORT_LIST);
        if (StrUtil.isNotBlank(qualifiedName)) {
            list.add(qualifiedName);
        }
        //添加import
        WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
            for (String impor : list) {
                psiJavaFile.getImportList().add(instance.createImportStatement(PsiJavaFileUtil.getPsiClass(impor)));
            }
        });

        //添加Mapper
        text = StrUtil.format(COMMON_CODE, val) + text;

        return text;
    }


    public String getAutowiredField(PsiJavaFile psiJavaFile, String selectedText, boolean isQueryChain) {
        Project project = psiJavaFile.getProject();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        Map<String, String> qualifiedNameImportMap = PsiJavaFileUtil.getQualifiedNameImportMap(psiJavaFile);
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            PsiField[] allFields = psiClass.getAllFields();
            for (PsiField field : allFields) {
                if ((field.hasAnnotation(MybatisFlexConstant.ANNOTATION_AUTOWIRED)
                        || field.hasAnnotation(MybatisFlexConstant.ANNOTATION_RESOURCE))
                        && selectedText.contains(field.getName())) {

                    String text = field.getText().split("\n")[1].replace(";", "").trim();

                    if (!Character.isUpperCase(text.charAt(0))) {
                        text = StrUtil.subAfter(text, " ", false);
                    }
                    String className = StrUtil.subBefore(text, " ", true);
                    String qualifiedName = qualifiedNameImportMap.get(className);
                    // 获取全局搜索范围
                    Collection<PsiClass> implementors = PsiJavaFileUtil.getSonPsiClass(qualifiedName, GlobalSearchScope.allScope(project));
                    if (CollUtil.isEmpty(implementors)) {
                        LOG.error(StrUtil.format("未找到实现类:{}", qualifiedName));
                        if (!isQueryChain) {
                            return className;
                        }
                        continue;
                    }
                    PsiClass sonPsiClass = implementors.iterator().next();
                    String name = sonPsiClass.getName();
                    if (text.contains(field.getName())) {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            psiJavaFile.getImportList().add(elementFactory.createImportStatement(sonPsiClass));
                        });
                        selectedText = StrUtil.format("{}=new {}();\n", text, name) + selectedText;
                    }

                }
            }
        }
        return selectedText;
    }

    private void showSql(Project project, String packageName, VirtualFile virtualFile) {
        CompilerManagerUtil.compile(new VirtualFile[]{virtualFile}, (b, i, i1, compileContext) -> {
            try {
                WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
                    try {
                        virtualFile.delete(this);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
                // 执行配置
                ProgramRunner runner = ProgramRunner.PROGRAM_RUNNER_EP.getExtensions()[0];
                Executor instance = MyBatisLogExecutor.getInstance();
                RunManagerEx runManager = (RunManagerEx) RunManager.getInstance(project);
                RunnerAndConfigurationSettings defaultSettings = runManager.getSelectedConfiguration();
                ExecutionEnvironment environment = new ExecutionEnvironment(instance, runner, defaultSettings, project);
                // 创建 Java 执行配置
                JavaCommandLineState commandLineState = new JavaCommandLineState(environment) {
                    @Override
                    protected JavaParameters createJavaParameters() throws ExecutionException {
                        JavaParameters params = new JavaParameters();
                        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                        params.configureByProject(project, JavaParameters.JDK_AND_CLASSES, projectSdk);
                        params.setMainClass(packageName + ".MybatisFlexSqlPreview");
                        return params;
                    }
                };
                ExecutionResult executionResult = commandLineState.execute(instance, runner);
                if (ObjectUtil.isNotNull(executionResult) && ObjectUtil.isNotNull(executionResult)) {
                    ProcessHandler processHandler = executionResult.getProcessHandler();
                    processHandler.addProcessListener(new ProcessAdapter() {
                        @Override
                        public void onTextAvailable(ProcessEvent event1, Key outputType) {
                            if (ProcessOutputTypes.STDOUT.equals(outputType)) {
                                if (StrUtil.startWithAnyIgnoreCase(event1.getText(), "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER", "TRUNCATE")) {
                                    new SQLPreviewDialog(event1.getText()).setVisible(true);
                                }
                            } else if (ProcessOutputTypes.STDERR.equals(outputType)) {
                                System.out.println(event1.getText());
                                LOG.error("sql获取失败，请检查该类方法是否本身就存在错误");
                            }
                        }
                    });
                    processHandler.startNotify();
                }
            } catch (
                    Exception e) {
                Messages.showErrorDialog("Error executing code:\n" + e.getMessage(), "Code Execution Error");
            }
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String selectedText = e.getData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        PsiJavaFile psiFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
        preview(selectedText, psiFile, () -> {
        });
    }
}
