package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.DialogUtil;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

import javax.swing.*;
import java.awt.*;

public class EditDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel editorPanel;
    private Editor templateEditor;

    public EditDialog(String template) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("自定义模板变量");
        setSize(1000, 600);
        DialogUtil.centerShow(this);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        editorPanel.setLayout(new GridLayout(1, 1));
        editorPanel.setPreferredSize(new Dimension(800, 600));
        templateEditor = createEditorWithText(template, ".groovy");
        editorPanel.add(templateEditor.getComponent());
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onOK() {
        // add your code here if necessary
        dispose();
    }

    String getScript() {
        return templateEditor.getDocument().getText();
    }

    public Editor createEditorWithText(String text, String fileSuffix) {
        Project project = ProjectUtils.getCurrentProject();
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile psiFile = psiFileFactory.createFileFromText(PlainTextLanguage.INSTANCE, text);
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        // 获取EditorFactory实例
        EditorFactory editorFactory = EditorFactory.getInstance();
        // // 创建一个Document实例
        // 创建一个Editor实例
        Editor editor = editorFactory.createEditor(document, project);
        // 设置Editor的一些属性
        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setVirtualSpace(false);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setLineNumbersShown(true);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setGutterIconsShown(true);
// 设置 Groovy 文件的高亮器和提示
        ((EditorEx) editor).setHighlighter(
                EditorHighlighterFactory.getInstance().createEditorHighlighter(project, ".groovy")
        );

// 启用自动补全提示（代码补全）功能
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, CompletionType.BASIC, el -> {
            // 只在 Groovy 文件中触发基本补全
            return el.getFileType().getName().equals("Groovy");
        });

        return editor;
    }

}
