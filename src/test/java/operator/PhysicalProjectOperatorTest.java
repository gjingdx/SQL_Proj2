package operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.Operator;
import logical.operator.ProjectOperator;
import logical.operator.ScanOperator;
import logical.operator.SelectOperator;
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
        Operator scanOp = new ScanOperator(plainSelect, 0);
        SelectOperator selectOp = new SelectOperator(scanOp, plainSelect);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(selectOp);
        ProjectOperator projectOp = new ProjectOperator(selectOp, plainSelect);

        PhysicalOperator physProjOp = new PhysicalProjectOperator(projectOp, physPB.getPhysOpChildren());

        Tuple tuple = physProjOp.getNextTuple();
        while (tuple != null) {
            assertEquals(9, tuple.getDataAt(0));
            System.out.println(tuple.getDataAt(0));
            tuple = physProjOp.getNextTuple();
        }
    }

    @Test
    public void reset() {
    }

    @Test
    public void dump() {
    }

    @Test
    public void getSchema() throws Exception {
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator scanOp = new PhysicalScanOperator(plainSelect, 0);
        PhysicalOperator selectOp = new PhysicalSelectOperator(scanOp, plainSelect);
        PhysicalOperator projectOp = new PhysicalProjectOperator(selectOp, plainSelect);
        System.out.println(projectOp.getSchema());
    }
}