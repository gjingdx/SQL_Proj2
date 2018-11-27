package PlanBuilder;

import logical.operator.*;
import model.IndexConfig;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.*;
import util.Catalog;
import util.Constants.SortMethod;
import util.IndexScanExpressionVisitor;
import util.SortJoinExpressionVisitor;
import util.unionfind.Constraints;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * visitor class to traverse the logical plan tree and print it out
 *
 * @author Yufu Mo ym445
 */
public class LogicalOperatorVisitor {

    // keep track of the current depth for number of '-'
    private int level;
    private List<String> hierarchy = new ArrayList<>();

    /**
     * visit ScanOperator
     * print out the line for scan operator
     *
     * @param logScanOp
     */
    public void visit(ScanOperator logScanOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("Leaf[");
        sb.append(logScanOp.getTable());
        sb.append(']');
        hierarchy.add(sb.toString());
    }

    /**
     * print out the line for select operator
     *
     * @param logSelectOp
     */
    public void visit(SelectOperator logSelectOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("Select[");
        if (logSelectOp.getExpression() != null) {
            sb.append(logSelectOp.getExpression().toString());
        }
        sb.append(']');
        hierarchy.add(sb.toString());
        for (Operator operator : logSelectOp.getChildren()) {
            level++;
            operator.accept(this);
            level--;
        }
    }

    /**
     * print out the line for join operator
     * print out the constraints for each column to join
     *
     * @param logicalJoinOp
     */
    public void visit(JoinOperator logicalJoinOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("Join[");
        if (logicalJoinOp.getJoinCondition() != null) {
            sb.append(logicalJoinOp.getJoinCondition().toString());
        }
        sb.append(']');
        hierarchy.add(sb.toString());
        hierarchy.addAll(logicalJoinOp.getUnionFind().printUnions());
        for (Operator operator : logicalJoinOp.getChildren()) {
            level++;
            operator.accept(this);
            level--;
        }
    }

    /**
     * print out the line for project operator
     *
     * @param logicalProjOp
     */
    public void visit(ProjectOperator logicalProjOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("Project[");
        if (logicalProjOp.getSelectItems() != null) {
            sb.append(logicalProjOp.getSelectItems().toString());
        }
        sb.append(']');
        hierarchy.add(sb.toString());
        for (Operator operator : logicalProjOp.getChildren()) {
            level++;
            operator.accept(this);
            level--;
        }
    }

    /**
     * print out the line for sort operator
     *
     * @param logSortOp
     */
    public void visit(SortOperator logSortOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("Sort[");
        if (logSortOp.getOrder() != null) {
            sb.append(logSortOp.getOrder().toString());
        }
        sb.append(']');
        hierarchy.add(sb.toString());
        for (Operator operator : logSortOp.getChildren()) {
            level++;
            operator.accept(this);
            level--;
        }
    }

    /**
     * print out the line for duplicate elimination operator
     *
     * @param logDupElimOp
     */
    public void visit(DuplicateEliminationOperator logDupElimOp) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('-');
        }
        sb.append("DupElim");
        hierarchy.add(sb.toString());
        for (Operator operator : logDupElimOp.getChildren()) {
            level++;
            operator.accept(this);
            level--;
        }
    }

    /**
     * @return the hierarchy string list to print
     */
    public List<String> getOutput() {
        return hierarchy;
    }
}
