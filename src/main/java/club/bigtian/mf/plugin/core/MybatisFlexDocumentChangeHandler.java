package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.util.KtFileUtil;
import club.bigtian.mf.plugin.core.util.PsiJavaFileUtil;
import club.bigtian.mf.plugin.core.util.VirtualFileUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author bigtian
 */
public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener , Disposable {
    private static final Logger LOG = Logger.getInstance(MybatisFlexDocumentChangeHandler.class);
    private static final Key<Boolean> CHANGE = Key.create("change");
    private static final Key<Boolean> LISTENER = Key.create("listener");

    public MybatisFlexDocumentChangeHandler() {
        super();
        // 所有的文档监听
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this, this);
        Document document;
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            document = editor.getDocument();
            document.putUserData(LISTENER, true);
            editor.addEditorMouseListener(new EditorMouseListener() {
                @Override
                public void mouseExited(@NotNull EditorMouseEvent event) {
                    executeCompile(editor);
                }
            });
        }
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();

        Document document = editor.getDocument();
        if (Boolean.TRUE.equals(document.getUserData(LISTENER))) {
            document.putUserData(LISTENER, false);
            document.removeDocumentListener(this);
        }
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorCreated(event);
        Editor editor = event.getEditor();
        editor.addEditorMouseListener(new EditorMouseListener() {
            @Override
            public void mouseExited(@NotNull EditorMouseEvent event) {
                executeCompile(editor);
            }
        });
        Document document = editor.getDocument();
        if (Boolean.TRUE.equals(document.getUserData(LISTENER))) {
            document.putUserData(LISTENER, true);
            document.addDocumentListener(this);
        }
    }

    private void executeCompile(Editor editor) {
        Document document = editor.getDocument();
        boolean flag = checkFile(editor);
        Project project = editor.getProject();
        VirtualFile currentFile = VirtualFileUtils.getVirtualFile(document);
        if (ObjectUtil.isNull(currentFile)) {
            return;
        }
        PsiFile psiFile = VirtualFileUtils.getPsiFile(project, currentFile);
        if (flag || ObjectUtil.isNull(currentFile) || !(psiFile instanceof PsiJavaFile) && !(psiFile instanceof KtFile)
                || !ObjectUtil.defaultIfNull(document.getUserData(CHANGE), false)) {
            return;
        }
        document.putUserData(CHANGE, false);
        // 执行任务的逻辑
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            compile(editor);
        });
    }


    /**
     * 校验文件是否导入了Table注解
     *
     * @param editor
     * @return
     */
    private static boolean checkFile(Editor editor) {
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
        Set<String> importSet = new HashSet<>();
        if (psiFile instanceof KtFile) {
            KtFile ktFile = (KtFile) psiFile;
            importSet = KtFileUtil.getImportSet(ktFile);
        }
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            importSet = PsiJavaFileUtil.getImportSet(psiJavaFile);
        }
        return !importSet.contains("import com.mybatisflex.annotation.Table;");
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        CharSequence newFragment = event.getNewFragment();
        if ((StrUtil.isBlank(newFragment) && StrUtil.isBlank(event.getOldFragment()))) {
            return;
        }
        document.putUserData(CHANGE, true);
    }

    private void compile(@NotNull Editor editor) {
        Project project = editor.getProject();
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        VirtualFile currentFile = VirtualFileUtils.getVirtualFile(editor.getDocument());
        LOG.warn("编译文件: " + currentFile.getName());
        compilerManager.compile(new VirtualFile[]{currentFile}, null);
    }


    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
