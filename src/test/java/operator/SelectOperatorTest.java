package operator;

import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;

public class SelectOperatorTest {

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 2;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator scanOp = new ScanOperator(plainSelect, 0);

        Operator selectOp = new SelectOperator(scanOp, plainSelect);

        Tuple tuple = selectOp.getNextTuple();
        while(tuple != null){
            assertEquals(2, tuple.getDataAt(1));
            System.out.println(tuple);
            tuple = selectOp.getNextTuple();
        }
    }

    @Test
    public void reset() {
    }

    @Test
    public void dump() {
    }

    @Test
    public void getSchema() {
    }
}