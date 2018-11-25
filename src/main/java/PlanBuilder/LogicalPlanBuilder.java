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
        // old constructLogicalPlanTree
//        int tableCount;
//        Operator opLeft;
//        if (plainSelect.getJoins() == null) {
//            tableCount = 1;
//        } else {
//            tableCount = 1 + plainSelect.getJoins().size();
//        }
//
//        opLeft = new ScanOperator(plainSelect, 0);
//        if (hasRelatedExpression(opLeft.getSchema(), plainSelect)) {
//            opLeft = new SelectOperator(opLeft, plainSelect);
//        }
//
//        for (int i = 1; i < tableCount; ++i) {
//            Operator opRight = new ScanOperator(plainSelect, i);
//            if (hasRelatedExpression(opRight.getSchema(), plainSelect)) {
//                opRight = new SelectOperator(opRight, plainSelect);
//            }
//            opLeft = new JoinOperator(opLeft, opRight, plainSelect);
//        }
//        if (plainSelect.getSelectItems() != null
//                && plainSelect.getSelectItems().size() > 0
//                && plainSelect.getSelectItems().get(0).toString() != "*")
//            opLeft = new ProjectOperator(opLeft, plainSelect);
//        if (plainSelect.getDistinct() != null) {
//            opLeft = new SortOperator(opLeft, plainSelect);
//            opLeft = new DuplicateEliminationOperator(opLeft);
//        } else {
//            if (plainSelect.getOrderByElements() != null)
//                opLeft = new SortOperator(opLeft, plainSelect);
//        }
//        return opLeft;

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
        plainSelect.getWhere().accept(ufVisitor);
        unionFind = ufVisitor.getUnionFind();
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
            }
            selectOps.add(logicOp);
        }

        Operator logicOp;
        if (selectOps.size() > 1) {
            logicOp = new JoinOperator(selectOps, plainSelect);
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