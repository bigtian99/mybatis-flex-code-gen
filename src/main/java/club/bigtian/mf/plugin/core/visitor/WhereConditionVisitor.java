package club.bigtian.mf.plugin.core.visitor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.ui.Messages;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.GroupByVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.Map;
import java.util.StringJoiner;

public class WhereConditionVisitor extends ExpressionDeParser implements GroupByVisitor {
    StringBuilder builder;
    private String tableDef;

    private Map<String, String> flexCloumMap;
    private String variable;
    Map<String, String> tableClounmMap;
    private boolean hasWhere;
    Map<String, String> aliasMap;

    public WhereConditionVisitor(String tableDef, StringBuilder builder, Map<String, String> flexCloumMap, String variable, Map<String, String> tableClounmMap, boolean hasWhere, Map<String, String> aliasMap) {
        this.tableDef = tableDef;
        this.builder = builder;

        this.flexCloumMap = flexCloumMap;
        this.variable = variable;
        this.tableClounmMap = tableClounmMap;
        this.hasWhere = hasWhere;
        this.aliasMap = aliasMap;
        if (hasWhere) {
            builder.append("\n.where(");
        }
    }

    @Override
    public void visit(AndExpression andExpression) {

        andExpression.getLeftExpression().accept(this);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
        builder.append(StrUtil.format("\n.and("));
        andExpression.getRightExpression().accept(this);
        builder.append(")");
    }

    @Override
    public void visit(OrExpression orExpression) {
        orExpression.getLeftExpression().accept(this);
        builder.append("\n.or(");

        orExpression.getRightExpression().accept(this);
        builder.append(")");
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression thanLeftExpression = equalsTo.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, "eq");
            return;
        }
        Column leftExpression = (Column) equalsTo.getLeftExpression();
        String leftColumnName = tableClounmMap.get(leftExpression.getColumnName());
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String camelCase = getMethod(leftColumnName.toUpperCase());
        String text = StrUtil.format("{}.{}.eq({})", leftAlias, leftColumnName, camelCase);
        builder.append(text);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }


    @Override
    public void visit(LikeExpression likeExpression) {
        String value = likeExpression.getRightExpression().toString();
        boolean start = value.startsWith("'%");
        boolean end = value.endsWith("%'");
        String content = "";
        if (start && end) {

        } else if (start) {
            content = "Left";
        } else if (end) {
            content = "Right";
        }
        Expression thanLeftExpression = likeExpression.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, likeExpression.isNot() ? "notLike" + content : "like" + content);
            return;
        }
        Column leftExpression = (Column) likeExpression.getLeftExpression();
        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);

        if (likeExpression.isNot()) {
            builder.append(StrUtil.format("{}.{}.notLike{}({})", leftAlias, tableClounmMap.get(columnName), content, method));
        } else {
            builder.append(StrUtil.format("{}.{}.like{}({})", leftAlias, tableClounmMap.get(columnName), content, method));
        }
        likeExpression.getLeftExpression().accept(this);
        likeExpression.getRightExpression().accept(this);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    private String getMethod(String column) {
        String camelCase = StrUtil.toCamelCase(flexCloumMap.get(column));
        String getMethod = StrUtil.format("{}.get{}()", variable, StrUtil.upperFirst(camelCase));
        return getMethod;
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        Expression thanLeftExpression = greaterThan.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, "gt");
            return;
        }
        Column leftExpression = (Column) thanLeftExpression;
        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);
        String text = StrUtil.format(" {}.{}.gt({})", leftAlias, tableClounmMap.get(columnName), method);
        builder.append(text);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    private void function(Function thanLeftExpression, String key, Object... args) {
        Function function = thanLeftExpression;
        builder.append(function.getName() + "(");

        ExpressionList parameters = function.getParameters();
        if (CollUtil.isNotEmpty(parameters)) {
            Column column = (Column) parameters.get(0);
            Table table = column.getTable();
            String leftAlias = tableDef;
            if (ObjectUtil.isNotNull(table)) {
                leftAlias = aliasMap.get(table.getName());
            }
            String columnName = column.getColumnName();
            String leftColumnName = tableClounmMap.get(columnName);
            builder.append(StrUtil.format("{}.{}", leftAlias, leftColumnName));
            builder.append(")");
            if(args.length==0){
                builder.append(StrUtil.format(".{}({})", key, getMethod(leftColumnName)));
            }else{
                builder.append(StrUtil.format(".{}()", key));

            }
        }

        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        Expression thanLeftExpression = greaterThanEquals.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, "ge");
            return;
        }
        Column leftExpression = (Column) greaterThanEquals.getLeftExpression();

        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);
        String text = StrUtil.format(" {}.{}.ge({})", leftAlias, tableClounmMap.get(columnName), method);
        builder.append(text);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        Expression thanLeftExpression = minorThan.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, "lt");
            return;
        }
        Column leftExpression = (Column) minorThan.getLeftExpression();
        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);
        String text = StrUtil.format(" {}.{}.lt({})", leftAlias, tableClounmMap.get(columnName), method);
        builder.append(text);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        Expression thanLeftExpression = minorThanEquals.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, "le");
            return;
        }
        Column leftExpression = (Column) minorThanEquals.getLeftExpression();
        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);
        String text = StrUtil.format(" {}.{}.le({})", leftAlias, tableClounmMap.get(columnName), method);
        builder.append(text);
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        Expression thanLeftExpression = isNullExpression.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, isNullExpression.isNot() ? "isNotNull" : "isNull","");
            return;
        }
        Column leftExpression = (Column) isNullExpression.getLeftExpression();
        String columnName = leftExpression.getColumnName();

        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        if (isNullExpression.isNot()) {
            builder.append(StrUtil.format("{}.{}.isNotNull()", leftAlias, tableClounmMap.get(columnName)));
        } else {
            builder.append(StrUtil.format("{}.{}.isNull()", leftAlias, tableClounmMap.get(columnName)));
        }
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }


    @Override
    public void visit(GroupByElement groupBy) {
        ExpressionList groupByExpressionList = groupBy.getGroupByExpressionList();
        if (CollUtil.isNotEmpty(groupByExpressionList)) {
            builder.append("\n.groupBy(");
            StringJoiner joiner = new StringJoiner(",");
            for (Object o : groupByExpressionList) {
                Column column = (Column) o;
                Table table = column.getTable();
                String leftAlias = tableDef;
                if (ObjectUtil.isNotNull(table)) {
                    leftAlias = aliasMap.get(table.getName());
                }
                joiner.add(StrUtil.format("{}.{}", leftAlias, tableClounmMap.get(column.getColumnName())));
            }
            builder.append(joiner.toString() + ")");
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        Messages.showWarningDialog("暂不支持 EXISTS 关键字(flex不支持)", "提示");
        throw new RuntimeException("暂不支持 EXISTS 关键字(flex不支持)");
    }

    @Override
    public void visit(InExpression inExpression) {
        Expression thanLeftExpression = inExpression.getLeftExpression();
        if (thanLeftExpression instanceof Function) {
            function((Function) thanLeftExpression, inExpression.isNot() ? "notIn" : "in");
            return;
        }
        inExpression.getLeftExpression().accept(this);
        Column leftExpression = (Column) inExpression.getLeftExpression();
        String columnName = leftExpression.getColumnName();

        String leftColumnName = tableClounmMap.get(columnName);
        Table table = leftExpression.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String method = getMethod(leftColumnName);

        if (inExpression.isNot()) {
            builder.append(StrUtil.format("{}.{}.notIn({})", leftAlias, tableClounmMap.get(columnName), method));
        } else {
            builder.append(StrUtil.format("{}.{}.in({})", leftAlias, tableClounmMap.get(columnName), method));
        }
        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
        inExpression.getRightExpression().accept(this);
    }
    @Override
    public void visit(Between between) {
        Column column = (Column) between.getLeftExpression();
        String columnName = column.getColumnName();

        Table table = column.getTable();
        String leftAlias = tableDef;
        if (ObjectUtil.isNotNull(table)) {
            leftAlias = aliasMap.get(table.getName());
        }
        String startVal = between.getBetweenExpressionStart().toString();
        String endVal = between.getBetweenExpressionEnd().toString();
        if (between.isNot()) {
            builder.append(StrUtil.format("{}.{}.notBetween({},{})", leftAlias, tableClounmMap.get(columnName), startVal, endVal));
        } else {
            builder.append(StrUtil.format("{}.{}.between({},{})", leftAlias, tableClounmMap.get(columnName), startVal, endVal));
        }

        if (hasWhere) {
            hasWhere = false;
            builder.append(")");
        }
    }

}
