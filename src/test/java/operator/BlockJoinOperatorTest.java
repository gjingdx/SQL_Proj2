package operator;

import model.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class BlockJoinOperatorTest{
    @Test    
    public void testJoin() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op1 = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator op2 = new PhysicalScanOperator(plainSelect, 1);
        PhysicalOperator opBlockJoin = new PhysicalBlockJoinOperator(op1, op2, plainSelect, 2);

        PhysicalOperator op3 = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator op4 = new PhysicalScanOperator(plainSelect, 1);
        PhysicalOperator opJoin = new PhysicalJoinOperator(op3, op4, plainSelect);
        Tuple tuple, tuple2;
        while((tuple = opBlockJoin.getNextTuple()) !=null 
            )
        {
            tuple2 = opJoin.getNextTuple();
            assertEquals(tuple.toString(), tuple2.toString());
        }
    }
}