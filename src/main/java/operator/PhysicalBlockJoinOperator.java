package operator;

import logical.operator.JoinOperator;
import model.Block;
import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalBlockJoinOperator extends PhysicalJoinOperator {
    Block block;

    /**
     * Init the schema of JoinOperator
     *
     * @param opLeft      last operator of outer tuple
     * @param opRight     last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalBlockJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, PlainSelect plainSelect, int blockSize) {
        super(opLeft, opRight, plainSelect);
        this.block = new Block(blockSize, opLeft.getSchema().size());
    }

    public PhysicalBlockJoinOperator(JoinOperator logicalJoinOp, PhysicalOperator leftChild, PhysicalOperator rightChild, int blockSize) {
        super(logicalJoinOp, leftChild, rightChild);
        this.block = new Block(blockSize, opLeft.getSchema().size());
    }

    /**
     * implements block join
     *
     * @return the next joined tuple
     */
    @Override
    protected Tuple crossProduction() {
        // import outer pages into the block
        if (block.isAllNull()) {
            loadOuterTupleIntoBlock();
            // all outer pages are read
            if (block.isAllNull()) {
                return null;
            }
        }

        if (outerTuple == null && innerTuple == null) {
            outerTuple = block.readNextTuple();
            innerTuple = opRight.getNextTuple();
        } else {
            outerTuple = block.readNextTuple();
            if (outerTuple == null) {
                block.reset();
                innerTuple = opRight.getNextTuple();
                outerTuple = block.readNextTuple();
            }
        }
        if (innerTuple == null) {
            block.clearData();
            loadOuterTupleIntoBlock();
            if (block.isAllNull()) {
                return null;
            }
            outerTuple = block.readNextTuple();
            opRight.reset();
            innerTuple = opRight.getNextTuple();
        }
        if (outerTuple == null || innerTuple == null) {
            return null;
        }

        // Concentrate Tuple
        return joinTuple(outerTuple, innerTuple);
    }

    private void loadOuterTupleIntoBlock() {
        Tuple leftTuple;
        while ((leftTuple = opLeft.getNextTuple()) != null) {
            if (!block.setNextTuple(leftTuple)) {
                break;
            }
        }
    }

}