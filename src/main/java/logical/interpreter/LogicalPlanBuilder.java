package logical.interpreter;

import logical.operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.JoinExpressionVisitor;

import java.util.Map;

/**
 * Handler class to parse SQL, construct query plan and handle initialization
 * Created by Yufu Mo
 */
public class LogicalPlanBuilder {
    /**
    * Construct a left deep join query plan
    *
    *           distinct
    *              |
    *             sort
    *              |
    *             join
    *           /      \
    *         join    scan
    *        /    \
    *   select   select
    *      |       |
    *    scan     scan
    *
    * @param plainSelect
    * @return
    */
    public static Operator constructLogicalPlanTree(PlainSelect plainSelect) {
        int tableCount;
        Operator opLeft;
        if (plainSelect.getJoins() == null) {
            tableCount = 1;
        } else {
            tableCount = 1 + plainSelect.getJoins().size();
        }

        opLeft = new ScanOperator(plainSelect, 0);
        if (hasRelatedExpression(opLeft.getSchema(), plainSelect)) {
            opLeft = new SelectOperator(opLeft, plainSelect);
        }

        for (int i = 1; i < tableCount; ++i) {
            Operator opRight = new ScanOperator(plainSelect, i);
            if (hasRelatedExpression(opRight.getSchema(), plainSelect)) {
                opRight = new SelectOperator(opRight, plainSelect);
            }
            opLeft = new JoinOperator(opLeft, opRight, plainSelect);
        }
        if (plainSelect.getSelectItems() != null
                && plainSelect.getSelectItems().size() > 0
                && plainSelect.getSelectItems().get(0).toString() != "*")
            opLeft = new ProjectOperator(opLeft, plainSelect);
        if (plainSelect.getDistinct() != null) {
            opLeft = new SortOperator(opLeft, plainSelect);
            opLeft = new DuplicateEliminationOperator(opLeft);
        } else {
            if (plainSelect.getOrderByElements() != null)
                opLeft = new SortOperator(opLeft, plainSelect);
        }
        return opLeft;
    }

    private static boolean hasRelatedExpression(Map<String, Integer> schemaMap, PlainSelect plainSelect) {
        Expression originExpression = plainSelect.getWhere();
        if (originExpression == null) {
            return false;
        }
        JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(schemaMap);
        originExpression.accept(joinExpressionVisitor);
        return joinExpressionVisitor.getExpression() != null;
    }
}