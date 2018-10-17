package model;

public interface TupleWriter{
    boolean writeNextTuple(BufferStateWrapper bufferStateWrapper);
}