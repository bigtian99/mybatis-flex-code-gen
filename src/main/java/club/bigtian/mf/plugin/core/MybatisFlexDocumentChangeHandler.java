package club.bigtian.mf.plugin.core;

import cn.hutool.core.util.ObjectUtil;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    public MybatisFlexDocumentChangeHandler() {
        super();
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            Document document = editor.getDocument();
            if (getPsiJavaFile(editor)) {
                document.addDocumentListener(this);
            }
        }
    }


    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorCreated(event);
        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        if (getPsiJavaFile(editor)) {
            document.addDocumentListener(this);
        }
    }

    @Nullable
    private static boolean getPsiJavaFile(Editor editor) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        VirtualFile currentFile = fileDocumentManager.getFile(editor.getDocument());
        if (ObjectUtil.isNull(currentFile)) {
            return false;
        }
        PsiManager psiManager = PsiManager.getInstance(editor.getProject());
        PsiFile psiFile = psiManager.findFile(currentFile);
        if (!(psiFile instanceof PsiJavaFile)) {
            return false;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        return psiJavaFile.getText().contains("com.mybatisflex.annotation.Table");
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorReleased(event);
        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        if (getPsiJavaFile(editor)) {
            document.removeDocumentListener(this);
        }
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {

        Runnable task = () -> {
            // 执行任务的逻辑
            Application application = ApplicationManager.getApplication();
            application.invokeLater(() -> {
                // 在正确的上下文中执行的代码
                // 修改模型、更新界面等操作
                compile(event);
            });
        };
        if (ObjectUtil.isNotNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
        // 延迟触发任务
        scheduledFuture = executorService.schedule(task, 800, TimeUnit.MILLISECONDS);
    }


    private void compile(@NotNull DocumentEvent event) {
        if (event.getOldLength() == 0 && event.getNewLength() == 0) {
            return;
        }
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
            if (!(psiFile instanceof PsiJavaFile)) {
                return;
            }
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiErrorElement errorElement = PsiTreeUtil.findChildOfType(psiJavaFile, PsiErrorElement.class);
            if (currentFile != null && ObjectUtil.isNull(errorElement) && !isPsiErrorElement(psiJavaFile)) {
                System.out.println("Task executed.");
                compilerManager.compile(new VirtualFile[]{currentFile}, null);
            }
        });
    }

    /**
     * 规避复制方法或者字段的时候也触发编译
     *
     * @param psiJavaFile
     * @return
     */
    private boolean isPsiErrorElement(PsiJavaFile psiJavaFile) {
        HashSet<String> elementSet = new HashSet<>();
        PsiClass[] classes = psiJavaFile.getClasses();
        for (PsiClass psiClass : classes) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                String text = psiMethod.getText();
                if (elementSet.contains(text)) {
                    return true;
                } else {
                    elementSet.add(text);
                }
            }
            for (PsiField field : psiClass.getFields()) {
                String text = field.getText();
                if (elementSet.contains(text)) {
                    return true;
                } else {
                    elementSet.add(text);
                }
            }
        }

        return false;
    }
}
