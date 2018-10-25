package operator;

import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.Deque;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalTupleJoinOperator extends PhysicalJoinOperator {
    /**
     * Init the schema of JoinOperator
     *
     * @param opLeft      last operator of outer tuple
     * @param opRight     last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalTupleJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, PlainSelect plainSelect) {
        super(opLeft, opRight, plainSelect);
    }

    /**
     * init PhysicalTupleJoinOperator
     * @param logicalJoinOp
     * @param leftChild
     * @param rightChild
     */
    public PhysicalTupleJoinOperator(JoinOperator logicalJoinOp, PhysicalOperator leftChild, PhysicalOperator rightChild) {
        //this.physOpChildren = physOpChildren;
        super(logicalJoinOp, leftChild, rightChild);
    }


    /**
     * implememts a cross production
     * @return next joined tuple
     */
    @Override
    protected Tuple crossProduction() {
        // update outer tuple and inner tuple
        if (outerTuple == null && innerTuple == null) {
            outerTuple = opLeft.getNextTuple();
            innerTuple = opRight.getNextTuple();
        } else {
            innerTuple = opRight.getNextTuple();
            if (innerTuple == null) {
                opRight.reset();
                outerTuple = opLeft.getNextTuple();
                innerTuple = opRight.getNextTuple();
            }
        }
        if (innerTuple == null || outerTuple == null) {
            return null;
        }

        // Concentrate Tuple
        return joinTuple(outerTuple, innerTuple);
    }

}