package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.entity.AptInfo;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.velocity.VelocityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author bigtian
 */
public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener, Disposable, FileEditorManagerListener {
    private static final Logger LOG = Logger.getInstance(MybatisFlexDocumentChangeHandler.class);
    private static final Key<Boolean> CHANGE = Key.create("change" );
    private static final Key<Boolean> LISTENER = Key.create("listener" );

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditor oldEditor = event.getOldEditor();
        if (ObjectUtil.isNotNull(oldEditor)) {
            VirtualFile oldFile = event.getOldFile();
            createAptFile(oldFile);
        }

    }

    private static void createAptFile(VirtualFile oldFile) {
        Boolean userData = oldFile.getUserData(CHANGE);
        if (BooleanUtil.isTrue(userData) && checkFile(oldFile)) {
            // 检查索引是否已准备好
            Project project = ProjectUtils.getCurrentProject();
            oldFile.putUserData(CHANGE, false);
            PsiClassOwner psiJavaFile = (PsiClassOwner) VirtualFileUtils.getPsiFile(project, oldFile);
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            Module moduleForFile = ModuleUtil.findModuleForFile(oldFile, project);
            PsiField[] fields = psiClass.getFields();
            List<AptInfo> list = new ArrayList<>();
            for (PsiField field : fields) {
                PsiAnnotation column = field.getAnnotation("com.mybatisflex.annotation.Column" );
                if (ObjectUtil.isNotNull(column)) {
                    PsiAnnotationMemberValue value = column.findAttributeValue("value" );
                    String fieldName = value.getText().replace("\"", "" );
                    list.add(new AptInfo(fieldName, StrUtil.toUnderlineCase(field.getName()).toUpperCase()));
                } else {
                    list.add(new AptInfo(field.getName(), StrUtil.toUnderlineCase(field.getName()).toUpperCase()));
                }
            }
            String path = StrUtil.subBefore(moduleForFile.getModuleFilePath(), ".idea", false) + (Modules.isManvenProject(moduleForFile)
                    ? "target/generated-sources/annotations/" : "build/generated/source/kapt/main/" )
                    + psiJavaFile.getPackageName().replace(".", "/" ) + "/table";

            PsiAnnotation table = psiClass.getAnnotation("com.mybatisflex.annotation.Table" );
            PsiDirectory psiDirectory = VirtualFileUtils.createSubDirectory(moduleForFile, path);
            VelocityContext context = new VelocityContext();
            String className = psiClass.getName() + "TableDef";
            context.put("className", className);
            context.put("packageName", psiJavaFile.getPackageName() + ".table" );
            context.put("list", list);
            context.put("instance", StrUtil.toUnderlineCase(psiClass.getName()).toUpperCase());
            context.put("talbeName", table.findAttributeValue("value" ).getText().replace("\"", "" ));
            String suffix = Modules.getProjectTypeSuffix(moduleForFile);
            String fileName = className + suffix;
            PsiFile psiFile = VelocityUtils.render(context, Template.getTemplateContent("AptTemplate" + suffix), fileName);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiFile file = psiDirectory.findFile(fileName);
                if (ObjectUtil.isNotNull(file)) {
                    file.getViewProvider().getDocument().setText(psiFile.getText());
                } else {
                    psiDirectory.add(psiFile);
                }
            });
        }
    }


    public MybatisFlexDocumentChangeHandler() {
        super();

        // 所有的文档监听
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this, this);
        Document document;

        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            ProjectUtils.setCurrentProject(editor.getProject());
            document = editor.getDocument();
            document.putUserData(LISTENER, true);
            editor.addEditorMouseListener(new EditorMouseListener() {
                @Override
                public void mouseExited(@NotNull EditorMouseEvent event) {
                    createAptFile(VirtualFileUtils.getVirtualFile(editor.getDocument()));
                }
            });
        }
        FileEditorManager.getInstance(ProjectUtils.getCurrentProject()).addFileEditorManagerListener(this);

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
                createAptFile(VirtualFileUtils.getVirtualFile(editor.getDocument()));
            }
        });
        Document document = editor.getDocument();
        if (Boolean.TRUE.equals(document.getUserData(LISTENER))) {
            document.putUserData(LISTENER, true);
            document.addDocumentListener(this);
        }
        ProjectUtils.setCurrentProject(editor.getProject());
    }






    private static boolean checkFile(VirtualFile currentFile) {
        if (ObjectUtil.isNull(currentFile)) {
            return false;
        }
        PsiManager psiManager = PsiManager.getInstance(ProjectUtils.getCurrentProject());
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
        return importSet.contains("com.mybatisflex.annotation.Table" );
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        CharSequence newFragment = event.getNewFragment();
        if ((StrUtil.isBlank(newFragment) && StrUtil.isBlank(event.getOldFragment()))) {
            return;
        }
        VirtualFile currentFile = VirtualFileUtils.getVirtualFile(document);
        if (ObjectUtil.isNotNull(currentFile)) {
            currentFile.putUserData(CHANGE, true);
        }
    }

    private void compile(@NotNull Editor editor) {
        VirtualFile currentFile = VirtualFileUtils.getVirtualFile(editor.getDocument());
        CompilerManagerUtil.compile(new VirtualFile[]{currentFile}, null);
    }


    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
