package io;

import model.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Implementation of TupleWriter, to deal with human readable files
 *
 * @author Yufu Mo ym445
 */
public class ReadableTupleWriter implements TupleWriter {
    private StringBuilder sb;
    private BufferedWriter output;

    public ReadableTupleWriter(String path, int tupleSize) {
        this.sb = new StringBuilder();
        try {
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeNextTuple(Tuple tuple) {
        if (tuple == null) {
            try {
                output.write(sb.toString());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sb.append(tuple.toString());
            sb.append("\n");
        }
    }

    @Override
    public void finish() {
        try {
            output.write(sb.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
