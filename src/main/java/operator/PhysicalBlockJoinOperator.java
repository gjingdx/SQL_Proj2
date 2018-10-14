package operator;

import model.Tuple;
import model.Block;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.*;

import java.util.Map;

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

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        Tuple next = crossProduction();
        Expression expr = plainSelect.getWhere();

        // return cross product if there's no selection
        if(expr == null){
            return next;
        }
        // join by join condition
        JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(this.schema);
        expr.accept(joinExpressionVisitor);
        Expression joinCondition = joinExpressionVisitor.getExpression();
        
        while(next != null){
            SelectExpressionVisitor sv = new SelectExpressionVisitor(next, this.getSchema());
            joinCondition.accept(sv);
            if (sv.getResult()) {
                break;
            }
            next = crossProduction();
        }
        return next;
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset(){
        opLeft.reset();
        opRight.reset();
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
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
            innerTuple = opRight.getNextTuple();
            if(innerTuple == null){
                opRight.reset();
                outerTuple = block.readNextTuple();
                innerTuple = opRight.getNextTuple();
            }
        }
        if(outerTuple == null){
            block.clearData();
            loadOuterTupleIntoBlock();
            if(block.isAllNull()){
                return null;
            }
            outerTuple = block.readNextTuple();
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