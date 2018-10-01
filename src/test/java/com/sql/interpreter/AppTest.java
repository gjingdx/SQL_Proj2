package com.sql.interpreter;

import org.junit.Test;

import junit.framework.Assert;

import java.io.*;

import util.Catalog;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void sqlResultMatch() throws Exception{
        Handler.init();
        Handler.parseSql();
        for(int index = 1; index<=8; ++index){
            File outfile = new File(Catalog.getInstance().getOutputPath() + String.valueOf(index));
            File expectOutputfile = new File("Samples/samples/expected_output/" + "query" + String.valueOf(index));
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
}
