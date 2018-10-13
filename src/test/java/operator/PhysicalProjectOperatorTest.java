package operator;

import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class PhysicalProjectOperatorTest {

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT BT.E, BT.F FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator scanOp = new ScanOperator(plainSelect, 0);
        PhysicalOperator selectOp = new SelectOperator(scanOp, plainSelect);
        PhysicalOperator projectOp = new PhysicalProjectOperator(selectOp, plainSelect);
        Tuple tuple = projectOp.getNextTuple();
        while(tuple != null){
            assertEquals(9, tuple.getDataAt(1));
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
    public void getSchema() throws Exception{
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator scanOp = new ScanOperator(plainSelect, 0);
        PhysicalOperator selectOp = new SelectOperator(scanOp, plainSelect);
        PhysicalOperator projectOp = new PhysicalProjectOperator(selectOp, plainSelect);
        System.out.println(projectOp.getSchema());
    }
}