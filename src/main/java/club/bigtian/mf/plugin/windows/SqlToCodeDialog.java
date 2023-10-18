package club.bigtian.mf.plugin.windows;

import club.bigtian.mf.plugin.core.util.*;
import club.bigtian.mf.plugin.core.visitor.JoinConditionVisitor;
import club.bigtian.mf.plugin.core.visitor.WhereConditionVisitor;
import cn.hutool.core.collection.CollUtil;
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
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.jetbrains.annotations.Nullable;

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
import java.util.stream.Collectors;

public class SqlToCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField variable;
    private JTextArea sql;
    private static final BasicFormatter FORMATTER = new BasicFormatter();
    private AnActionEvent event;
    static Map<String, String> flexMethodMappingMap = new HashMap<>();


    static {
        flexMethodMappingMap.put("substr", "substring");
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
        String tableName = "";
        StringBuilder builder;
        if (parse instanceof Update) {
            Update update = (Update) parse;
            tableName = update.getTable().getName();
            builder = new StringBuilder("UpdateChain.of(");
            builder.append(variable.getText());
            builder.append(")");
        } else if (parse instanceof PlainSelect) {
            PlainSelect select = (PlainSelect) parse;
            Table fromItem = (Table) select.getFromItem();
            tableName = fromItem.getNameParts().get(0);
            builder = new StringBuilder("QueryWrapper wrapper=QueryWrapper.create()");

        } else if (parse instanceof Delete) {
            Delete delete = (Delete) parse;
            tableName = delete.getTable().getName();
            builder = new StringBuilder("QueryWrapper wrapper=QueryWrapper.create()");
        } else {
            builder = new StringBuilder();
        }


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
        if (ObjectUtil.isNull(tableClounmMap)) {
            Messages.showWarningDialog("未找到对应表对应的APT文件，请检查", "提示");
            return;
        }
        String tableDefImport = tableDefMappingMap.get(tableClounmMap.get(null));
        String finalText = text;
        HashMap<String, String> tableDefMap = new HashMap<>();
        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            PsiDocumentManager.getInstance(event.getProject()).commitDocument(document);
            document.insertString(line, transfrom(finalText, tableClounmMap, tableDefKeyTable, tableDefMappingMap, tableDefMap, builder));
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


    private String transfrom(String sql, Map<String, String> tableColunmMap, Map<String, Map<String, String>> tableDefKeyTable, Map<String, String> tableDefMappingMap, HashMap<String, String> tableDefMap, StringBuilder builder) {

        // 需要查询的列
        String tableDef = tableColunmMap.get(null);
        Map<String, String> flexCloumMap = tableColunmMap.entrySet().stream()
                .filter(el -> StrUtil.isNotEmpty(el.getKey()))
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        try {
            Statement parse = CCJSqlParserUtil.parse(sql);

            Map<String, String> aliasMap = new HashMap<>();
            StringJoiner joiner = new StringJoiner(",");
            Expression where;
            WhereConditionVisitor expressionVisitor;
            tableDefMap.put(tableDef, tableDefMappingMap.get(tableDef));
            if (parse instanceof PlainSelect) {
                PlainSelect select = (PlainSelect) parse;
                select(tableColunmMap, select, tableDef, aliasMap, joiner, tableDefMap);
                builder.append(StrUtil.format("\n.select({})\n", joiner));
                builder.append(StrUtil.format(".from({})", tableDef));
                List<Join> joins = getJoins(tableDefKeyTable, select, aliasMap);
                joins(tableColunmMap, tableDefKeyTable, tableDefMappingMap, tableDefMap, joins, builder, tableDef, flexCloumMap, aliasMap);
                where = select.getWhere();
                expressionVisitor = new WhereConditionVisitor(tableDef, builder, flexCloumMap, variable.getText(), tableColunmMap, ObjectUtil.isNotNull(where), aliasMap, tableDefMap);
                groupBy(select, expressionVisitor);
                having(select, builder, expressionVisitor);
                orderBy(tableColunmMap, tableDefKeyTable, select, builder, tableDef, aliasMap);
            } else if (parse instanceof Delete) {
                Delete delete = (Delete) parse;
                where = delete.getWhere();
                expressionVisitor = new WhereConditionVisitor(tableDef, builder, flexCloumMap, variable.getText(), tableColunmMap, ObjectUtil.isNotNull(where), aliasMap, tableDefMap);
            } else if (parse instanceof Update) {
                Update update = (Update) parse;
                where = update.getWhere();
                for (UpdateSet updateSet : update.getUpdateSets()) {
                    for (Column column : updateSet.getColumns()) {
                        String defColumn = tableColunmMap.get(column.getColumnName());
                        String method = getMethod(defColumn, flexCloumMap);
                        String text = StrUtil.format("\n.set({}.{},{})", tableDef, defColumn, method);
                        builder.append(text);
                    }
                }
                expressionVisitor = new WhereConditionVisitor(tableDef, builder, flexCloumMap, variable.getText(), tableColunmMap, ObjectUtil.isNotNull(where), aliasMap, tableDefMap);
            } else {
                Messages.showWarningDialog("不支持的语法", "提示");
                return "";
            }
            if (ObjectUtil.isNotNull(where)) {
                where.accept(expressionVisitor);
            }
            if (parse instanceof Update) {
                builder.append("\n.update()");
            }
            builder.append(";");

        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }

        return builder.toString();
    }

    private String getMethod(String column, Map<String, String> flexCloumMap) {
        String camelCase = StrUtil.toCamelCase(flexCloumMap.get(column));
        String getMethod = StrUtil.format("{}.get{}()", variable.getText(), StrUtil.upperFirst(camelCase));
        return getMethod;
    }

    @Nullable
    private static List<Join> getJoins(Map<String, Map<String, String>> tableDefKeyTable, PlainSelect select, Map<String, String> aliasMap) {
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
        return joins;
    }

    private static void select(Map<String, String> tableClounmMap, PlainSelect select, String tableDef, Map<String, String> aliasMap, StringJoiner joiner, HashMap<String, String> tableDefMap) {
        Alias alias = select.getFromItem().getAlias();
        String aliasTableDef;
        if (ObjectUtil.isNotNull(alias)) {
            aliasTableDef = alias.getName();
            aliasMap.put(aliasTableDef, tableDef);
        }
        List<SelectItem<?>> selectItems = select.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            Expression expression = selectItem.getExpression();
            String aliasName = tableDef;
            String columnName;
            if (expression instanceof AllTableColumns) {
                AllTableColumns allColumns = (AllTableColumns) expression;
                Table table = allColumns.getTable();
                aliasName = aliasMap.get(table.getName());
                columnName = tableClounmMap.get("*");
            } else if (expression instanceof Function) {
                Function function = (Function) expression;
                ExpressionList parameters = function.getParameters();
                if (CollUtil.isNotEmpty(parameters)) {
                    StringJoiner methodJoin = new StringJoiner(",");
                    for (Object parameter : parameters) {
                        if (parameter instanceof Column) {
                            Column column = (Column) parameters.get(0);
                            Table table = column.getTable();
                            String leftAlias = tableDef;
                            if (ObjectUtil.isNotNull(table)) {
                                leftAlias = aliasMap.get(table.getName());
                            }
                            String columnsName = column.getColumnName();
                            String leftColumnName = tableClounmMap.get(columnsName);
                            String name = function.getName();
                            name = flexMethodMappingMap.getOrDefault(name, name);
                            methodJoin.add(StrUtil.format("{}({}.{}", name, leftAlias, leftColumnName));
                            tableDefMap.put(name, "com.mybatisflex.core.query.QueryMethods");
                        } else {
                            methodJoin.add(parameter.toString());
                        }
                    }
                    joiner.add(methodJoin.toString() + ")");
                    continue;
                } else {
                    Column column = (Column) expression;
                    if (ObjectUtil.isNotNull(column.getTable())) {
                        aliasName = aliasMap.get(column.getTable().getName());
                    }
                    columnName = tableClounmMap.get(column.getColumnName());
                }
                joiner.add(StrUtil.format("{}", aliasName + "." + columnName));
            }
        }
    }

    private void joins(Map<String, String> tableClounmMap, Map<String, Map<String, String>> tableDefKeyTable, Map<String, String> tableDefMappingMap, HashMap<String, String> tableDefMap, List<Join> joins, StringBuilder builder, String tableDef, Map<String, String> flexCloumMap, Map<String, String> aliasMap) {
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
    }

    private static void groupBy(PlainSelect select, WhereConditionVisitor expressionVisitor) {
        GroupByElement groupBy = select.getGroupBy();

        if (ObjectUtil.isNotNull(groupBy)) {
            groupBy.accept(expressionVisitor);
        }
    }

    private static void having(PlainSelect select, StringBuilder builder, WhereConditionVisitor expressionVisitor) {
        Expression having = select.getHaving();
        if (ObjectUtil.isNotNull(having)) {
            builder.append("\n.having(");
            having.accept(expressionVisitor);
            builder.append(")");
        }
    }

    private static void orderBy(Map<String, String> tableClounmMap, Map<String, Map<String, String>> tableDefKeyTable, PlainSelect select, StringBuilder builder, String tableDef, Map<String, String> aliasMap) {
        List<OrderByElement> orderByElements = select.getOrderByElements();
        Map<String, Map<String, String>> tableDefColumMap = tableDefKeyTable.entrySet().stream()
                .collect(Collectors.toMap(el -> el.getValue().get(null), Map.Entry::getValue));

        if (CollUtil.isNotEmpty(orderByElements)) {
            builder.append("\n.orderBy(");
            StringJoiner orderJoiner = new StringJoiner(",");
            orderByElements.forEach(el -> {
                Column column = (Column) el.getExpression();
                Table table = column.getTable();
                String aliasOorderName = tableDef;
                String columnName = tableClounmMap.get(column.getColumnName());
                if (ObjectUtil.isNotNull(table)) {
                    aliasOorderName = aliasMap.get(table.getName());
                    columnName = tableDefColumMap.get(aliasOorderName).get(column.getColumnName());
                }
                orderJoiner.add(StrUtil.format("{}.{}.{}()", aliasOorderName, columnName, el.isAsc() ? "asc" : "desc"));
            });
            builder.append(orderJoiner);
            builder.append(")");

        }
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
        if (!importSet.contains(importText + "." + field)) {
            psiClassOwner.getImportList().add(PsiJavaFileUtil.createImportStaticStatement(PsiJavaFileUtil.getPsiClass(importText), field));
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
