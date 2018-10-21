package model;

import util.Constants;

/**
 * Block only serve Block Nested Loop Join
 * store the data in a tuple array
 */
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

    /**
     * resize a clean block
     * @param tupleSize
     */
    public void resetOuterBlockSize(int tupleSize){
        clear();
        outerBlock = new Tuple[this.blockSize * (Constants.PAGE_SIZE / (tupleSize * Constants.INT_SIZE))];
    }

    /**
     * get the maximum count of tuple in the block
     * @return the size of outer block
     */
    public int getMaximumSize(){
        return outerBlock.length;
    }

    /**
     * reset all data in block null
     */
    public void clearData(){
        for(int i = 0; i<getMaximumSize(); ++i){
            outerBlock[i] = null;
        }
        initIndex();
    }

    /**
     * set block to null
     */
    public void clear(){
        outerBlock = null;
        initIndex();
    }

    /**
     * set next position tuple
     * @param tuple
     * @return is succeed
     */
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

    /**
     * read the next tuple
     * @return tuple, null if out of index
     */
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

    /**
     * judge if the block is all null
     * @return
     */
    public boolean isAllNull(){
        if(outerBlock.length > 0 && outerBlock[0] != null){
            return false;
        }
        return true;
    }

    /**
     * reset the read and set index
     */
    public void reset(){
        initIndex();
    }

    private void initIndex(){
        this.setIndex = 0;
        this.readIndex = 0;
    }

}