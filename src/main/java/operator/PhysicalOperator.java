package operator;

import model.Tuple;
import io.TupleWriter;
import model.BufferStateWrapper;
import util.Catalog;
import util.Constants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * Abstract class for operator
 * Created by Yufu Mo
 */
public abstract class PhysicalOperator implements TupleWriter{

    /**
     * get the next tuple of the operator's output
     * return null if the operator has no more output
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

//    /**
//     * @return an list of children of a physical operation
//     */
//    public abstract List<PhysicalOperator> getChildren();

    public void dump(int i) {
        String path = Catalog.getInstance().getOutputPath();
        try{
            File file = new File(path + i);
            FileOutputStream fout = new FileOutputStream(file);
            FileChannel fc = fout.getChannel();
            BufferStateWrapper bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                    ByteBuffer.allocate(Constants.PAGE_SIZE), getSchema().size());

            while (writeNextTuple(bufferStateWrapper)) {
                // the buffer has no space for a tuple
                if (!bufferStateWrapper.hasSpace()) {
                    bufferStateWrapper.writeBuffer(fc);
                    bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                            ByteBuffer.allocate(Constants.PAGE_SIZE), getSchema().size());
                }
                else {

                }
            }
            if (!bufferStateWrapper.bufferIsEmpty()) {
                bufferStateWrapper.writeBuffer(fc);
            }
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean writeNextTuple(BufferStateWrapper bufferStateWrapper) {
        Tuple tuple = getNextTuple();
        if (tuple == null) {
            return false;
        }
        bufferStateWrapper.putTuple(tuple);
        return true;
    }


}
