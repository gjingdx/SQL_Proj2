package operator;

import java.io.File;
import java.util.Map;

import btree.Deserializer;
import btree.Rid;
import io.*;
import logical.operator.*;
import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import util.*;

public class PhysicalIndexScanOperator extends PhysicalOperator {
    private Expression selectCondition;
    private Map<String, Integer> schema;

    private BinaryTupleReader tupleReader;
    private Deserializer deserializer;
    

    public PhysicalIndexScanOperator(SelectOperator logSelectOp, int lowKey, int highKey, String indexFile, String tableFile){
        this.selectCondition = logSelectOp.getExpression();
        this.schema = logSelectOp.getSchema();

        this.tupleReader = new BinaryTupleReader(tableFile);
        deserializer = new Deserializer(new File(indexFile), lowKey, highKey);
    }

    @Override
    public Tuple getNextTuple() {
        Tuple next = getNextTupleFromIndex();
        // clusetered do not need get next rid
        while (next != null) {
            SelectExpressionVisitor sv = new SelectExpressionVisitor(next, this.getSchema());
            selectCondition.accept(sv);
            if (sv.getResult()) {
                break;
            }
            next = getNextTupleFromIndex();
        }
        return next;
    }

    private Tuple getNextTupleFromIndex(){
        Rid rid = deserializer.getNextRid();
        if (rid == null) {
            return null;
        }
        try {
            tupleReader.reset(rid.getPageId(), rid.getTupleId());
            Tuple tuple = tupleReader.readNextTuple();
            return tuple;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void reset(){
        try {
            tupleReader.reset();
            deserializer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }
}