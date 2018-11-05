package btree;

import io.BinaryTupleReader;
import junit.framework.Assert;
import model.Tuple;
import org.junit.Test;
import util.Catalog;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BPlusTreeTest {

    @Test
    public void bPlusTreeConstructorTest() throws Exception {
        String relationName = "Samples/samples-2/input/db/data/Boats";
        System.out.println(relationName);

        Map<String, Integer> schema = new HashMap<>();
        schema.put("D", 0);
        schema.put("E", 1);
        schema.put("F", 2);
        int attribute = schema.get("E");
        String indexFile = "Samples/samples-2/output/index";

        BPlusTree bPlusTree = new BPlusTree(relationName, attribute, 10, indexFile);

        int lowKey = 1;
        int highKey = 50;

        BinaryTupleReader sailorReader = new BinaryTupleReader(Catalog.getInstance().getDataPath("Boats"));

        Deserializer deser = new Deserializer(new File(indexFile), lowKey, highKey);
        Rid rid;
        Set<String> ourOut = new HashSet<>();
        while ((rid = deser.getNextRid()) != null) {
            sailorReader.reset(rid.getPageId(), rid.getTupleId());
            Tuple tuple = sailorReader.readNextTuple();
            ourOut.add(tuple.toString());
            System.out.println(tuple.toString());
            //Assert.assertTrue(tuple.getDataAt(1) >= lowKey && tuple.getDataAt(1) < highKey);
        }

        String expectedIndex = "Samples/samples-2/expected_indexes/Boats.E";
        Deserializer expectedDeser = new Deserializer(new File(expectedIndex), lowKey, highKey);
        Set<String> expected = new HashSet<>();
        while ((rid = expectedDeser.getNextRid()) != null) {
            sailorReader.reset(rid.getPageId(), rid.getTupleId());
            Tuple tuple = sailorReader.readNextTuple();
            expected.add(tuple.toString());
            //Assert.assertTrue(tuple.getDataAt(1) >= lowKey && tuple.getDataAt(1) < highKey);
        }
        Assert.assertEquals(expected, ourOut);
    }

}