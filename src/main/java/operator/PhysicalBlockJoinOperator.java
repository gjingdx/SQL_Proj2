package operator;

import model.Tuple;
import model.Block;
import logical.operator.JoinOperator;

import java.util.Deque;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalBlockJoinOperator extends PhysicalJoinOperator{
    Block block;
    /**
     * Init the schema of JoinOperator
     * @param opLeft last operator of outer tuple
     * @param opRight last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalBlockJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, PlainSelect plainSelect, int blockSize){
        super(opLeft, opRight, plainSelect);
        this.block = new Block(blockSize, opLeft.getSchema().size());
    }

    public PhysicalBlockJoinOperator(JoinOperator logicalJoinOp, Deque<PhysicalOperator> physOpChildren, int blockSize) {
        super(logicalJoinOp, physOpChildren);
        this.block = new Block(blockSize, opLeft.getSchema().size());
        System.out.println(schema.toString());
    }

    /**
     * implement cross production
     * @return result tuple
     */
    @Override
    protected Tuple crossProduction(){
        // import outer pages into the block
        if(block.isAllNull()){
            loadOuterTupleIntoBlock();
            // all outer pages are read
            if(block.isAllNull()){
                return null;
            }
        }

        if(outerTuple == null && innerTuple == null){
            outerTuple = block.readNextTuple();
            innerTuple = opRight.getNextTuple();
        }
        else{
            outerTuple = block.readNextTuple();
            if(outerTuple == null){
                block.reset();
                innerTuple = opRight.getNextTuple();
                outerTuple = block.readNextTuple();
            }
        }
        if(innerTuple == null){
            block.clearData();
            loadOuterTupleIntoBlock();
            if(block.isAllNull()){
                return null;
            }
            outerTuple = block.readNextTuple();
            opRight.reset();
        }
        if(outerTuple == null || innerTuple == null){
            return null;
        }

        // Concentrate Tuple
        int[] newTupleData = new int[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for(int i = 0; i < outerTuple.getDataLength(); i++){
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for(int i = 0; i < innerTuple.getDataLength(); i++){
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
    }

    private void loadOuterTupleIntoBlock(){
        Tuple leftTuple;
        while((leftTuple = opLeft.getNextTuple()) != null){
            if(!block.setNextTuple(leftTuple)){
                break;
            }
        }
    }

}