package io;

import model.Tuple;
import util.Constants;

import java.io.*;
import java.nio.ByteBuffer;

public class ReadableTupleWriter implements TupleWriter{
    private int index;
    private ByteBuffer byteBuffer;
    private int tupleSize;
    private int tupleCount;

    private StringBuilder sb;
    private BufferedWriter output;

    public ReadableTupleWriter(String path, int tupleSize) {
        this.index = 2 * Constants.INT_SIZE;
        this.tupleSize = tupleSize;
        this.tupleCount = 0;
        this.sb = new StringBuilder();

        try {
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.byteBuffer = ByteBuffer.allocate(Constants.PAGE_SIZE);
    }

    @Override
    public void writeNextTuple(Tuple tuple) {
        if (tuple == null) {
            try {
                output.write(sb.toString());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            sb.append(tuple.toString());
            sb.append("\n");
        }
    }

    public void finish() {
        try {
            output.write(sb.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
