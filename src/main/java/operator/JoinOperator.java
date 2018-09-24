package operator;

import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;

import java.util.HashMap;
import java.util.Map;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class JoinOperator extends Operator{
    private Operator opLeft, opRight;
    private PlainSelect plainSelect;
    private Map<String, Integer> schema;
    Tuple outerTuple;
    Tuple innerTuple;

    /**
     * Init the schema of JoinOperator
     * @param opLeft last operator of outer tuple
     * @param opRight last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public JoinOperator(Operator opLeft, Operator opRight, PlainSelect plainSelect){
        this.opLeft = opLeft;
        this.opRight = opRight;
        this.plainSelect = plainSelect;
        this.schema = new HashMap<>();
        schema.putAll(opLeft.getSchema());
        for (Map.Entry<String, Integer> entry : opRight.getSchema().entrySet()) {
            schema.put(entry.getKey(), entry.getValue() + opLeft.getSchema().size());
        }
        Catalog.getInstance().setCurrentSchema(schema);

        outerTuple = null;
        innerTuple = null;
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
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
        long[] newTupleData = new long[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for(int i = 0; i < outerTuple.getDataLength(); i++){
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for(int i = 0; i < innerTuple.getDataLength(); i++){
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
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

}