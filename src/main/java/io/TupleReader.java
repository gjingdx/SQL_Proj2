package io;

import model.Tuple;

/**
 * An interface for tuplereader classes
 */
public interface TupleReader {
    /**
     * reset the tuple reader
     */
    void reset() throws Exception;

    /**
     * read the next tuple in the buffer page
     * if out of range, read next page
     *
     * @return
     */
    Tuple readNextTuple() throws Exception;

    /**
     * move back one position
     */
    void moveBack() throws Exception;

    /**
     * record the current position
     */
    void recordPosition();

    /**
     * revert to the record position
     */
    void revertToPosition() throws Exception;

    /**
     * move back to the position before ccertain tuple
     *
     * @param ith tuple
     */
    void reset(long i) throws Exception;

    /**
     * close the file if not closed
     */
    void close() throws Exception;
}