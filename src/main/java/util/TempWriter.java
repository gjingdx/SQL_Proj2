package util;

import model.Tuple;
import model.TupleWriter;
import model.BufferStateWrapper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class TempWriter implements TupleWriter{
    File file;
    List<Tuple> tupleList;
    int index;

    public TempWriter(String name){
        file = new File(Catalog.getInstance().getOutputPath() + '\\' + name);
        index = 0;
    }

    public void writeTupleList(List<Tuple> tupleList) throws Exception{
        if(tupleList == null) return;
        int tupleSize = tupleList.get(0).getDataLength();

        FileOutputStream fout = new FileOutputStream(file);
        FileChannel fc = fout.getChannel();
        BufferStateWrapper bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                ByteBuffer.allocate(Constants.PAGE_SIZE), tupleSize);

        while (writeNextTuple(bufferStateWrapper)) {
            // the buffer has no space for a tuple
            if (!bufferStateWrapper.hasSpace()) {
                bufferStateWrapper.writeBuffer(fc);
                bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                        ByteBuffer.allocate(Constants.PAGE_SIZE), tupleSize);
            }
            else {

            }
        }
        if (!bufferStateWrapper.bufferIsEmpty()) {
            bufferStateWrapper.writeBuffer(fc);
        }
        fout.close();
    }

    @Override
    public boolean writeNextTuple(BufferStateWrapper bufferStateWrapper){
        if(index >= tupleList.size()){
            return false;
        }
        Tuple tuple = tupleList.get(index++);
        bufferStateWrapper.putTuple(tuple);
        return true;
    }

    public void reset(){
        index = 0;
    }
    
}