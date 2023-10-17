package club.bigtian.mf.plugin.core.visitor;

import cn.hutool.core.util.StrUtil;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.Map;

public class JoinConditionVisitor extends ExpressionDeParser {
    StringBuilder builder;
    private String tableDef;

    private Map<String, String> flexCloumMap;
    private String variable;
    Map<String, String> tableClounmMap;

    Map<String, String> aliasMap;

    public JoinConditionVisitor(String tableDef, StringBuilder builder, Map<String, String> flexCloumMap, String variable, Map<String, String> tableClounmMap, Map<String, String> aliasMap) {
        this.tableDef = tableDef;
        this.builder = builder;
        this.flexCloumMap = flexCloumMap;
        this.variable = variable;
        this.tableClounmMap = tableClounmMap;
        this.aliasMap = aliasMap;
    }

    @Override
    public void visit(AndExpression andExpression) {

        andExpression.getLeftExpression().accept(this);

        builder.append(StrUtil.format("\n.and("));
        andExpression.getRightExpression().accept(this);
        builder.append(")");
    }


    @Override
    public void visit(EqualsTo equalsTo) {
        Column leftExpression = (Column) equalsTo.getLeftExpression();
        String leftColumnName = tableClounmMap.get(leftExpression.getColumnName());
        Column rightExpression = (Column) equalsTo.getRightExpression();
        String rightColumnName = tableClounmMap.get(rightExpression.getColumnName());
        String leftAlias = aliasMap.get(leftExpression.getTable().getName());
        String rigthAlias = aliasMap.get(rightExpression.getTable().getName());
        String text = StrUtil.format("{}.{}.eq({}.{})", leftAlias, leftColumnName, rigthAlias,rightColumnName );
        builder.append(text);
    }


}
