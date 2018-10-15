package operator;

import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.*;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * PhysicalJoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalJoinOperator extends PhysicalOperator {
    protected PhysicalOperator opLeft, opRight;

    //private Deque<PhysicalOperator> physOpChildren;
    protected Expression joinCondition;
    protected Map<String, Integer> schema;
    protected Tuple outerTuple;
    protected Tuple innerTuple;

    /**
     * Init the schema of PhysicalJoinOperator
     * @param opLeft last operator of outer tuple
     * @param opRight last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, PlainSelect plainSelect){
        this.opLeft = opLeft;
        this.opRight = opRight;
        this.schema = new HashMap<>();
        schema.putAll(opLeft.getSchema());
        for (Map.Entry<String, Integer> entry : opRight.getSchema().entrySet()) {
            schema.put(entry.getKey(), entry.getValue() + opLeft.getSchema().size());
        }
        Catalog.getInstance().setCurrentSchema(schema);

        outerTuple = null;
        innerTuple = null;
    }

    public PhysicalJoinOperator(JoinOperator logicalJoinOp, Deque<PhysicalOperator> physOpChildren) {
        //this.physOpChildren = physOpChildren;
        this.opRight = physOpChildren.pop();
        this.opLeft = physOpChildren.pop();
        this.joinCondition = logicalJoinOp.getJoinCondition();
        this.schema = logicalJoinOp.getSchema();

        outerTuple = null;
        innerTuple = null;
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        Tuple next = crossProduction();
        // return cross product if there's no selection
        if(joinCondition == null){
            return next;
        }
        
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
    protected Tuple crossProduction(){
        // update outer tuple and inner tuple
        if(outerTuple == null && innerTuple == null){
            outerTuple = opLeft.getNextTuple();
            innerTuple = opRight.getNextTuple();
        }
        else{
            innerTuple = opRight.getNextTuple();
            if(innerTuple == null){
                opRight.reset();
                outerTuple = opLeft.getNextTuple();
                innerTuple = opRight.getNextTuple();
            }
        }
        if(innerTuple == null || outerTuple == null){
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

}