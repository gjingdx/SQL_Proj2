package com.sql.interpreter;


import logical.operator.*;
import operator.*;
import operator.PhysicalDuplicateEliminationOperator;
import util.Catalog;
import util.Catalog.*;
import util.Constants.*;

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
        // TODO: need to switch cases for JoinOps
        PhysicalJoinOperator physJoinOp;
        switch(Catalog.getInstance().getJoinMethod()) {
            case TNLJ:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, physOpChildren);
                physOpChildren.push(physJoinOp);
                break;
            case BNLJ:
                physJoinOp = new PhysicalBlockJoinOperator(logicalJoinOp, physOpChildren, 2);
                physOpChildren.push(physJoinOp);
                break;
            case SMJ:
                physJoinOp = new PhysicalTupleJoinOperator(logicalJoinOp, physOpChildren);
                physOpChildren.push(physJoinOp);
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
        // TODO: need to switch cases to SortOps
        switch (Catalog.getInstance().getSortMethod()) {
            case IN_MEMORY:
                physSelectOp = new PhysicalSortOperator(logSortOp, physOpChildren);
                physOpChildren.push(physSelectOp);
                break;
            case EXTERNAL:
                physSelectOp = new PhysicalSortOperator(logSortOp, physOpChildren);
                physOpChildren.push(physSelectOp);
                break;
            default:
                physSelectOp = new PhysicalSortOperator(logSortOp, physOpChildren);
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
