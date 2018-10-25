package operator;

import com.sql.interpreter.Handler;
import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.ScanOperator;
import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;


public class PhysicalSortOperatorTest {
    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator logScanOp = new ScanOperator(plainSelect, 0);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(logScanOp);
        SortOperator logSortOp = new SortOperator(logScanOp, plainSelect);
        PhysicalOperator child = physPB.getPhysOpChildren().pop();
        PhysicalOperator physSortOp = new PhysicalMemorySortOperator(logSortOp, child);
        Tuple tuple = physSortOp.getNextTuple();
        long last = Long.MIN_VALUE;
        while (tuple != null) {
            long cur = tuple.getDataAt(2);
            assertEquals(true, last <= cur);
            tuple = physSortOp.getNextTuple();
        }
    }

    @Test
    public void dump() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        Handler.init(new String[0]);
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator sortOp = new PhysicalMemorySortOperator(op, plainSelect);
        sortOp.dump(0);
    }
}