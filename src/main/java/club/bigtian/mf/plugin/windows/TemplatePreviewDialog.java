package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.ProjectUtils;
import com.intellij.ide.fileTemplates.impl.FileTemplateHighlighter;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.ui.ComboBoxCompositeEditor;
import com.intellij.ui.LanguageTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TemplatePreviewDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList list1;
    private EditorComponentImpl textField1;
    private EditorComponentImpl textField2;

    public TemplatePreviewDialog() {
        setContentPane(contentPane);
        setTitle("Mybatis Flex Code Generate");
        setSize(1050, 460);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here
        dispose();
    }

    private void createUIComponents() {
        // Create a document for the editor
        Document document = EditorFactory.getInstance().createDocument("package ${packageName};\n" +
                "\n" +
                "import com.mybatisflex.core.query.QueryColumn;\n" +
                "import com.mybatisflex.core.table.TableDef;\n" +
                "\n" +
                "class ${className} : TableDef(\"\", \"${talbeName}\") {\n" +
                "\n" +
                "#foreach($column in $list)\n" +
                "    val $column.name = QueryColumn(this, \"${column.columnName}\")\n" +
                "#end\n" +
                "\n" +
                "    /**\n" +
                "     * 所有字段。\n" +
                "     */\n" +
                "    val $allColumns = QueryColumn(this, \"*\")\n" +
                "\n" +
                "    /**\n" +
                "     * 默认字段，不包含逻辑删除或者 large 等字段。\n" +
                "     */\n" +
                "    val $defaultColumns = arrayOf(#foreach($column in $list) #if($column.large==false)$column.name #if($foreach.hasNext),#end#end#end)\n" +
                "\n" +
                "\n" +
                "    companion object {\n" +
                "        val  $instance = ${className}()\n" +
                "    }\n" +
                "}\n");

        // Create the editor using the document
        EditorImpl editor = (EditorImpl) EditorFactory.getInstance().createEditor(document, ProjectUtils.getCurrentProject());
        EditorSettings editorSettings = editor.getSettings();
        // 关闭虚拟空间
        editorSettings.setVirtualSpace(false);
        // 关闭标记位置（断点位置）
        editorSettings.setLineMarkerAreaShown(false);
        // 关闭缩减指南
        editorSettings.setIndentGuidesShown(false);
        // 显示行号
        editorSettings.setLineNumbersShown(true);
        // 支持代码折叠
        editorSettings.setFoldingOutlineShown(true);
        // 附加行，附加列（提高视野）
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        // 不显示换行符号
        editorSettings.setCaretRowShown(false);
        editor.setCaretEnabled(true);
        editor.setCaretVisible(true);
        textField1 = new EditorComponentImpl(editor);
        textField1.setEditable(true);
        textField1.setEnabled(true);
        textField2 = new EditorComponentImpl(editor);
        // TODO: place custom component creation code here

    }
}
