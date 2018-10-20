package io;

import model.Tuple;

public interface TupleReader{
    /**
     * reset the tuple reader
     */
    void init();
    
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
}