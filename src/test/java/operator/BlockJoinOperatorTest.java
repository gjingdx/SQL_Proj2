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
        Operator op1 = new ScanOperator(plainSelect, 0);
        Operator op2 = new ScanOperator(plainSelect, 1);
        Operator opBlockJoin = new BlockJoinOperator(op1, op2, plainSelect, 2);

        Operator op3 = new ScanOperator(plainSelect, 0);
        Operator op4 = new ScanOperator(plainSelect, 1);
        Operator opJoin = new JoinOperator(op3, op4, plainSelect);
        Tuple tuple, tuple2;
        while((tuple = opBlockJoin.getNextTuple()) !=null 
            )
        {
            tuple2 = opJoin.getNextTuple();
            assertEquals(tuple.toString(), tuple2.toString());
        }
    }
}