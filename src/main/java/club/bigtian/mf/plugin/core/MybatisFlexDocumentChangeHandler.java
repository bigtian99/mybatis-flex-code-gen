package club.bigtian.mf.plugin.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener, Disposable {

    private final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    public MybatisFlexDocumentChangeHandler() {
        super();
        // 所有的文档监听
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this, this);
    }


    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorCreated(event);
        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        // if (getPsiJavaFile(editor)) {
        document.addDocumentListener(this);
        // }
    }

    @Nullable
    private static boolean getPsiJavaFile(Editor editor) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        VirtualFile currentFile = fileDocumentManager.getFile(editor.getDocument());
        if (ObjectUtil.isNull(currentFile)) {
            return false;
        }
        PsiManager psiManager = PsiManager.getInstance(Objects.requireNonNull(editor.getProject()));
        PsiFile psiFile = psiManager.findFile(currentFile);
        // 支持java和kotlin
        if (!(psiFile instanceof PsiJavaFile) && !(psiFile instanceof KtFile)) {
            return false;
        }

        if (psiFile instanceof KtFile ktFile) {
            for (KtImportDirective anImport : Objects.requireNonNull(ktFile.getImportList()).getImports()) {
                if ("com.mybatisflex.annotation.Table".equals(Objects.requireNonNull(anImport.getImportedFqName()).asString())) {
                    return true;
                }
            }
        }
        if (psiFile instanceof PsiJavaFile psiJavaFile) {
            for (PsiImportStatement importStatement : Objects.requireNonNull(psiJavaFile.getImportList()).getImportStatements()) {
                if ("com.mybatisflex.annotation.Table".equals(importStatement.getQualifiedName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorReleased(event);
        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        // if (getPsiJavaFile(editor)) {
        document.removeDocumentListener(this);
        // }
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        EditorFactory.getInstance().editors(event.getDocument()).findAny().ifPresent(editor -> {
            boolean flag = getPsiJavaFile(editor);
            if (!flag) {
                return;
            }
            Runnable task = () -> {
                // 执行任务的逻辑
                Application application = ApplicationManager.getApplication();
                application.invokeLater(() -> {
                    compile(event);
                });
            };
            if (ObjectUtil.isNotNull(scheduledFuture)) {
                scheduledFuture.cancel(true);
            }
            // 延迟触发任务
            scheduledFuture = EXECUTOR_SERVICE.schedule(task, 800, TimeUnit.MILLISECONDS);
        });

    }


    private void compile(@NotNull DocumentEvent event) {
        EditorFactory.getInstance().editors(event.getDocument()).findFirst().ifPresent(editor -> {
            Project project = editor.getProject();
            CompilerManager compilerManager = CompilerManager.getInstance(project);

            PsiManager psiManager = PsiManager.getInstance(project);
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            VirtualFile currentFile = fileDocumentManager.getFile(editor.getDocument());
            if (ObjectUtil.isNull(currentFile)) {
                return;
            }
            PsiFile psiFile = psiManager.findFile(currentFile);
            if (!(psiFile instanceof PsiJavaFile) && !(psiFile instanceof KtFile)) {
                return;
            }
            PsiClassOwner psiJavaFile = (PsiClassOwner) psiFile;
            PsiErrorElement errorElement = PsiTreeUtil.findChildOfType(psiJavaFile, PsiErrorElement.class);
            if (ObjectUtil.isNull(errorElement) && !isPsiErrorElement(psiJavaFile)) {
                System.out.println("Task executed.");
                compilerManager.compile(new VirtualFile[]{currentFile}, null);
                // TODO Kotlin的编译暂时不支持
            }
        });
    }


    /**
     * 规避复制方法或者字段的时候也触发编译
     *
     * @param psiJavaFile
     * @return
     */
    private boolean isPsiErrorElement(PsiClassOwner psiJavaFile) {
        if (psiJavaFile instanceof KtFile) {
            return false;
        }
        HashSet<String> elementSet = new HashSet<>();
        PsiClass[] classes = psiJavaFile.getClasses();
        for (PsiClass psiClass : classes) {
            HashSet<Object> fieldSet = new HashSet<>();
            for (PsiField field : psiClass.getFields()) {
                String text = field.getText();
                if (elementSet.contains(text)) {
                    return true;
                } else {
                    elementSet.add(text);
                }
                fieldSet.add(field.getName());
            }
            PsiAnnotation annotation = psiClass.getAnnotation("lombok.Data");
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                // 解决用户cv方法，导致编译错误
                String text = psiMethod.getText();
                if (elementSet.contains(text)) {
                    return true;
                } else {
                    elementSet.add(text);
                }
                if (ObjectUtil.isNull(annotation)) {
                    // 解决用户修改字段后，get/set没有及时更新，导致编译报错
                    PsiCodeBlock body = psiMethod.getBody();
                    if (psiMethod.getName().startsWith("get")) {
                        String bodyText = body.getText();
                        String aReturn = StrUtil.subBetween(bodyText, "return ", ";").trim();
                        if (!fieldSet.contains(aReturn)) {
                            return true;
                        }
                    } else if (psiMethod.getName().startsWith("set")) {
                        String bodyText = body.getText();
                        String aReturn = StrUtil.subBetween(bodyText, "this.", "=").trim();
                        if (!fieldSet.contains(aReturn)) {
                            return true;
                        }
                    }
                }

            }


        }
        return false;
    }


    @Override
    public void dispose() {
        if (ObjectUtil.isNotNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
        EXECUTOR_SERVICE.shutdown();
        Disposer.dispose(this);
    }
}
