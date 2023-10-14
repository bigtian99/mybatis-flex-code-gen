package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.*;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class SqlToCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField variable;
    private JTextArea sql;
    private static final BasicFormatter FORMATTER = new BasicFormatter();
    private AnActionEvent event;

    private static Map<String, Consumer<StringBuilder>> METHOD_MAP = new HashMap<>();
    private static Map<String, Function<StringBuilder, String>> OPERATION_MAP = new HashMap<>();

    static {
        METHOD_MAP.put("and", SqlToCodeDialog::and);
        METHOD_MAP.put("or", SqlToCodeDialog::or);
        OPERATION_MAP.put("=", SqlToCodeDialog::eq);


    }

    public SqlToCodeDialog(AnActionEvent event) {
        this.event = event;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("SQL转 flex 代码（Beta）");
        setSize(500, 500);
        DialogUtil.centerShow(this);
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

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        variable.setText(MybatisFlexUtil.getSelectedText(event));
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(250);
                sql.requestFocus();
            } catch (InterruptedException e) {

            }
        }).start();
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // 尝试从剪贴板获取文本内容
        try {
            String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (StrUtil.isNotEmpty(clipboardContent)) {
                sql.setText(FORMATTER.format(clipboardContent));
            }
            // 在这里可以对剪贴板内容进行进一步处理
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
            // 处理异常情况
        }
        javax.swing.text.Document document = sql.getDocument();
        AtomicBoolean flag = new AtomicBoolean(false);
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                Boolean formated = ObjectUtil.defaultIfNull((Boolean) document.getProperty("formated"), false);
                if (!formated) {
                    SwingUtilities.invokeLater(() -> {
                        flag.set(true);
                        sql.setText(FORMATTER.format(sql.getText()));
                        flag.set(false);
                    });
                    document.putProperty("formated", true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!flag.get()) {
                    document.putProperty("formated", false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

        });
    }

    private void onOK() {
        createCode();
        dispose();
    }

    private void createCode() {
        String text = sql.getText().replace("=", " = ") + " ";
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1);
        }
        String tableName = StrUtil.subBetween(text, "from\n", "\n").trim();

        int line = MybatisFlexUtil.getLine(event);
        Editor editor = MybatisFlexUtil.getEditor(event);
        Document document = editor.getDocument();
        PsiJavaFile psiClassOwner = (PsiJavaFile) VirtualFileUtils.getPsiFile(document);
        Map<String, Map<String, String>> tableDefKeyTable = TableDefUtils.getDependenciesTableDefKeyTable(psiClassOwner.getVirtualFile());
        Map<String, String> tableDefMappingMap = TableDefUtils.getTableDefMappingMap();
        Map<String, String> tableClounmMap = tableDefKeyTable.get(tableName);

        String tableDefImport = tableDefMappingMap.get(tableClounmMap.get(null));
        String finalText = text;
        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
            document.insertString(line, transfrom(finalText, tableClounmMap));
            // 保存文档
            PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
            checkHasImport(psiClassOwner, "com.mybatisflex.core.query.QueryWrapper");
            checkHasStaticImport(psiClassOwner, tableDefImport, tableClounmMap.get(null));

            PsiFile psiFile = VirtualFileUtils.getPsiFile(document);
            CodeReformat.reformat(psiFile);

        });
    }


    private String transfrom(String sql, Map<String, String> tableClounmMap) {
        StringBuilder builder = new StringBuilder("QueryWrapper wrapper=QueryWrapper.create()\n");

        Assert.isTrue(sql.startsWith("select"), "sql must start with select");
        // 需要查询的列
        String tableDef = tableClounmMap.get(null);
        String columns = StrUtil.subBetween(sql, "select", "from").replace(" ", "");
        StringJoiner joiner = new StringJoiner(",");
        for (String s : columns.split(",")) {
            joiner.add(StrUtil.format("{}", tableDef + "." + tableClounmMap.get(s.trim())));
        }
        builder.append(StrUtil.format(".select({})\n", joiner));
        builder.append(StrUtil.format(".from({})\n", tableDef));
        String where = StrUtil.subAfter(sql, "where", false);
        AtomicReference<StringBuilder> temp = new AtomicReference<>(new StringBuilder(""));
        AtomicBoolean flag = new AtomicBoolean(false);
        Arrays.stream(where.split(" "))
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .forEach(el -> {
                    Consumer<StringBuilder> consumer = METHOD_MAP.get(el);
                    StringBuilder stringBuilder = temp.get();
                    if (ObjectUtil.isNotEmpty(consumer)) {
                        consumer.accept(builder);
                        flag.set(true);
                        return;
                    }
                    Function<StringBuilder, String> operation = OPERATION_MAP.get(el);
                    if (ObjectUtil.isNotEmpty(operation)) {
                        String tmpStr = tableDef + operation.apply(stringBuilder);
                        if (flag.get()) {
                            builder.append(tmpStr);
                            builder.append(")\n");
                            flag.set(false);
                        } else {
                            builder.append(StrUtil.format(".where({})\n", tmpStr));
                        }
                        temp.set(new StringBuilder());
                        return;
                    }
                    String s = tableClounmMap.get(el);
                    if (StrUtil.isEmpty(s)) {
                        return;
                    }
                    stringBuilder.append(s);
                });
        builder.append(";");
        return builder.toString();
    }


    private static void checkHasImport(PsiJavaFile psiClassOwner, String importText) {
        Set<String> importSet = PsiJavaFileUtil.getQualifiedNameImportSet(psiClassOwner);
        // 如果没有导入，则导入
        if (!importSet.contains(importText)) {
            psiClassOwner.getImportList().add(PsiJavaFileUtil.createImportStatement(PsiJavaFileUtil.getPsiClass(importText)));
        }
    }

    private static void checkHasStaticImport(PsiJavaFile psiClassOwner, String importText, String field) {
        Set<String> importSet = PsiJavaFileUtil.getQualifiedNameImportSet(psiClassOwner);
        // 如果没有导入，则导入
        if (!importSet.contains(importText)) {
            psiClassOwner.getImportList().add(PsiJavaFileUtil.createImportStaticStatement(PsiJavaFileUtil.getPsiClass(importText), field));
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void and(StringBuilder builder) {
        builder.append(".and(");
    }

    public static void or(StringBuilder builder) {
        builder.append(".or(");
    }

    public static String eq(StringBuilder column) {
        return StrUtil.format(".{}.eq()", column.toString());
    }
}
