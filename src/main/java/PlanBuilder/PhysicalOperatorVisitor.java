package PlanBuilder;

import logical.operator.Operator;
import operator.*;
import util.Catalog;

import java.util.Deque;
import java.util.LinkedList;

/**
 * print all the physical query Plan
 * Class PhysicalOperatorVisitor
 *
 * @author jg2273
 */
public class PhysicalOperatorVisitor {

    private Deque<PhysicalOperator> physOpChildren = new LinkedList<>();
    private StringBuilder phPBTree = new StringBuilder();

    /**
     * visit (print) PhysicalDuplicateEliminationOperator
     * @param phDupElimOp
     * @param level
     */
    public void visit(PhysicalDuplicateEliminationOperator phDupElimOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("DupElim");
        phPBTree.append("\n");
        for (PhysicalOperator child : phDupElimOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalExternalSortOperator
     * @param phExternalSortOp
     * @param level
     */
    public void visit(PhysicalExternalSortOperator phExternalSortOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("ExternalSort[");
        // get sort by attribute
        if (phExternalSortOp.getOrder() != null) {
            phPBTree.append(phExternalSortOp.getOrder().get(0));
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : phExternalSortOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalMemorySortOperator
     * @param memorySortOp
     * @param level
     */
    public void visit(PhysicalMemorySortOperator memorySortOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("MemorySort[");
        if (memorySortOp.getOrder() != null) {
            phPBTree.append(memorySortOp.getOrder().get(0));
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : memorySortOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalProjectOperator
     * @param phProjOp
     * @param level
     */
    public void visit(PhysicalProjectOperator phProjOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("Project");
        if (phProjOp.getSelectItems() != null) {
            phPBTree.append(phProjOp.getSelectItems().toString());
        } else {
            phPBTree.append("[]");
        }
        phPBTree.append("\n");
        for (PhysicalOperator child : phProjOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalSortMergeJoinOperator
     * @param sMJOp
     * @param level
     */
    public void visit(PhysicalSortMergeJoinOperator sMJOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("SMJ[");
        if (sMJOp.getJoinCondition() != null) {
            phPBTree.append(sMJOp.getJoinCondition().toString());
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : sMJOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }


    /**
     * visit (print) PhysicalHashJoinOperator
     * @param hashJoinOperator
     * @param level
     */
    public void visit(PhysicalHashJoinOperator hashJoinOperator, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("HSHJ[");
        if (hashJoinOperator.getJoinCondition() != null) {
            phPBTree.append(hashJoinOperator.getJoinCondition().toString());
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : hashJoinOperator.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalBlockJoinOperator
     * @param blockJoinOp
     * @param level
     */
    public void visit(PhysicalBlockJoinOperator blockJoinOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("BNLJ[");
        if (blockJoinOp.getJoinCondition() != null) {
            phPBTree.append(blockJoinOp.getJoinCondition().toString());
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : blockJoinOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalTupleJoinOperator
     * @param tupleJoinOp
     * @param level
     */
    public void visit(PhysicalTupleJoinOperator tupleJoinOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("TNLJ[");
        if (tupleJoinOp.getJoinCondition() != null) {
            phPBTree.append(tupleJoinOp.getJoinCondition().toString());
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : tupleJoinOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalSelectOperator
     * @param phSelectOp
     * @param level
     */
    public void visit(PhysicalSelectOperator phSelectOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("Select[");
        if (phSelectOp.getExpression() != null) {
            phPBTree.append(phSelectOp.getExpression().toString());
        }
        phPBTree.append("]\n");
        for (PhysicalOperator child : phSelectOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    /**
     * visit (print) PhysicalScanOperator
     * @param phScanOp
     * @param level
     */
    public void visit(PhysicalScanOperator phScanOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("TableScan[");
        phPBTree.append(phScanOp.getTableName());
        phPBTree.append("]\n");
    }

    /**
     * visit (print) PhysicalIndexScanOperator
     * @param indexScanOp
     * @param level
     */
    public void visit(PhysicalIndexScanOperator indexScanOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("IndexScan[");
        phPBTree.append(indexScanOp.getTableName() + ",");
        //get index attribute
        phPBTree.append(indexScanOp.getIndexConfig().columnName + ",");
        phPBTree.append(indexScanOp.getLowKey() + ",");
        phPBTree.append(indexScanOp.getHighKey());
        phPBTree.append("]\n");
    }

    public StringBuilder getPhPBTree() {
        return phPBTree;
    }
}
