package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.core.visitor.JoinConditionVisitor;
import club.bigtian.mf.plugin.core.visitor.WhereConditionVisitor;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Statement parse = null;
        try {
            parse = CCJSqlParserUtil.parse(text);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }

        PlainSelect select = (PlainSelect) parse;

        Table fromItem = (Table) select.getFromItem();
        String tableName = fromItem.getNameParts().get(0);

        int line = MybatisFlexUtil.getLine(event);
        Editor editor = MybatisFlexUtil.getEditor(event);
        Document document = editor.getDocument();
        PsiJavaFile psiClassOwner = (PsiJavaFile) VirtualFileUtils.getPsiFile(document);
        Map<String, Map<String, String>> tableDefKeyTable = TableDefUtils.getDependenciesTableDefKeyTable(psiClassOwner.getVirtualFile());
        if (CollUtil.isEmpty(tableDefKeyTable)) {
            Messages.showWarningDialog("请先生成TableDef文件，再使用该功能", "提示");
            return;
        }
        Map<String, String> tableDefMappingMap = TableDefUtils.getTableDefMappingMap();
        Map<String, String> tableClounmMap = tableDefKeyTable.get(tableName);

        String tableDefImport = tableDefMappingMap.get(tableClounmMap.get(null));
        String finalText = text;
        HashMap<String, String> tableDefMap = new HashMap<>();
        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
            document.insertString(line, transfrom(finalText, tableClounmMap, tableDefKeyTable, tableDefMappingMap, tableDefMap));
            // 保存文档
            PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
            checkHasImport(psiClassOwner, "com.mybatisflex.core.query.QueryWrapper");
            checkHasStaticImport(psiClassOwner, tableDefImport, tableClounmMap.get(null));
            for (Map.Entry<String, String> entry : tableDefMap.entrySet()) {
                checkHasStaticImport(psiClassOwner, entry.getValue(), entry.getKey());
            }
            PsiFile psiFile = VirtualFileUtils.getPsiFile(document);
            CodeReformat.reformat(psiFile);

        });
    }


    private String transfrom(String sql, Map<String, String> tableClounmMap, Map<String, Map<String, String>> tableDefKeyTable, Map<String, String> tableDefMappingMap, HashMap<String, String> tableDefMap) {
        StringBuilder builder = new StringBuilder("QueryWrapper wrapper=QueryWrapper.create()\n");

        Assert.isTrue(sql.startsWith("select"), "sql must start with select");
        // 需要查询的列
        String tableDef = tableClounmMap.get(null);
        Map<String, String> flexCloumMap = tableClounmMap.entrySet().stream()
                .filter(el -> StrUtil.isNotEmpty(el.getKey()))
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);

            PlainSelect select = (PlainSelect) parse;

            List<SelectItem<?>> selectItems = select.getSelectItems();
            StringJoiner joiner = new StringJoiner(",");
            Map<String, String> aliasMap = new HashMap<>();
            Alias alias = select.getFromItem().getAlias();
            if (ObjectUtil.isNotNull(alias)) {
                String name = alias.getName();
                aliasMap.put(name, tableDef);
            }
            List<Join> joins = select.getJoins();
            if (CollUtil.isNotEmpty(joins)) {
                joins.forEach(el -> {
                    Table fromItem = (Table) el.getRightItem();
                    String joinAlias = fromItem.getAlias().getName();
                    String joinName = fromItem.getNameParts().get(0);
                    String joinTableDef = tableDefKeyTable.get(joinName).get(null);
                    aliasMap.put(joinAlias, joinTableDef);
                });
            }

            for (SelectItem<?> selectItem : selectItems) {
                Expression expression = selectItem.getExpression();
                String aliasName = tableDef;
                String columnName;
                if (expression instanceof AllTableColumns) {
                    AllTableColumns allColumns = (AllTableColumns) expression;
                    Table table = allColumns.getTable();
                    aliasName = aliasMap.get(table.getName());
                    columnName = tableClounmMap.get("*");
                } else {
                    Column column = (Column) expression;
                    if (ObjectUtil.isNotNull(column.getTable())) {
                        aliasName = aliasMap.get(column.getTable().getName());
                    }
                    columnName = tableClounmMap.get(column.getColumnName());
                }
                joiner.add(StrUtil.format("{}", aliasName + "." + columnName));
            }
            builder.append(StrUtil.format(".select({})\n", joiner));
            builder.append(StrUtil.format(".from({})", tableDef));

            if (CollUtil.isNotEmpty(joins)) {

                joins.forEach(el -> {
                    Table fromItem = (Table) el.getRightItem();
                    String joinName = fromItem.getNameParts().get(0);
                    String joinTableDef = tableDefKeyTable.get(joinName).get(null);
                    tableDefMap.put(joinTableDef, tableDefMappingMap.get(joinTableDef));
                    if (el.isLeft()) {
                        builder.append(StrUtil.format("\n.leftJoin({})", joinTableDef));
                    } else if (el.isRight()) {
                        builder.append(StrUtil.format("\n.rightJoin({})", joinTableDef));
                    } else if (el.isInner()) {
                        builder.append(StrUtil.format("\n.innerJoin({})", joinTableDef));
                    } else if (el.isOuter()) {
                        builder.append(StrUtil.format("\n.outerJoin({})", joinTableDef));
                    } else if (el.isFull()) {
                        builder.append(StrUtil.format("\n.fullJoin({})", joinTableDef));
                    } else if (el.isCross()) {
                        builder.append(StrUtil.format("\n.crossJoin({})", joinTableDef));
                    } else {
                        builder.append(StrUtil.format("\n.join({})", joinTableDef));
                    }
                    Collection<Expression> onList = el.getOnExpressions();
                    if (CollUtil.isNotEmpty(onList)) {
                        onList.forEach(exp -> {
                            builder.append("\n.on(");
                            exp.accept(new JoinConditionVisitor(tableDef, builder, flexCloumMap, variable.getText(), tableClounmMap, aliasMap));
                            builder.append(")");
                        });
                    }
                });
            }
            Expression where = select.getWhere();
            WhereConditionVisitor expressionVisitor = new WhereConditionVisitor(tableDef, builder, flexCloumMap, variable.getText(), tableClounmMap, ObjectUtil.isNotNull(where), aliasMap);
            if (ObjectUtil.isNotNull(where)) {
                where.accept(expressionVisitor);
            }

            GroupByElement groupBy = select.getGroupBy();

            if (ObjectUtil.isNotNull(groupBy)) {
                groupBy.accept(expressionVisitor);
            }
            Expression having = select.getHaving();
            if (ObjectUtil.isNotNull(having)) {
                builder.append("\n.having(");
                having.accept(expressionVisitor);
                builder.append(")");
            }
            List<OrderByElement> orderByElements = select.getOrderByElements();
            if (CollUtil.isNotEmpty(orderByElements)) {
                orderByElements.forEach(el -> {
                    builder.append("\n.orderBy(");
                    builder.append(StrUtil.format("{}.{}.{}()", tableDef, tableClounmMap.get(el.getExpression().toString()), el.isAsc() ? "asc" : "desc"));
                    builder.append(")");
                });
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
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
