package operator;

import io.*;
import model.Tuple;
import util.Catalog;

import java.util.Map;

/**
 * Abstract class for operator
 * Created by Yufu Mo
 */
public abstract class PhysicalOperator {

    /**
     * get the next tuple of the operator's output
     * return null if the operator has no more output
     *
     * @return the next tuple of the operator's output
     */
    public abstract Tuple getNextTuple();

    /**
     * reset the operator's state and start returning its output again from the
     * beginning
     */
    public abstract void reset();

    /**
     * @return the current schema of the operator
     */
    public abstract Map<String, Integer> getSchema();


    public void dump(int i) {
        String path = Catalog.getInstance().getOutputPath() + i;
        TupleWriter tupleWriter = new BinaryTupleWriter(path, getSchema().size());
        Tuple tuple = getNextTuple();
        while (tuple != null) {
            tupleWriter.writeNextTuple(tuple);
            tuple = getNextTuple();
        }
        // finish
        tupleWriter.finish();
    }

}
