package com.sql.interpreter;


import logical.operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.*;
import operator.PhysicalDuplicateEliminationOperator;
import util.Catalog;
import util.SortJoinExpressionVisitor;

import java.util.*;

public class PhysicalPlanBuilder {

    private Deque<PhysicalOperator> physOpChildren = new LinkedList<>();;


//    private Operator[] getLogicalChildren(Operator logicalOp) {
//        if (physOpChildren == null) {
//            physOpChildren = new LinkedList<>();
//        }
//        Operator[] logicalChildren = logicalOp.getChildren();
//        return logicalChildren;
//    }

    public void visit(ScanOperator logScanOp) {
        PhysicalScanOperator physScanOp = new PhysicalScanOperator(logScanOp);
        physOpChildren.push(physScanOp);
    }

    public void visit(SelectOperator logSelectOp) {
        Operator[] children = logSelectOp.getChildren();
        children[0].accept(this);
        PhysicalSelectOperator physSelectOp = new PhysicalSelectOperator(logSelectOp, physOpChildren);
        physOpChildren.push(physSelectOp);
    }

    public void visit(JoinOperator logicalJoinOp) {
        Operator[] children = logicalJoinOp.getChildren();
        children[0].accept(this);
        children[1].accept(this);
        PhysicalOperator physJoinOp;
        switch(Catalog.getInstance().getJoinMethod()) {
            case TNLJ:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, physOpChildren);
                physOpChildren.push(physJoinOp);
                break;
            case BNLJ:
                physJoinOp = new PhysicalBlockJoinOperator(logicalJoinOp, physOpChildren, Catalog.getInstance().getJoinBlockSize());
                physOpChildren.push(physJoinOp);
                break;
            case SMJ:
                Expression joinCondition = logicalJoinOp.getJoinCondition();
                SortJoinExpressionVisitor sj = new SortJoinExpressionVisitor(children[0].getSchema(), children[1].getSchema());
                joinCondition.accept(sj);
                List<List<OrderByElement>> orders = sj.getOrders();
                if(orders.get(0).size() == 0){
                    physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, physOpChildren);
                    physOpChildren.push(physJoinOp);
                }
                else{
                    //System.out.println(joinCondition.toString());
                    //System.out.println(orders.get(1).toString());
                    //System.out.println(orders.get(0).toString());
                    PhysicalExternalSortOperator rightSort = new PhysicalExternalSortOperator(orders.get(0), physOpChildren);
                    PhysicalExternalSortOperator leftSort = new PhysicalExternalSortOperator(orders.get(1), physOpChildren);

                    physJoinOp = new PhysicalSortMergeJoinOperator(logicalJoinOp, leftSort, rightSort);
                    physOpChildren.push(physJoinOp);
                }
                break;
            default:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, physOpChildren);
                physOpChildren.push(physJoinOp);
        }

    }

    public void visit(ProjectOperator logicalProjOp) {
        Operator[] children = logicalProjOp.getChildren();
        children[0].accept(this);
        PhysicalProjectOperator physProjOp = new PhysicalProjectOperator(logicalProjOp, physOpChildren);
        physOpChildren.push(physProjOp);
    }

    public void visit(SortOperator logSortOp) {
        Operator[] children = logSortOp.getChildren();
        children[0].accept(this);
        PhysicalSortOperator physSelectOp;
        switch (Catalog.getInstance().getSortMethod()) {
            case IN_MEMORY:
                physSelectOp = new PhysicalMemorySortOperator(logSortOp, physOpChildren);
                physOpChildren.push(physSelectOp);
                break;
            case EXTERNAL:
                physSelectOp = new PhysicalExternalSortOperator(logSortOp, physOpChildren);
                physOpChildren.push(physSelectOp);
                break;
            default:
                physSelectOp = new PhysicalMemorySortOperator(logSortOp, physOpChildren);
                physOpChildren.push(physSelectOp);
        }
    }

    public void visit(DuplicateEliminationOperator logDupElimOp) {
        Operator[] children = logDupElimOp.getChildren();
        children[0].accept(this);
        PhysicalDuplicateEliminationOperator physDupEliOp = new PhysicalDuplicateEliminationOperator(logDupElimOp, physOpChildren);
        physOpChildren.push(physDupEliOp);
    }

    public Deque<PhysicalOperator> getPhysOpChildren() {
        return physOpChildren;
    }
}
