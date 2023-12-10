package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.CustomConfig;
import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.entity.AptInfo;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author bigtian
 */
public class MybatisFlexDocumentChangeHandler implements DocumentListener, EditorFactoryListener, Disposable, FileEditorManagerListener {
    public static final Key<Boolean> CHANGE = Key.create("change");
    private static final Key<Boolean> LISTENER = Key.create("listener");
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {

    }

    public static void createAptFile(List<VirtualFile> virtualFiles) {
        Project project = ProjectUtils.getCurrentProject();
        virtualFiles = virtualFiles.stream()
                .filter(oldFile -> {
                    if (ObjectUtil.isNull(oldFile)) {
                        return false;
                    }
                    Boolean userData = oldFile.getUserData(CHANGE);
                    return !(ObjectUtil.isNull(oldFile) || !oldFile.getName().endsWith(".java") || !oldFile.isWritable()) && BooleanUtil.isTrue(userData) && checkFile(oldFile);
                }).collect(Collectors.toList());
        Map<PsiDirectory, List<PsiFile>> psiDirectoryMap = new HashMap<>();
        try {

            // 检查索引是否已准备好
            for (VirtualFile oldFile : virtualFiles) {
                Module moduleForFile = ModuleUtil.findModuleForFile(oldFile, project);
                CustomConfig config = Modules.moduleConfig(moduleForFile);
                if (!ObjectUtil.defaultIfNull(config.isEnable(), true)) {
                    continue;
                }
                String moduleDirPath = Modules.getPath(moduleForFile);
                PsiClassOwner psiJavaFile = (PsiClassOwner) VirtualFileUtils.getPsiFile(project, oldFile);

                String path = moduleDirPath + CustomConfig.getConfig(config.getGenPath(),
                        "target/generated-sources/annotations/",
                        "build/generated/source/kapt/main/", Modules.isManvenProject(moduleForFile))
                        + psiJavaFile.getPackageName().replace(".", "/") + "/table";

                PsiDirectory psiDirectory = VirtualFileUtils.createSubDirectory(moduleForFile, path);
                // 等待索引准备好
                DumbService.getInstance(project).runWhenSmart(() -> {
                    // 在智能模式下，执行需要等待索引准备好的操作，比如创建文件
                    // 创建文件等操作代码
                    oldFile.putUserData(CHANGE, false);
                    PsiClass psiClass = psiJavaFile.getClasses()[0];
                    PsiField[] fields = psiClass.getAllFields();
                    Map<String, AptInfo> fieldMap = new HashMap<>();
                    for (PsiField field : fields) {
                        if (field.getName().startsWith("queryWrapper") || PsiJavaFileUtil.checkFieldModifiers(field)) {
                            continue;
                        }
                        PsiAnnotation column = field.getAnnotation("com.mybatisflex.annotation.Column");
                        AptInfo aptInfo;
                        if (ObjectUtil.isNotNull(column)) {
                            PsiAnnotationMemberValue ignore = column.findAttributeValue("ignore");
                            if (ignore.textMatches("true")) {
                                continue;
                            }
                            PsiAnnotationMemberValue value = column.findAttributeValue("value");
                            PsiAnnotationMemberValue isLarge = column.findAttributeValue("isLarge");
                            String fieldName = value.getText().replace("\"", "");
                            aptInfo = new AptInfo(fieldName, getDefInstanceName(config, field.getName(), false), isLarge.getText().contains("true"));
                        } else {
                            aptInfo = new AptInfo(field.getName(), getDefInstanceName(config, field.getName(), false), false);
                        }
                        if (fieldMap.containsKey(aptInfo.getColumnName())) {
                            continue;
                        }
                        fieldMap.computeIfAbsent(aptInfo.getName(), k -> aptInfo);
                    }

                    PsiAnnotation table = psiClass.getAnnotation("com.mybatisflex.annotation.Table");
                    VelocityContext context = new VelocityContext();
                    String className = getClassName(config, psiClass.getName()) + ObjectUtil.defaultIfEmpty(config.getTableDefClassSuffix(), "TableDef");
                    context.put("className", className);
                    context.put("allColumns", getDefInstanceName(config, "allColumns", false));
                    context.put("defaultColumns", getDefInstanceName(config, "defaultColumns", false));
                    context.put("packageName", psiJavaFile.getPackageName() + "." + ObjectUtil.defaultIfEmpty(config.getAllInTablesPackage(), "table"));
                    context.put("list", fieldMap.values());
                    context.put("instance", getDefInstanceName(config, psiClass.getName(), true));
                    context.put("talbeName", table.findAttributeValue("value").getText().replace("\"", ""));
                    String suffix = Modules.getProjectTypeSuffix(moduleForFile);
                    String fileName = className + suffix;
                    PsiFile psiFile = VelocityUtils.render(context, Template.getTemplateContent("AptTemplate" + suffix), fileName);
                    psiDirectoryMap.computeIfAbsent(psiDirectory, k -> new ArrayList<>()).add(psiFile);
                });
            }
            // 等待索引准备好
            DumbService.getInstance(project).runWhenSmart(() -> {
                // 执行需要索引的操作
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    for (Map.Entry<PsiDirectory, List<PsiFile>> entry : psiDirectoryMap.entrySet()) {
                        PsiDirectory psiDirectory = entry.getKey();
                        List<PsiFile> psiFiles = entry.getValue();
                        for (PsiFile tmpFile : psiFiles) {
                            PsiFile file = psiDirectory.findFile(tmpFile.getName());
                            if (ObjectUtil.isNotNull(file)) {
                                file.getViewProvider().getDocument().setText(tmpFile.getText());
                            } else {
                                psiDirectory.add(tmpFile);
                            }
                        }
                    }
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDefInstanceName(CustomConfig config, String className, boolean clasVar) {
        String type = ObjectUtil.defaultIfNull(config.getTableDefPropertiesNameStyle(), "upperCase");
        String instanceSuffix = "";
        if (clasVar) {
            instanceSuffix = ObjectUtil.defaultIfNull(config.getTableDefInstanceSuffix(), "");
        }
        className = getClassName(config, className);
        String instace = toUnderlineCase(className);
        switch (type) {
            case "upperCase":
                instace = instace.toUpperCase();
                break;
            case "lowerCase":
                instace = instace.toLowerCase();
                break;
            case "upperCamelCase":
                instace = StrUtil.upperFirst(StrUtil.toCamelCase(instace));
                break;
            case "lowerCamelCase":
                instace = StrUtil.lowerFirst(StrUtil.toCamelCase(instace));
                break;
            default:
                instace = instace.toUpperCase();
                break;
        }
        return instace + instanceSuffix;
    }

    public static String toUnderlineCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @NotNull
    private static String getClassName(CustomConfig config, String className) {
        String ignoreEntitySuffixes = config.getTableDefIgnoreEntitySuffixes();
        String[] suffixes = ObjectUtil.defaultIfEmpty(ignoreEntitySuffixes, "").split(",");
        String target = Arrays.stream(suffixes).filter(s -> className.endsWith(s.trim())).findFirst().orElse("").trim();
        return className.replace(target, "");
    }


    public MybatisFlexDocumentChangeHandler() {
        super();

        try {
            NotificationUtils.start();
            // // 所有的文档监听
            // EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this, this);
            // Document document;
            //
            // for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            //     ProjectUtils.setCurrentProject(editor.getProject());
            //     document = editor.getDocument();
            //
            //     editor.addEditorMouseListener(new EditorMouseListener() {
            //         @Override
            //         public void mouseExited(@NotNull EditorMouseEvent event) {
            //             createAptFile(Arrays.asList(VirtualFileUtils.getVirtualFile(editor.getDocument())));
            //         }
            //     });
            // }
            Project project = ProjectUtils.getCurrentProject();
            if (ObjectUtil.isNull(project)) {
                return;
            }
            FileEditorManager.getInstance(project).addFileEditorManagerListener(this);
            if(PsiJavaFileUtil.isFlexProject()){
                new Thread(() -> {
                    scheduler.scheduleAtFixedRate(() -> {
                        try {
                            DumbService.getInstance(project).runWhenSmart(() -> {
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    PsiJavaFileUtil.createAptFile();
                                });
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, 10, 1, TimeUnit.MINUTES);
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {

        Editor editor = event.getEditor();
        Document document = editor.getDocument();
        VirtualFile currentFile = VirtualFileUtils.getVirtualFile(document);
        ProjectUtils.setCurrentProject(editor.getProject());
        if (!checkFile(currentFile)) {
            return;
        }
        document.removeDocumentListener(this);
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        try {
            EditorFactoryListener.super.editorCreated(event);
            Editor editor = event.getEditor();
            Document document = editor.getDocument();
            VirtualFile currentFile = VirtualFileUtils.getVirtualFile(document);
            ProjectUtils.setCurrentProject(editor.getProject());
            if (!checkFile(currentFile)) {
                return;
            }
            editor.addEditorMouseListener(new EditorMouseListener() {
                @Override
                public void mouseExited(@NotNull EditorMouseEvent event) {
                    createAptFile(Arrays.asList(VirtualFileUtils.getVirtualFile(editor.getDocument())));
                }
            });
            document.addDocumentListener(this);
        } catch (Exception e) {

        }
    }


    private static boolean checkFile(VirtualFile currentFile) {
        if (ObjectUtil.isNull(currentFile) || currentFile instanceof LightVirtualFile) {
            return false;
        }
        Project project = ProjectUtils.getCurrentProject();
        if (project.isDisposed()) {
            return false;
        }
        PsiManager psiManager = PsiManager.getInstance(project);
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
        return importSet.contains("com.mybatisflex.annotation.Table");
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        // if (MybatisFlexUtil.isFlexProject()) {
        //     return;
        // }
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


    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
