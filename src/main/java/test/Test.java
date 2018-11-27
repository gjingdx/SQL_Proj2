package test;

import com.sql.interpreter.Handler;
import io.BinaryTupleReader;
import model.Tuple;

import java.util.HashSet;
import java.util.Set;

class Test {
    public static void main(String[] args) {
        try{
            args = new String[2];
            args[0] = "../project5/expected_output/query";
            args[1] = "../project5/output/query";
            //Handler.init(args);
            cmpResult(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // path : xxx/query
    private static void cmpResult(String expectedPath, String outputPath) throws Exception{
        for (int index = 1; index <= 20; ++index) {
            String outfile = outputPath + String.valueOf(index);
            String expectOutputfile = expectedPath + String.valueOf(index);
            BinaryTupleReader r1 = new BinaryTupleReader(outfile);
            BinaryTupleReader r2 = new BinaryTupleReader(expectOutputfile);
            Tuple t1 = null, t2 = null;
            Set<String> outputBin = new HashSet<>();
            Set<String> expectedBin = new HashSet<>();

            boolean flag = true;

            while ((t1 = r1.readNextTuple()) != null) {
                outputBin.add(t1.toString());
            }
            while ((t2 = r2.readNextTuple()) != null) {
                if (!outputBin.contains(t2.toString())) {
                    flag = false;
                    System.out.println("Query " + index + " fail");
                    break;
                }
                expectedBin.add(t2.toString());
            }
            if (!flag){ continue; }
            if(outputBin.equals(expectedBin)) {
                System.out.println("Query " + index + " pass");
            }
            else {
                System.out.println("Query " + index + " fail");
            }
        }
    }
}