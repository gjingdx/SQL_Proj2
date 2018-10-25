package operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.ScanOperator;
import logical.operator.SelectOperator;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class PhysicalSelectOperatorTest {

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator logScanOp = new ScanOperator(plainSelect, 0);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        logScanOp.accept(physPB);
        //PhysicalScanOperator physScanOp = new PhysicalScanOperator(logScanOp);
        SelectOperator logSelectOp = new SelectOperator(logScanOp, plainSelect);
        PhysicalOperator child = physPB.getPhysOpChildren().pop();
        PhysicalSelectOperator physSelectOp = new PhysicalSelectOperator(logSelectOp, child);

        Tuple tuple = physSelectOp.getNextTuple();
        while (tuple != null) {
            assertEquals(9, tuple.getDataAt(1));
            System.out.println(tuple);
            tuple = physSelectOp.getNextTuple();
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