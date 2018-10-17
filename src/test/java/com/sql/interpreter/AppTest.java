package com.sql.interpreter;

import org.junit.Test;
import junit.framework.Assert;
import java.io.*;

import model.Tuple;
import util.Catalog;
import util.TableReader;

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
        for(int index = 1; index<=10; ++index){
            File outfile = new File(Catalog.getInstance().getOutputPath() + String.valueOf(index));
            File expectOutputfile = new File("Samples/samples/expected/" + "query" + String.valueOf(index));
            TableReader r1 = new TableReader(outfile);
            TableReader r2  = new TableReader(expectOutputfile);
            
            Tuple t1 = null, t2 = null;
            while((t1 = r1.readNextTuple())!=null && (t2=r2.readNextTuple())!=null){
                Assert.assertEquals(t1.toString(), t2.toString());
            }
            Assert.assertNull(t1);
            Assert.assertNull(t2);
        }
    }
}
