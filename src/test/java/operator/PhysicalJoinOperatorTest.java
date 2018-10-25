package operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.JoinOperator;
import logical.operator.ScanOperator;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

public class PhysicalJoinOperatorTest {
    @Test
    public void testJoin() throws Exception {
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator op1 = new ScanOperator(plainSelect, 0);
        ScanOperator op2 = new ScanOperator(plainSelect, 1);
        JoinOperator logJoinOp = new JoinOperator(op1, op2, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(op1);
        physPB.visit(op2);
        PhysicalOperator rightChild = physPB.getPhysOpChildren().pop();
        PhysicalOperator leftChild = physPB.getPhysOpChildren().pop();
        PhysicalJoinOperator physJoinOp = new PhysicalTupleJoinOperator(logJoinOp, leftChild, rightChild);
        Tuple tuple;
        ArrayList<String> outputStrings = new ArrayList<>();
        while ((tuple = physJoinOp.getNextTuple()) != null) {
            outputStrings.add(tuple.toString());
        }
        System.out.println(outputStrings);
        //ssertEquals(expectedResult, outputStrings);
    }
}