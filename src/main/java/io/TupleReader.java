package io;

import model.Tuple;

public interface TupleReader {
    /**
     * reset the tuple reader
     */
    void reset();

    /**
     * read the next tuple in the buffer page
     * if out of range, read next page
     *
     * @return
     */
    Tuple readNextTuple();

    /**
     * move back one position
     */
    void moveBack();

    /**
     * record the current position
     */
    void recordPosition();

    /**
     * revert to the record position
     */
    void revertToPosition();

    /**
     * move back to the position before ccertain tuple
     * @param ith tuple
     */
    void reset(long i);
}