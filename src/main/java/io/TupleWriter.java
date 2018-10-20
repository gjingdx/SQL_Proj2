package io;

import model.BufferStateWrapper;

public interface TupleWriter{
    boolean writeNextTuple(BufferStateWrapper bufferStateWrapper);
}