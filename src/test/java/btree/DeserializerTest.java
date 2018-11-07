package btree;

import io.*;
import model.Tuple;
import org.junit.Test;
import util.Catalog;

import java.io.File;
import junit.framework.Assert;

public class DeserializerTest {

    @Test
    public void deserializeSailor() throws Exception {
        int lowKey = 1;
        int highKey = 50;
        String inputFile = "Samples/samples-2/expected_indexes/Boats.E";
        BinaryTupleReader sailorReader = new BinaryTupleReader("Samples/samples-2/input/db/data/Boats");
        Deserializer deser = new Deserializer(new File(inputFile), lowKey, highKey);
        Rid rid;
        while ((rid = deser.getNextRid()) != null) {
            sailorReader.reset(rid.getPageId(), rid.getTupleId());
            Tuple tuple = sailorReader.readNextTuple();
            Assert.assertTrue(tuple.getDataAt(1) >= lowKey && tuple.getDataAt(1) <= highKey);
        }
    }
}