package operator;

import model.Tuple;
import org.junit.Test;
import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class JoinOperatorTest{
    @Test
    public void testJoin() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op1 = new ScanOperator(plainSelect, 0);
        Operator op2 = new ScanOperator(plainSelect, 1);
        Operator opJoin = new JoinOperator(op1, op2, plainSelect);
        Tuple tuple;
        while((tuple = opJoin.getNextTuple()) !=null){
            System.out.println(tuple.toString());
        }
    }
}