package model;

import java.io.IOException;

public interface TupleReader{
    void init();
    Tuple readNextTuple();
    void readPage();
}