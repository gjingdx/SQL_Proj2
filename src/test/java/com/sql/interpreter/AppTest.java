package com.sql.interpreter;

import org.junit.Test;
import junit.framework.Assert;
import java.io.*;
import java.util.Collections;
import java.util.HashSet;

import model.Tuple;
import util.Catalog;
import io.BinaryTupleReader;


/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void sqlResultMatchReadable() throws Exception{
        Handler.init(new String[0]);
        Handler.parseSql();
        for(int index = 1; index<=10; ++index){
            File outfile = new File(Catalog.getInstance().getOutputPath() + String.valueOf(index));
            File expectOutputfile = new File("Samples/samples/expected/" + "query" + String.valueOf(index) + "_humanreadable");
            BufferedReader br1 = new BufferedReader(new FileReader(outfile));
            BufferedReader br2 = new BufferedReader(new FileReader(expectOutputfile));
            String str1=br1.readLine(), str2=br2.readLine();
            while(str1!=null && str2!=null){
                Assert.assertEquals(str1, str2);
                str1=br1.readLine();
                str2=br2.readLine();
            }
            Assert.assertNull(str1);
            Assert.assertNull(str2);
            br1.close();
            br2.close();
        }
    }

    @Test
    public void sqlResultMatchBinary() throws Exception{
        Handler.init(new String[0]);
        Handler.parseSql();
        for(int index = 1; index<=12; ++index){
            String outfile = Catalog.getInstance().getOutputPath() + String.valueOf(index);
            String expectOutputfile = "Samples/samples/expected/" + "query" + String.valueOf(index);
            BinaryTupleReader r1 = new BinaryTupleReader(outfile);
            BinaryTupleReader r2  = new BinaryTupleReader(expectOutputfile);

            Tuple t1 = null, t2 = null;
            HashSet<String> outputBin = new HashSet<>();
            HashSet<String> expectedBin = new HashSet<>();
            while((t1 = r1.readNextTuple())!=null) {
                outputBin.add(t1.toString());
            }
            while ((t2=r2.readNextTuple())!=null) {
                Assert.assertTrue(index + t2.toString() + " lost", outputBin.contains(t2.toString()));
                expectedBin.add(t2.toString());
            }
            Assert.assertEquals("index: "+index, outputBin, expectedBin);
//            while((t1 = r1.readNextTuple())!=null && (t2=r2.readNextTuple())!=null){
//                Assert.assertEquals(t1.toString(), t2.toString());
//            }
        }
        for(int index = 13; index<=15; ++index){
            String outfile = Catalog.getInstance().getOutputPath() + String.valueOf(index);
            String expectOutputfile = "Samples/samples/expected/" + "query" + String.valueOf(index);
            BinaryTupleReader r1 = new BinaryTupleReader(outfile);
            BinaryTupleReader r2  = new BinaryTupleReader(expectOutputfile);
            
            Tuple t1 = null, t2 = null;
            while((t1 = r1.readNextTuple())!=null && (t2=r2.readNextTuple())!=null){
                Assert.assertEquals(""+ index, t1.toString(), t2.toString());
            }
        }
    }
}
