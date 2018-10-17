package operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import logical.operator.JoinOperator;
import logical.operator.ScanOperator;
import model.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class PhysicalJoinOperatorTest {
    @Test
    public void testJoin() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator op1 = new ScanOperator(plainSelect, 0);
        ScanOperator op2 = new ScanOperator(plainSelect, 1);
        JoinOperator logJoinOp = new JoinOperator(op1, op2, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(op1);
        physPB.visit(op2);
        PhysicalJoinOperator physJoinOp = new PhysicalTupleJoinOperator(logJoinOp, physPB.getPhysOpChildren());
        Tuple tuple;
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList(
            "1,200,50,1,101",
            "1,200,50,1,102",
            "1,200,50,1,103",
            "2,200,200,2,101",
            "3,100,105,3,102",
            "4,100,50,4,104"
        ));
        ArrayList<String> outputStrings = new ArrayList<>();
        while((tuple = physJoinOp.getNextTuple()) !=null){
            outputStrings.add(tuple.toString());
        }
        System.out.println(outputStrings);
        //ssertEquals(expectedResult, outputStrings);
    }
}