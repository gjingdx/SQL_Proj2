package operator;

import com.sql.interpreter.Handler;
import junit.framework.Assert;
import org.junit.Test;
import util.Catalog;

import java.io.File;
import java.io.FileInputStream;


public class PhysicalOperatorTest {
    @Test
    public void dump() throws Exception {
        Handler.init(new String[0]);
        Handler.parseSql();

        for (int index = 1; index <= 15; ++index) {
            File outfile = new File(Catalog.getInstance().getOutputPath() + index);
            File expectOutputfile = new File("Samples/samples/expected/" + "query" + index);
            FileInputStream outputStream, expectedStream;
            byte[] bytesArrayOutput, bytesArrayExpected;
            bytesArrayOutput = new byte[(int) outfile.length()];
            bytesArrayExpected = new byte[(int) expectOutputfile.length()];

            //read file into bytes[]
            outputStream = new FileInputStream(outfile);
            expectedStream = new FileInputStream(expectOutputfile);
            outputStream.read(bytesArrayOutput);
            expectedStream.read(bytesArrayExpected);
            Assert.assertEquals(bytesArrayExpected.length, bytesArrayOutput.length);
            for (int i = 0; i < bytesArrayExpected.length; i++) {
                Assert.assertEquals(bytesArrayExpected[i], bytesArrayOutput[i]);
            }
            outputStream.close();
            expectedStream.close();
        }
    }

    @Test
    public void writeNextTuple() throws Exception {
    }

}