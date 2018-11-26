package PlanBuilder;

import logical.operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.JoinExpressionVisitor;
import util.UnionFindExpressionVisitor;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.util.*;

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
        // get number of tables used in total which is num of scanOp
        int numTable;
        if (plainSelect.getJoins() == null) {
            numTable = 1;
        } else {
            numTable = 1 + plainSelect.getJoins().size();
        }

        //List<Operator> scanOps = new ArrayList<>(); // list of scan Ops
        // list of select Ops or scan Op that pass to JoinOp
        List<Operator> selectOps = new ArrayList<>();

        UnionFind unionFind = new UnionFind();
        UnionFindExpressionVisitor ufVisitor = new UnionFindExpressionVisitor(unionFind);
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(ufVisitor);
            unionFind = ufVisitor.getUnionFind();
        }
        Set<String> attributes = unionFind.getAttributeSet();
        for (int i = 0; i < numTable; i++) {
            Operator logicOp = new ScanOperator(plainSelect, i);
            //scanOps.add(scan);
            Map<String, Constraints> constraints = new HashMap<>();
            for (String attribute : attributes) {
                if (logicOp.getSchema().containsKey(attribute) && !unionFind.find(attribute).isNull()) {
                    constraints.put(attribute, unionFind.find(attribute));
                }
            }
            if (!constraints.isEmpty()) {
                logicOp = new SelectOperator(logicOp, constraints, plainSelect);
            } else if (numTable == 1 && plainSelect.getWhere() != null) {
                logicOp = new SelectOperator(logicOp, constraints, plainSelect);
            }
            selectOps.add(logicOp);
        }

        Operator logicOp;
        if (selectOps.size() > 1) {
            logicOp = new JoinOperator(selectOps, plainSelect, true);
        } else {
            logicOp = selectOps.get(0);
        }
        if (plainSelect.getSelectItems() != null
                && plainSelect.getSelectItems().size() > 0
                && plainSelect.getSelectItems().get(0).toString() != "*")
            logicOp = new ProjectOperator(logicOp, plainSelect);
        if (plainSelect.getDistinct() != null) {
            logicOp = new SortOperator(logicOp, plainSelect);
            logicOp = new DuplicateEliminationOperator(logicOp);
        } else {
            if (plainSelect.getOrderByElements() != null)
                logicOp = new SortOperator(logicOp, plainSelect);
        }
        return logicOp;
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