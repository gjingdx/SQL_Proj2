package com.sql.interpreter;

import logical.operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.*;
import util.Catalog;
import util.Constants.SortMethod;
import util.SortJoinExpressionVisitor;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class PhysicalPlanBuilder {

    private Deque<PhysicalOperator> physOpChildren = new LinkedList<>();

    /**
     * visit ScanOperator
     *
     * @param logScanOp
     */
    public void visit(ScanOperator logScanOp) {
        PhysicalScanOperator physScanOp = new PhysicalScanOperator(logScanOp);
        physOpChildren.push(physScanOp);
    }

    /**
     * @param logSelectOp
     */
    public void visit(SelectOperator logSelectOp) {
        Operator[] children = logSelectOp.getChildren();
        children[0].accept(this);
        PhysicalOperator child = physOpChildren.pop();
        PhysicalSelectOperator physSelectOp = new PhysicalSelectOperator(logSelectOp, child);
        physOpChildren.push(physSelectOp);
    }

    /**
     * @param logicalJoinOp
     */
    public void visit(JoinOperator logicalJoinOp) {
        Operator[] children = logicalJoinOp.getChildren();
        children[0].accept(this);
        children[1].accept(this);
        PhysicalOperator rightChild = physOpChildren.pop();
        PhysicalOperator leftChild = physOpChildren.pop();
        PhysicalOperator physJoinOp;
        switch (Catalog.getInstance().getJoinMethod()) {
            case TNLJ:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, leftChild, rightChild);
                physOpChildren.push(physJoinOp);
                break;
            case BNLJ:
                physJoinOp = new PhysicalBlockJoinOperator(logicalJoinOp, leftChild, rightChild,
                        Catalog.getInstance().getJoinBlockSize());
                physOpChildren.push(physJoinOp);
                break;
            case SMJ:
                Expression joinCondition = logicalJoinOp.getJoinCondition();
                // if there is no join condition, there will be no SMJ implements. 
                // So does no order extracted from join condition
                if (joinCondition != null) {
                    SortJoinExpressionVisitor sj = new SortJoinExpressionVisitor(children[0].getSchema(), children[1].getSchema());
                    joinCondition.accept(sj);
                    List<List<OrderByElement>> orders = sj.getOrders();
                    if (orders.get(0).size() != 0) {
                        PhysicalSortOperator rightSort, leftSort;
                        if (Catalog.getInstance().getSortMethod() == SortMethod.EXTERNAL) {
                            rightSort = new PhysicalExternalSortOperator(orders.get(0), rightChild);
                            leftSort = new PhysicalExternalSortOperator(orders.get(1), leftChild);
                        } else {
                            rightSort = new PhysicalMemorySortOperator(orders.get(0), rightChild);
                            leftSort = new PhysicalMemorySortOperator(orders.get(1), leftChild);
                        }
                        physJoinOp = new PhysicalSortMergeJoinOperator(logicalJoinOp, leftSort, rightSort);
                        physOpChildren.push(physJoinOp);
                        break;
                    }
                }
            default:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, leftChild, rightChild);
                physOpChildren.push(physJoinOp);
        }

    }

    /**
     * @param logicalProjOp
     */
    public void visit(ProjectOperator logicalProjOp) {
        Operator[] children = logicalProjOp.getChildren();
        children[0].accept(this);
        PhysicalOperator child = physOpChildren.pop();
        PhysicalProjectOperator physProjOp = new PhysicalProjectOperator(logicalProjOp, child);
        physOpChildren.push(physProjOp);
    }

    /**
     * @param logSortOp
     */
    public void visit(SortOperator logSortOp) {
        Operator[] children = logSortOp.getChildren();
        children[0].accept(this);
        PhysicalOperator child = physOpChildren.pop();
        PhysicalSortOperator physSelectOp;
        switch (Catalog.getInstance().getSortMethod()) {
            case IN_MEMORY:
                physSelectOp = new PhysicalMemorySortOperator(logSortOp, child);
                physOpChildren.push(physSelectOp);
                break;
            case EXTERNAL:
                physSelectOp = new PhysicalExternalSortOperator(logSortOp, child);
                physOpChildren.push(physSelectOp);
                break;
            default:
                physSelectOp = new PhysicalMemorySortOperator(logSortOp, child);
                physOpChildren.push(physSelectOp);
        }
    }

    /**
     * @param logDupElimOp
     */
    public void visit(DuplicateEliminationOperator logDupElimOp) {
        Operator[] children = logDupElimOp.getChildren();
        children[0].accept(this);
        PhysicalOperator child = physOpChildren.pop();
        PhysicalDuplicateEliminationOperator physDupEliOp =
                new PhysicalDuplicateEliminationOperator(logDupElimOp, child);
        physOpChildren.push(physDupEliOp);
    }

    /**
     * @return the physOpChildren stack
     */
    public Deque<PhysicalOperator> getPhysOpChildren() {
        return physOpChildren;
    }
}
