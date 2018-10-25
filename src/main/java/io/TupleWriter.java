package io;

import model.Tuple;


/**
 * TupleWriter interface for different tuplewriter classes
 * @author Yufu Mo
 */
public interface TupleWriter {

    /**
     * write tuple to files
     *
     * @param tuple
     */
    void writeNextTuple(Tuple tuple);


    /**
     * dealing with IO when there is no more tuple to write
     */
    void finish();
}