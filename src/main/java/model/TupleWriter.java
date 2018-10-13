package model;

import java.nio.ByteBuffer;

public interface TupleWriter{
    boolean writeNextTuple(BufferStateWrapper bufferStateWrapper);
}