package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.CustomConfig;
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
import com.intellij.testFramework.LightVirtualFile;
import org.apache.velocity.VelocityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.*;


/**
 * @author bigtian
 */
public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener, Disposable, FileEditorManagerListener {
    private static final Logger LOG = Logger.getInstance(MybatisFlexDocumentChangeHandler.class);
    private static final Key<Boolean> CHANGE = Key.create("change" );
    private static final Key<Boolean> LISTENER = Key.create("listener" );

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        // try {
        //     FileEditor oldEditor = event.getOldEditor();
        //     if (ObjectUtil.isNotNull(oldEditor)) {
        //         VirtualFile oldFile = event.getOldFile();
        //         createAptFile(oldFile);
        //     }
        // } catch (Exception e) {
        //
        // }
    }

    private static void createAptFile(VirtualFile oldFile) {
        if (ObjectUtil.isNull(oldFile) || !oldFile.getName().endsWith(".java" )) {
            return;
        }
        Boolean userData = oldFile.getUserData(CHANGE);
        Project project = ProjectUtils.getCurrentProject();
        Module moduleForFile = ModuleUtil.findModuleForFile(oldFile, project);
        CustomConfig config = Modules.moduleConfig(moduleForFile);
        if (BooleanUtil.isTrue(userData) && checkFile(oldFile) && ObjectUtil.defaultIfNull(config.isEnable(), true)) {
            // 检查索引是否已准备好
            oldFile.putUserData(CHANGE, false);
            PsiClassOwner psiJavaFile = (PsiClassOwner) VirtualFileUtils.getPsiFile(project, oldFile);
            PsiClass psiClass = psiJavaFile.getClasses()[0];
            String moduleDirPath = Modules.getPath(moduleForFile);
            PsiField[] fields = psiClass.getAllFields();
            List<AptInfo> list = new ArrayList<>();
            for (PsiField field : fields) {
                if (field.getName().startsWith("queryWrapper" )) {
                    continue;
                }
                PsiAnnotation column = field.getAnnotation("com.mybatisflex.annotation.Column" );
                if (ObjectUtil.isNotNull(column)) {
                    PsiAnnotationMemberValue value = column.findAttributeValue("value" );
                    String fieldName = value.getText().replace("\"", "" );
                    list.add(new AptInfo(fieldName, StrUtil.toUnderlineCase(field.getName()).toUpperCase()));
                } else {
                    list.add(new AptInfo(field.getName(), StrUtil.toUnderlineCase(field.getName()).toUpperCase()));
                }
            }


            String path = moduleDirPath + CustomConfig.getConfig(config.getGenPath(),
                    "target/generated-sources/annotations/",
                    "build/generated/source/kapt/main/", Modules.isManvenProject(moduleForFile))
                    + psiJavaFile.getPackageName().replace(".", "/" ) + "/table";

            PsiAnnotation table = psiClass.getAnnotation("com.mybatisflex.annotation.Table" );
            PsiDirectory psiDirectory = VirtualFileUtils.createSubDirectory(moduleForFile, path);
            VelocityContext context = new VelocityContext();
            String className = psiClass.getName() + ObjectUtil.defaultIfEmpty(config.getTableDefClassSuffix(), "TableDef" );
            context.put("className", className);
            context.put("packageName", psiJavaFile.getPackageName() + "." + ObjectUtil.defaultIfEmpty(config.getAllInTablesPackage(), "table" ));
            context.put("list", list);
            context.put("instance", getDefInstanceName(config, psiClass.getName()));
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

    public static String getDefInstanceName(CustomConfig config, String className) {
        String type = ObjectUtil.defaultIfNull(config.getTableDefPropertiesNameStyle(), "upperCase" );
        String ignoreEntitySuffixes = config.getTableDefIgnoreEntitySuffixes();
        String[] suffixes = ObjectUtil.defaultIfEmpty(ignoreEntitySuffixes, "" ).split("," );
        String target = Arrays.stream(suffixes).filter(s -> className.endsWith(s.trim())).findFirst().orElse("" ).trim();
        String instace = StrUtil.toUnderlineCase(className.replace(target, "" ));
        switch (type) {
            case "upperCase":
                return instace.toUpperCase();
            case "lowerCase":
                return instace.toLowerCase();
            case "upperCamelCase":
                return StrUtil.upperFirst(StrUtil.toCamelCase(instace));
            case "lowerCamelCase":
                return StrUtil.lowerFirst(StrUtil.toCamelCase(instace));
            default:
                return instace.toUpperCase();
        }
    }


    public MybatisFlexDocumentChangeHandler() {
        super();
        try {
            NotificationUtils.start();
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
            Project project = ProjectUtils.getCurrentProject();
            if (ObjectUtil.isNull(project)) {
                return;
            }
            FileEditorManager.getInstance(project).addFileEditorManagerListener(this);
        } catch (Exception e) {

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
        try {
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
        } catch (Exception e) {

        }
    }


    private static boolean checkFile(VirtualFile currentFile) {
        if (ObjectUtil.isNull(currentFile) || currentFile instanceof LightVirtualFile) {
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
            importSet = PsiJavaFileUtil.getQualifiedNameImportSet(psiJavaFile);
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
