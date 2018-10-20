package io;

import model.Tuple;
import io.TupleReader;
import util.Constants;

import java.io.*;
import java.util.*;

public class ReadableTableReader implements TupleReader{
    File file;
    RandomAccessFile readerPointer;
    int tupleCount = 0;
    int tupleSize = 0;
    List<Tuple> tupleList;
    int readIndex = 0;

    public ReadableTableReader(String file, int tupleSize){
        this.file = new File(file);
        this.tupleSize = tupleSize;
        this.tupleCount = Constants.PAGE_SIZE / (tupleSize * Constants.INT_SIZE);
        tupleList = new ArrayList<>();
        try{
            this.readerPointer = new RandomAccessFile(this.file, "r");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void init(){
        try{
            readerPointer.seek(0);
            readIndex = 0;
            tupleList.clear();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void readPage(){
        tupleList.clear();
        readIndex = 0;
        if(tupleCount == 0)return;
        try{
            for(int i =0; i< tupleCount; i++){
                String str = readerPointer.readLine();
                if(str == null){
                    tupleCount = 0;
                    tupleSize = 0;
                    break;
                }
                tupleList.add(new Tuple(str));
            }
        }catch(IOException e){
            System.out.printf("Unexpected table file format\n");
        }
    }

    @Override
    public Tuple readNextTuple(){
        if(readIndex < tupleList.size()){
            Tuple tuple = tupleList.get(readIndex++);
            return tuple;
        }
        readPage();
        if(tupleList.size() == 0){
            return null;
        }
        else{
            return readNextTuple();
        }
    }

}