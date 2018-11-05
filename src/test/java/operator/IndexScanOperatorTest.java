package operator;

import com.sql.interpreter.Handler;
import com.sql.interpreter.PhysicalPlanBuilder;
import util.*;

import logical.operator.*;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class IndexScanOperatorTest {
    public IndexScanOperatorTest() {
        try{
            Handler.parserIndexInfo();
            Catalog.getInstance().setIndexScan(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E > 9 and BT.E <= 20;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator logScanOp = new ScanOperator(plainSelect, 0);
        Operator selectOp = new SelectOperator(logScanOp, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        selectOp.accept(physPB);

        PhysicalOperator operator = physPB.getPhysOpChildren().pop();

        Tuple tuple;
        while ((tuple = operator.getNextTuple()) != null) {
            Assert.assertTrue(9 < tuple.getDataAt(1) && 20 >= tuple.getDataAt(1));
            System.out.println(tuple);
            tuple = operator.getNextTuple();
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