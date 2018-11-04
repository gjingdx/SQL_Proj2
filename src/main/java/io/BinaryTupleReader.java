package io;

import model.Tuple;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Table Reader, implements the tuple reader and store the buffer
 * Read the table from disk and fetch a tuple
 */
public class BinaryTupleReader implements TupleReader {
    private File file;
    private RandomAccessFile readerPointer;
    private ByteBuffer bufferPage;
    private int tupleSize;
    private int tupleCount;
    private int tuplePointer;

    private long pageIndex = 0;
    private long recordTupleIndex;

    public BinaryTupleReader(String file) {
        this.file = new File(file);
        try {
            reset();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            tupleCount = 0;
        }
    }

    @Override
    public void recordPosition() {
        int maxTupleCountPerPage = (Constants.PAGE_SIZE - 2 * Constants.INT_SIZE) / (tupleSize * Constants.INT_SIZE);
        recordTupleIndex = (pageIndex - 1) * maxTupleCountPerPage +
                (tuplePointer - 2 * Constants.INT_SIZE) / (Constants.INT_SIZE * tupleSize);
    }

    public int getLastReadPageIndex() {
        return (int)pageIndex - 1;
    }

    public int getLastReadTupleInPageIndex() {
        return (tuplePointer - 2 * Constants.INT_SIZE) / (Constants.INT_SIZE * tupleSize) - 1;
    }

    @Override
    public void revertToPosition() throws Exception {
        reset(recordTupleIndex);
    }

    @Override
    public void reset() throws Exception {
        this.readerPointer = new RandomAccessFile(this.file, "r");
        readPage();
    }

    public void readPage() throws Exception {
        try {
            pageIndex++;
            this.bufferPage = ByteBuffer.allocate(Constants.PAGE_SIZE);
            FileChannel inChannel = readerPointer.getChannel();

            int byteRead = inChannel.read(bufferPage);
            if (byteRead == -1) {
                this.tupleCount = 0;
                this.bufferPage = null;
            }
            if (bufferPage != null) {
                tupleSize = bufferPage.getInt(0);
                tupleCount = bufferPage.getInt(Constants.INT_SIZE);
                tuplePointer = 2 * Constants.INT_SIZE;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Unexpected table file format\n");
        }
    }

    @Override
    public Tuple readNextTuple() throws Exception {
        Tuple tuple = null;
        if (this.tupleCount <= 0) {
            return null;
        }
        int[] tupleData = new int[tupleSize];
        if (tuplePointer >= (2 + tupleCount * tupleSize) * Constants.INT_SIZE) {
            readPage();
            if (bufferPage == null) {
                close();
                return null;
            }
        }
        for (int i = 0; i < tupleSize; ++i) {
            tupleData[i] = bufferPage.getInt(tuplePointer);
            tuplePointer += Constants.INT_SIZE;
        }
        tuple = new Tuple(tupleData);
        return tuple;
    }

    @Override
    public void moveBack() throws Exception {
        if (tuplePointer == 0) {
            throw new Exception("Unable to move back");
        }
        if (tuplePointer > 2 * Constants.INT_SIZE) {
            tuplePointer -= tupleSize * Constants.INT_SIZE;
        }
    }

    // the next tuple to read is the ith tuple
    @Override
    public void reset(long i) throws Exception {
        if (i < 0) {
            throw new Exception("Negative tuple position");
        }
        int maxTupleCountPerPage = (Constants.PAGE_SIZE - 2 * Constants.INT_SIZE) / (tupleSize * Constants.INT_SIZE);
        long pageIndex = i / maxTupleCountPerPage;
        long newTuplePointer = ((i % maxTupleCountPerPage) * tupleSize + 2) * Constants.INT_SIZE;
        long newReaderPointer = (long) pageIndex * Constants.PAGE_SIZE;
        if (this.pageIndex - 1 != pageIndex || tupleCount == 0) {
            //need to reread bufferpage
            try {
                this.readerPointer = new RandomAccessFile(this.file, "r");
                readerPointer.seek(newReaderPointer);
                this.pageIndex = pageIndex;
                readPage();
            } catch (Exception e) {
                System.err.println("Failed to reset tuple");
                throw e;
            }
        }
        tuplePointer = (int) newTuplePointer;
    }

    public void reset(int pageIndex, int tupleIndex) throws Exception {
        int maxTupleCountPerPage = (Constants.PAGE_SIZE - 2 * Constants.INT_SIZE) / (tupleSize * Constants.INT_SIZE);
        long i = pageIndex * maxTupleCountPerPage + tupleIndex;
        reset(i);
    }

    @Override
    public void close() throws IOException {
        readerPointer.getChannel().close();
        readerPointer.close();
    }
}