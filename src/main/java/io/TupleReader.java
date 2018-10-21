package io;

import model.Tuple;

public interface TupleReader{
    /**
     * reset the tuple reader
     */
    void reset();
    
    /**
     * read the next tuple in the buffer page
     * if out of range, read next page
     * @return
     */
    Tuple readNextTuple();

    /**
     * read the next page
     */
    void readPage();

    /**
     * move back one position
     */
    void moveBack();

    /**
     * 
     */
    void recordPosition();

    void moveToPosition();
}