package io;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import model.Tuple;
import util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BinaryTupleWriter implements TupleWriter {

    private int index;
    private ByteBuffer byteBuffer;
    private int tupleSize;
    private int tupleCount;
    private FileOutputStream fileOutputStream;
    private FileChannel fileChannel;

    public BinaryTupleWriter(String path, int tupleSize) {
        this.index = 2 * Constants.INT_SIZE;
        this.tupleSize = tupleSize;
        this.tupleCount = 0;

        try {
            File file = new File(path);
            this.fileOutputStream = new FileOutputStream(file);
            this.fileChannel = fileOutputStream.getChannel();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.byteBuffer = ByteBuffer.allocate(Constants.PAGE_SIZE);
    }

    @Override
    public void writeNextTuple(Tuple tuple) {
        if (tuple == null) {
            if (!bufferIsEmpty()) {
                writeBuffer();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (!bufferHasSpace()) {
                writeBuffer();
            }
            putTuple(tuple);
        }
    }

    public void finish() {
        if (!bufferIsEmpty()) {
            writeBuffer();
        }
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * @return
     */
    public boolean writeBuffer() {
        // put metadata
        byteBuffer.putInt(0, tupleSize);
        byteBuffer.putInt(Constants.INT_SIZE, tupleCount);
        try{
            fileChannel.write(byteBuffer);
            // reset buffer
            this.index = 2 * Constants.INT_SIZE;
            this.tupleCount = 0;
            this.byteBuffer = ByteBuffer.allocate(Constants.PAGE_SIZE);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
    public boolean bufferHasSpace() {
        return index + tupleSize * Constants.INT_SIZE <= Constants.PAGE_SIZE;
    }
}
