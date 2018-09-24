package operator;

import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;


public class SortOperatorTest {
    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);

        Operator sortOp = new SortOperator(op, plainSelect);
        Tuple tuple = sortOp.getNextTuple();
        long last = Long.MIN_VALUE;
        while(tuple != null){
            long cur = tuple.getDataAt(2);
            assertEquals(true, last <= cur);
            tuple = sortOp.getNextTuple();
        }

    }

    @Test
    public void dump() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);
        Operator sortOp = new SortOperator(op, plainSelect);
        sortOp.dump(0);
    }
}