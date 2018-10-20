package io;

import model.Tuple;
import util.Constants;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A wrapper class to encapsulate byte buffer and relevant states including
 * index, tuple size and counts.
 * Provide write and read method.
 * @author Yufu Mo
 */
public class BufferStateWrapper {
    private int index;
    private ByteBuffer byteBuffer;
    private int tupleSize;
    private int tupleCount;

    public BufferStateWrapper(int index, ByteBuffer byteBuffer, int tupleSize) {
        this.index = index;
        this.byteBuffer = byteBuffer;
        this.tupleSize = tupleSize;
        tupleCount = 0;
    }

    public BufferStateWrapper(int tupleSize) {
        this.index = 2 * Constants.INT_SIZE;
        this.byteBuffer = ByteBuffer.allocate(Constants.PAGE_SIZE);
        this.tupleSize = tupleSize;
        tupleCount = 0;
    }

    /**
     * put int in buffer
     * @param data
     */
    public void putInt(int data) {
        byteBuffer.putInt(index, data);
        index += Constants.INT_SIZE;
    }

    /**
     * put tuple in buffer
     * @param tuple
     */
    public void putTuple(Tuple tuple) {
        for (int i = 0; i < tuple.getDataLength(); i++) {
            putInt(tuple.getDataAt(i));
        }
        tupleCount++;
    }

    /**
     * write buffer to file
     * @param fileChannel
     * @return
     */
    public boolean writeBuffer(FileChannel fileChannel) {
        putMetaData();
        try{
            fileChannel.write(byteBuffer);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * put tuple size and tuple count at first 8 bytes
     */
    public void putMetaData() {
        byteBuffer.putInt(0, tupleSize);
        byteBuffer.putInt(Constants.INT_SIZE, tupleCount);
    }

    /**
     * check if buffer is empty, for the edge case when there is a new empty BufferStateWrapper created
     * @return
     */
    public boolean bufferIsEmpty() {
        return index == Constants.INT_SIZE * 2;
    }

    /**
     * check if the buffer has space for a tuple
     * @return
     */
    public boolean hasSpace() {
        return index + tupleSize * Constants.INT_SIZE <= Constants.PAGE_SIZE;
    }
}
