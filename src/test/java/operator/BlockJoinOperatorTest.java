package operator;

import model.Tuple;
import com.sql.interpreter.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.Constants.JoinMethod;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class BlockJoinOperatorTest {
    @Test
    public void testJoin() throws Exception {
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op1 = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator op2 = new PhysicalScanOperator(plainSelect, 1);
        PhysicalOperator opBlockJoin = new PhysicalBlockJoinOperator(op1, op2, plainSelect, 2);

        PhysicalOperator op3 = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator op4 = new PhysicalScanOperator(plainSelect, 1);
        PhysicalOperator opJoin = new PhysicalTupleJoinOperator(op3, op4, plainSelect);
        
        Tuple tuple;
        int count1 = 0, count2 = 0;
        while ((tuple = opBlockJoin.getNextTuple()) != null
                ) {
            count1 ++;
            assertEquals(tuple.getDataAt(0), tuple.getDataAt(3));
            //assertEquals(tuple.getDataAt(4), tuple.getDataAt(5));
        }
        while ((opJoin.getNextTuple()) != null
                ){
            count2 ++;
        }
        assertEquals("output less or greater than expected", count1, count2);

    }
}