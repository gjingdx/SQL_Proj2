package operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.DuplicateEliminationOperator;
import logical.operator.Operator;
import logical.operator.ScanOperator;
import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertNotSame;

public class PhysicalDuplicateEliminationOperatorTest {
    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator logScanOp = new ScanOperator(plainSelect, 0);
        SortOperator logSortOp = new SortOperator(logScanOp, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(logSortOp);

        DuplicateEliminationOperator logDupOp = new DuplicateEliminationOperator(logSortOp);
        PhysicalOperator child = physPB.getPhysOpChildren().pop();
        PhysicalOperator physDupOp = new PhysicalDuplicateEliminationOperator(logDupOp, child);

        Tuple tuple = physDupOp.getNextTuple();
        Tuple last = new Tuple(new int[0]);
        while (tuple != null) {
            assertNotSame(last, tuple);
            //System.out.println(tuple);
            last = tuple;
            tuple = physDupOp.getNextTuple();
        }
    }

}