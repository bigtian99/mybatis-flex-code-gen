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
import org.jetbrains.kotlin.psi.KtFile;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * @author bigtian
 */
public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener, Disposable {
    private static final Logger LOG = Logger.getInstance(MybatisFlexDocumentChangeHandler.class);
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
        document.addDocumentListener(this);
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
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        EditorFactoryListener.super.editorReleased(event);
        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        document.removeDocumentListener(this);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        CharSequence newFragment = event.getNewFragment();
        if ((StrUtil.isBlank(newFragment) && StrUtil.isBlank(event.getOldFragment()))) {
            return;
        }


        Document document = event.getDocument();
        EditorFactory.getInstance().editors(document).findAny().ifPresent(editor -> {
            boolean flag = checkFile(editor);
            if (flag) {
                return;
            }
            Project project = editor.getProject();
            VirtualFile currentFile = VirtualFileUtils.getVirtualFile(editor.getDocument());
            if (ObjectUtil.isNull(currentFile)) {
                return;
            }
            PsiFile psiFile = VirtualFileUtils.getPsiFile(project, currentFile);
            if (!(psiFile instanceof PsiJavaFile) && !(psiFile instanceof KtFile)) {
                return;
            }
            PsiClassOwner psiJavaFile = (PsiClassOwner) psiFile;
            Runnable task = () -> {
                // 执行任务的逻辑
                Application application = ApplicationManager.getApplication();
                application.invokeLater(() -> {
                    PsiErrorElement errorElement = PsiTreeUtil.findChildOfType(psiJavaFile, PsiErrorElement.class);
                    if (ObjectUtil.isNull(errorElement) && !isPsiErrorElement(psiJavaFile)) {
                        compile(event);
                    }
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
            VirtualFile currentFile = VirtualFileUtils.getVirtualFile(editor.getDocument());
            LOG.warn("编译文件: " + currentFile.getName());
            compilerManager.compile(new VirtualFile[]{currentFile}, null);
            // TODO Kotlin的编译暂时不支持
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
                if (fieldSet.contains(field.getName())) {
                    return true;
                }
                fieldSet.add(field.getName());
            }
            PsiAnnotation annotation = psiClass.getAnnotation("lombok.Data");
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                // 解决用户cv方法，导致编译错误
                String text = psiMethod.getText();
                for (PsiAnnotation psiMethodAnnotation : psiMethod.getAnnotations()) {
                    text = text.replace(psiMethodAnnotation.getText(), "").trim();
                }
                PsiCodeBlock body = psiMethod.getBody();
                assert body != null;
                text = text.replace(body.getText(), "").trim();
                if (elementSet.contains(text)) {
                    return true;
                } else {
                    elementSet.add(text);
                }
                //TODO 注解必填项不填，导致编译错误
                //get/set方法
                if (ObjectUtil.isNull(annotation) && (psiMethod.getName().startsWith("get") || psiMethod.getName().startsWith("set"))) {
                    // 解决用户修改字段后，get/set没有及时更新，导致编译报错
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
