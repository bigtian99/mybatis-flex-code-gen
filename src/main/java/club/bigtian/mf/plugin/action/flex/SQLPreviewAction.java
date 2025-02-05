package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.function.ManyFunction;
import club.bigtian.mf.plugin.core.function.SimpleFunction;
import club.bigtian.mf.plugin.core.log.MyBatisLogExecutor;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.windows.MybatisFlexSettingDialog;
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
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SQLPreviewAction extends AnAction {

    private static final List<String> IMPORT_LIST = Arrays.asList("com.mybatisflex.core.FlexGlobalConfig",
            "com.mybatisflex.core.mybatis.FlexConfiguration",
            "org.springframework.jdbc.datasource.DelegatingDataSource",
            "org.apache.ibatis.mapping.Environment",
            "com.mybatisflex.core.dialect.DbType",
            "org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory");

    private static final String COMMON_CODE =
            "    Environment environment = new Environment(\"mybatisFlex\", new JdbcTransactionFactory(), new DelegatingDataSource());\n" +
                    "        FlexConfiguration configuration = new FlexConfiguration(environment);\n" +
                    "        FlexGlobalConfig globalConfig = FlexGlobalConfig.getDefaultConfig();\n" +
                    "        globalConfig.setConfiguration(configuration);\n" +
                    "        FlexGlobalConfig.setConfig(\"mybatisFlex\", globalConfig, true);\n" +
                    "        configuration.addMapper({}.class);\n"+
                    "           globalConfig.setDbType(DbType.{});\n";
    private static final String TEMPLATE =
            "        {} baseMapper = Mappers.ofMapperClass({}.class);\n" +
            "        Field field = ReflectUtil.getField(ServiceImpl.class, \"mapper\");\n" +
            "        ReflectUtil.setFieldValue({}, field, baseMapper);";

    private static final List<String> SERVICE_IMPORT_LIST = Arrays.asList("cn.hutool.core.util.ReflectUtil",
            "com.mybatisflex.core.mybatis.Mappers",
            "java.lang.reflect.Field", "com.mybatisflex.spring.service.impl.ServiceImpl");
    private static final String SYSTEM_OUT_PRINTLN_TO_SQL = "\nSystem.out.println({}.toSQL());";
    private static final String CLASS_TEMPLATE = "public class MybatisFlexSqlPreview {\n    public static void main(String[] args) { {}\n}\n}";

    private PsiClass entityClass;
    PsiMethod constructorMethod;
    List<String> list;
    PsiElementFactory instance = PsiElementFactory.getInstance(ProjectUtils.getCurrentProject());

    /**
     * 排除 import 导入包
     */
    private final List<String> excludeImportList = Arrays.asList("org.mapstruct.factory.Mappers");
    AnActionEvent event;

    public void preview(String selectedText, PsiJavaFile psiFile,AnActionEvent actionEvent, SimpleFunction function) {
        try {
            this.event = actionEvent;
            if (selectedText.startsWith("QueryWrapper")) {
                selectedText = StrUtil.format(SYSTEM_OUT_PRINTLN_TO_SQL, selectedText);
            } else {
                // 处理链式调用
                selectedText = chain(selectedText, psiFile);
            }
            createFile(psiFile, StrUtil.format(CLASS_TEMPLATE, selectedText), psiFile.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // new File("MybatisFlexSqlPreview").delete();
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
            PsiImportList psiJavaFileImportList = psiJavaFile.getImportList();
            if (ObjectUtil.isNotNull(importList)) {
                Arrays.stream(importList.getAllImportStatements())
                        .filter(el -> {
                            String anImport = StrUtil.subBetween(el.getText(), "import ", ";").trim();
                            return !excludeImportList.contains(anImport);
                        })
                        .forEach(el -> psiJavaFileImportList.add(el));
            }
            if (CollUtil.isNotEmpty(list)) {
                for (String impor : list) {
                    psiJavaFileImportList.add(instance.createImportStatement(PsiJavaFileUtil.getPsiClass(impor)));
                }
            }
            try {
                PsiFile file = containingDirectory.findFile(psiJavaFile.getName());
                if (ObjectUtil.isNotNull(file)) {
                    file.delete();
                }

                PsiElement element = containingDirectory.add(CodeReformat.reformat(psiJavaFile));
                VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
                showSql(project, packageName, virtualFile);
                // compileJavaCode(psiJavaFile.getText());
                // showSql(project, packageName, null);

            } catch (Exception e) {
                e.printStackTrace();
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
            text = StrUtil.subAfter(text, "=", false).trim();
        }
        Map<String, String> qualifiedNameImportMap = PsiJavaFileUtil.getQualifiedNameImportMap(psiJavaFile);

        String val = null;
        if (text.startsWith("this.")) {
            text = StrUtil.subAfter(text, "this.", false);
        }
        // 处理service里面的链式调用
        String print = StrUtil.format(SYSTEM_OUT_PRINTLN_TO_SQL, StrUtil.startWithAny(text, "queryChain()", "query()", "updateChain()") ? "service." + text : text);
        boolean flag = StrUtil.containsAny(text, "queryChain()", "query()", "updateChain()");
        AtomicReference<String> variableReference = new AtomicReference<>();
        String ofValue = null;
        if (flag) {
            AtomicReference<String> atomicReference = new AtomicReference<>();
            text = "\n" + getImplText(psiJavaFile, text, qualifiedNameImportMap, (entityClass, variableTem) -> {
                atomicReference.set(entityClass);
                variableReference.set(variableTem);
            });
            val = atomicReference.get();
        } else {
            ofValue = getOfValue(text, qualifiedNameImportMap, psiJavaFile);
            if (ObjectUtil.isNotNull(ofValue)) {
                if (!ofValue.contains(":")) {
                    val = ofValue;
                }
            } else {
                val = null;
            }
            text = "";
        }
        Collection<PsiClass> implementors =
                PsiJavaFileUtil.getSonPsiClass(MybatisFlexConstant.MYBATISFLEX_CORE_BASE_MAPPER,
                        GlobalSearchScope.allScope(ProjectUtils.getCurrentProject()));
        Iterator<PsiClass> iterator = implementors.iterator();
        String qualifiedName = "";
        // 判断是不是对应的mapper
        if (StrUtil.isNotBlank(val)) {
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
        }
        list = new ArrayList<>(IMPORT_LIST);
        if (ObjectUtil.isNotNull(ofValue) && ofValue.contains(":")) {
            String[] valueArr = ofValue.split(":");
            qualifiedName = valueArr[1];
            val = valueArr[0];
            print = print.replace("(this)", StrUtil.format("(Mappers.ofMapperClass({}.class))", val));
            list.add("com.mybatisflex.core.mybatis.Mappers");
        }
        if (StrUtil.isNotBlank(qualifiedName)) {
            list.add(qualifiedName);
        }
        if (flag) {
            text += StrUtil.format(TEMPLATE, val, val, variableReference.get());
            // 导入新的
            list.addAll(SERVICE_IMPORT_LIST);
        }
        // 添加import
        WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
            if (ObjectUtil.isNotNull(entityClass)) {
                boolean hashConstructor = isHasConstructor(entityClass);
                if (!hashConstructor) {
                    constructorMethod = instance.createMethodFromText(StrUtil.format("public {}(){}", entityClass.getName(), "{}"), null);
                    entityClass.add(constructorMethod);
                }
            }
        });

        text = StrUtil.format(COMMON_CODE, ObjectUtil.defaultIfNull(val,
                "Object"), Template.getConfigData(MybatisFlexConstant.SQL_DIALECT,"mysql")) + text + print;

        // if (StrUtil.isNotBlank(val)) {
        //     // 添加Mapper
        //     text = StrUtil.format(COMMON_CODE, val) + text + print;
        //
        // } else {
        //     text += print;
        // }

        return text;
    }

    private String getOfValue(String text, Map<String, String> qualifiedNameImportMap, PsiJavaFile psiJavaFile) {
        String val = "";
        if (text.contains("of(")) {
            val = StrUtil.subBetween(text, "of(", ")");
        } else if (text.contains("create(")) {
            if (text.contains("create()")) {
                return null;
            }
            val = StrUtil.subBetween(text, "create(", ")");
        } else {
            return null;
//        //获取类
        }
        String entityName = null;

        if (val.startsWith("Mappers")) {
            val = StrUtil.subBetween(val, "(", ".class");

        } else if ("this".equals(val)) {
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            entityName = StrUtil.subBetween(psiClass.getText(), "BaseMapper<", ">");
            val = psiClass.getName() + ":" + psiClass.getQualifiedName();
        } else {
            val = StrUtil.subBefore(val, ".", false);
        }

        entityClass = PsiJavaFileUtil.getPsiClass(qualifiedNameImportMap.get(ObjectUtil.defaultIfNull(entityName, val)));

        return val;
    }

    private boolean isHasConstructor(PsiClass entityClass) {
        boolean hashConstructor = false;
        PsiMethod[] constructors = entityClass.getConstructors();
        for (PsiMethod constructor : constructors) {
            if (constructor.getText().startsWith("public") && constructor.getParameters().length == 0) {
                hashConstructor = true;
                break;
            }
        }
        return hashConstructor;
    }

    private void removeNoArgsConstructor(PsiClass entityClass) {
        PsiMethod[] constructors = entityClass.getConstructors();
        for (PsiMethod constructor : constructors) {
            if (constructor.getText().startsWith("public") && constructor.getParameters().length == 0) {
                constructor.delete();
                break;
            }
        }
    }


    public String getImplText(PsiJavaFile psiJavaFile, String selectedText, Map<String, String> qualifiedNameImportMap, ManyFunction<String> consumer) {
        String temVal = "";
        Project project = psiJavaFile.getProject();
        PsiClass[] classes = psiJavaFile.getClasses();
        if (StrUtil.startWithAny(selectedText, "query()", "queryChain()", "updateChain()")) {
            String name = StrUtil.subBefore(psiJavaFile.getName(), ".", true);
            temVal = StrUtil.format("{} service=new {}();\n", name, name);
            String text = psiJavaFile.getText();
            String mapper = StrUtil.subBetween(text, "ServiceImpl<", ",").trim();
            consumer.applay(qualifiedNameImportMap.get(mapper), "service");
            return temVal;
        }
        for (PsiClass psiClass : classes) {
            PsiField[] allFields = psiClass.getAllFields();
            for (PsiField field : allFields) {
                if (selectedText.contains(field.getName())) {
                    String text = field.getText();
                    if (text.contains("=")) {
                        text = StrUtil.subBefore(text, "=", true);
                    }
                    if (text.contains("\n")) {
                        String[] split = text.split("\n");
                        text = split[split.length - 1];
                    }
                    text = text.replace(";", "").trim();
                    while (!Character.isUpperCase(text.charAt(0))) {
                        text = StrUtil.subAfter(text, " ", false);
                    }
                    String className = StrUtil.subBefore(text, " ", true);
                    String qualifiedName = qualifiedNameImportMap.get(className);
                    // 获取全局搜索范围
                    Collection<PsiClass> implementors = PsiJavaFileUtil.getSonPsiClass(qualifiedName, GlobalSearchScope.allScope(project));
                    if (CollUtil.isEmpty(implementors)) {
                        // 如果没有找到子类，就默认自身是实现类
                        implementors.add(PsiJavaFileUtil.getPsiClass(qualifiedName));
                    }
                    PsiClass sonPsiClass = implementors.iterator().next();
                    String genericity = PsiJavaFileUtil.getGenericity(sonPsiClass);
                    String name = sonPsiClass.getName();
                    consumer.applay(genericity, StrUtil.subAfter(text, " ", true));
                    if (text.contains(field.getName())) {
                        temVal = StrUtil.format("{}=new {}();\n", text, name);
                    }
                }
            }
        }
        return temVal;
    }


    private void showSql(Project project, String packageName, VirtualFile virtualFile) {
        ArrayList<VirtualFile> virtualFiles = new ArrayList<VirtualFile>();
        if (ObjectUtil.isNotNull(entityClass)) {
            virtualFiles.add(entityClass.getContainingFile().getVirtualFile());
        }
        virtualFiles.add(virtualFile);
        CompilerManagerUtil.compile(virtualFiles.toArray(new VirtualFile[0]), (b, i, i1, compileContext) -> {
        try {
                ApplicationManager.getApplication().invokeLater(() -> {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            if (!MybatisFlexSettingDialog.insideSchemaFlag) {
                                virtualFile.delete(this);
                            }
                            if (ObjectUtil.isNotNull(entityClass)) {
                                removeNoArgsConstructor(entityClass);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }, ModalityState.defaultModalityState());
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
                    params.configureByProject(project, JavaParameters.JDK_AND_CLASSES_AND_TESTS, projectSdk);
                    // params.setMainClass(packageName + ".MybatisFlexSqlPreview");
                    params.setMainClass(packageName + ".MybatisFlexSqlPreview");
                    return params;
                }
            };
            ExecutionResult executionResult = commandLineState.execute(instance, runner);
            if (ObjectUtil.isNotNull(executionResult)) {
                ProcessHandler processHandler = executionResult.getProcessHandler();
                processHandler.addProcessListener(new ProcessAdapter() {
                    @Override
                    public void onTextAvailable(ProcessEvent event1, Key outputType) {
                        if (ProcessOutputTypes.STDOUT.equals(outputType)) {
                            if (StrUtil.startWithAnyIgnoreCase(event1.getText(), "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER", "TRUNCATE","WITH")) {
                                new SQLPreviewDialog(event1.getText(),event).setVisible(true);
                            }
                        } else if (ProcessOutputTypes.STDERR.equals(outputType)) {
                            String text = event1.getText();
                            System.out.println(text);
                            if (text.startsWith("Exception in")) {
                                NotificationUtils.notifyError((StrUtil.subAfter(text, ":", true)), "Mybatis-Flex system tips");
                            }
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
        // 如果是从项目视图中右键点击的进来的则创建新的类
        String selectedText = e.getData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        if (StrUtil.isNotBlank(selectedText)) {
            PsiJavaFile psiFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
            preview(selectedText, psiFile,e, () -> {
            });
            return;
        }

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(PsiJavaFileUtil.isFlexProject());
    }
}
