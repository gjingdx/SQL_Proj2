package io;

import model.Tuple;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ReadableTupleReader implements TupleReader {
    File file;
    RandomAccessFile readerPointer;
    int tupleCount = 0;
    int tupleSize = 0;
    List<Tuple> tupleList;
    int readIndex = 0;
    long lineSize = 0;
    long recordPosition = 0;

    public ReadableTupleReader(String file, int tupleSize) {
        this.file = new File(file);
        this.tupleSize = tupleSize;
        this.tupleCount = Constants.PAGE_SIZE / (tupleSize * Constants.INT_SIZE);
        tupleList = new ArrayList<>();
        try {
            this.readerPointer = new RandomAccessFile(this.file, "r");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        try {
            readerPointer.seek(0);
            readIndex = 0;
            tupleList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple readNextTuple() {
        try {
            long p1 = readerPointer.getFilePointer();
            Tuple ret = new Tuple(readerPointer.readLine());
            this.lineSize = readerPointer.getFilePointer() - p1;
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void moveBack() {
        try {
            readerPointer.seek(readerPointer.getFilePointer() - lineSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void recordPosition() {
        try {
            recordPosition = readerPointer.getFilePointer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revertToPosition() {
        try {
            readerPointer.seek(recordPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset(long i) {
        try {
            readerPointer.seek(i * lineSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            readerPointer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}