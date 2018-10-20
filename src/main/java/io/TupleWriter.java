package io;
import model.Tuple;

public interface TupleWriter{
    void writeNextTuple(Tuple tuple);
    void finish();
}