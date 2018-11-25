package operator;

import java.io.File;
import java.util.Map;

import btree.Deserializer;
import btree.Rid;
import logical.operator.*;
import model.*;
import net.sf.jsqlparser.expression.Expression;
import util.*;

/**
 * Index Scan operator reads a index file to make scan and selection at the same time
 */
public class PhysicalIndexScanOperator extends PhysicalScanOperator {
    private Expression selectCondition;
    private Deserializer deserializer;
    private IndexConfig indexConfig;
    private int attr;
    private int lowKey;
    private int highKey;
    Rid startRid;
    Rid tempRid;
    
    /**
     * Constructor of PhysicalIndexScanOperator
     * 
     * @param logSelectOp related logical operator
     * @param lowKey, if none, parser MIN_INT
     * @param highKey, if none, parser MAX_INT
     */
    public PhysicalIndexScanOperator(SelectOperator logSelectOp, int lowKey, int highKey){
        super((ScanOperator)logSelectOp.getChildren().get(0));
        this.indexConfig = Catalog.getInstance().getIndexConfig(schema);
        this.selectCondition = logSelectOp.getExpression();

        String indexFile = indexConfig.indexFile;
        try {
            this.deserializer = new Deserializer(new File(indexFile), lowKey, highKey);
        
            this.attr = Catalog.getInstance().getTableSchema(indexConfig.tableName).get(indexConfig.schemaName);
            this.highKey = highKey;
            this.lowKey = lowKey;

            this.startRid = deserializer.getNextRid();
            this.tempRid = startRid;
        } catch (Exception e) {
            deserializer = null;
            e.printStackTrace();
        }
    }

    @Override
    public Tuple getNextTuple() {
        if (startRid == null) {
            return null;
        }

        Tuple next = nextTuple();
        while (next != null) {
            SelectExpressionVisitor sv = new SelectExpressionVisitor(next, this.getSchema());
            selectCondition.accept(sv);
            if (sv.getResult()) {
                break;
            }
            next = nextTuple();
        }
        return next;
    }

    // if unclustered, get the next tuple from data entry
    private Tuple getNextTupleFromIndex() {
        if (tempRid == null) {
            return null;
        }
        try {
            binaryTupleReader.reset(tempRid.getPageId(), tempRid.getTupleId());
            Tuple tuple = binaryTupleReader.readNextTuple();
            tempRid = deserializer.getNextRid();
            return tuple;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Tuple nextTuple() {
        if (indexConfig.isClustered) {
            Tuple tuple = super.getNextTuple();
            if (tuple == null) {
                return null;
            }
            if (tuple.getDataAt(attr) <= highKey) {
                return tuple;
            }
            else {
                return null;
            }
        }
        else {
            return getNextTupleFromIndex();
        }
    }

    @Override
    public void reset(){
        try {
            binaryTupleReader.reset();
            deserializer.reset();
            this.startRid = deserializer.getNextRid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }
}