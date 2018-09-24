package operator;

import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class DuplicateEliminationOperatorTest {
    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);

        Operator sortOp = new SortOperator(op, plainSelect);
        Operator dupOp = new DuplicateEliminationOperator(sortOp);

        Tuple tuple = dupOp.getNextTuple();
        Tuple last = new Tuple(new long[0]);
        while(tuple != null){
            assertNotSame(last, tuple);
            last = tuple;
            tuple = dupOp.getNextTuple();
        }
    }

}