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

    public PhysicalTupleJoinOperator(JoinOperator logicalJoinOp, Deque<PhysicalOperator> physOpChildren) {
        //this.physOpChildren = physOpChildren;
        super(logicalJoinOp, physOpChildren);
    }


    /**
     * implement cross production
     *
     * @return result tuple
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
        int[] newTupleData = new int[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for (int i = 0; i < outerTuple.getDataLength(); i++) {
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for (int i = 0; i < innerTuple.getDataLength(); i++) {
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
    }

}