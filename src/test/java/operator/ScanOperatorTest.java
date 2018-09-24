package operator;

import org.junit.Test;
import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import model.Tuple;

public class ScanOperatorTest{

    @Test
    public void testReadFile() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);
        Tuple tuple;
        while((tuple = op.getNextTuple()) != null){
            System.out.println(tuple.toString());
        }
    }
}