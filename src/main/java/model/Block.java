package model;

import util.Constants;

public class Block{
    private Tuple[] outerBlock;
    private int blockSize;
    private int setIndex;
    private int readIndex;
    
    public Block(int blockSize, int tupleSize){
        this.blockSize = blockSize;
        outerBlock = new Tuple[this.blockSize * (Constants.PAGE_SIZE / (tupleSize * Constants.INT_SIZE))];
        initIndex();
        
    }

    public void resetOuterBlockSize(int tupleSize){
        clear();
        outerBlock = new Tuple[this.blockSize * (Constants.PAGE_SIZE / (tupleSize * Constants.INT_SIZE))];
    }

    public int getMaximumSize(){
        return outerBlock.length;
    }

    public void clearData(){
        for(int i = 0; i<getMaximumSize(); ++i){
            outerBlock[i] = null;
        }
        initIndex();
    }

    public void clear(){
        outerBlock = null;
        initIndex();
    }

    public Boolean setNextTuple(Tuple tuple){
        if(setIndex < getMaximumSize()){
            outerBlock[setIndex++] = tuple;   
            if(setIndex == getMaximumSize()){
                setIndex = 0;
                return false;
            }
            else return true;
        }
        else{
            setIndex = 0;
            return false;
        }

    }

    public Tuple readNextTuple(){
        Tuple tuple;
        if(readIndex < getMaximumSize()){
            tuple = outerBlock[readIndex++];
        }
        else{
            readIndex = 0;
            tuple = null;
        }
        return tuple;
    }

    public boolean isAllNull(){
        if(outerBlock.length > 0 && outerBlock[0] != null){
            return false;
        }
        return true;
    }

    private void initIndex(){
        this.setIndex = 0;
        this.readIndex = 0;
    }

}