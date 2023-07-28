package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.log.MyBatisLogExecutor;
import club.bigtian.mf.plugin.core.util.CodeReformat;
import club.bigtian.mf.plugin.core.util.CompilerManagerUtil;
import club.bigtian.mf.plugin.windows.SQLPreviewDialog;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.util.IncorrectOperationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLPreviewAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        PsiJavaFile psiFile = (PsiJavaFile) event.getData(CommonDataKeys.PSI_FILE);

        if (project == null || psiFile == null) {
            Messages.showInfoMessage("Please select some Java code to execute.", "No Code Selected");
            return;
        }

        PsiElement psiElement = psiFile.findElementAt(event.getData(CommonDataKeys.CARET).getOffset());
        if (psiElement == null) {
            Messages.showErrorDialog("Cannot find Java element at caret.", "Code Execution Error");
            return;
        }

        // 获取选中代码片段的文本
        String selectedText = event.getData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        String variable = StrUtil.subBetween(selectedText, "QueryWrapper ", " =");
        selectedText += StrUtil.format("\nSystem.out.println({}.toSQL());", variable);
        String text = "public class MybatisFlexSqlPreview {\n    public static void main(String[] args) {" + selectedText + "\n}\n}";
        String packageName = psiFile.getPackageName();
        PsiJavaFile psiJavaFile = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText("MybatisFlexSqlPreview.java", JavaFileType.INSTANCE, text);
        psiJavaFile.setPackageName(psiFile.getPackageName());
        psiJavaFile.getImportList().add(psiFile.getImportList());
        PsiDirectory containingDirectory = psiFile.getContainingDirectory();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                List<String> list = Arrays.stream(containingDirectory.getFiles()).map(PsiFile::getName).collect(Collectors.toList());
                if (list.contains(psiJavaFile.getName())) {
                    PsiFile file = containingDirectory.findFile(psiJavaFile.getName());
                    file.delete();
                }
                PsiElement element = containingDirectory.add(CodeReformat.reformat(psiJavaFile));
                PsiJavaFileImpl psiJava = (PsiJavaFileImpl) element;
                VirtualFile virtualFile = psiJava.getContainingFile().getVirtualFile();
                try {
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
                            params.setMainClass("club.bigtian.study.domain.table.MybatisFlexSqlPreview");
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
                                    new SQLPreviewDialog(event1.getText()).setVisible(true);
                                }
                            }
                        });
                        processHandler.startNotify();

                    }
                } catch (
                        Exception e) {
                    Messages.showErrorDialog("Error executing code:\n" + e.getMessage(), "Code Execution Error");
                }

                CompilerManagerUtil.compile(new VirtualFile[]{virtualFile}, (b, i, i1, compileContext) -> {
                    try {
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
                                params.setMainClass(packageName+".MybatisFlexSqlPreview");
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
                                        new SQLPreviewDialog(event1.getText()).setVisible(true);
                                    } else if (ProcessOutputTypes.STDERR.equals(outputType)) {
                                        System.out.println(event1.getText());
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
            } catch (IncorrectOperationException e) {
                System.out.println(e.getMessage());
            }
        });

    }
}
