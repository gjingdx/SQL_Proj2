package logical.operator;

import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import junit.framework.Assert;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

public class JoinOperatorTest {
    JoinOperator op;
    public JoinOperatorTest() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
        parse(new StringReader(statement))).getSelectBody();
        Operator op1 = new ScanOperator(plainSelect, 0);
        Operator op2 = new ScanOperator(plainSelect, 1);
        op = new JoinOperator(op1, op2, plainSelect);
    }

    @Test
    public void getChildrenTest()  {
        Assert.assertEquals("Children: ", 2, op.getChildren().length);
    }

    @Test
    public void getSchema() {
        Map<String, Integer> expectedSchema = new HashMap<>();
        expectedSchema.put("Sailors.A", 0);
        expectedSchema.put("Sailors.B", 1);
        expectedSchema.put("Sailors.C", 2);
        expectedSchema.put("Reserves.G", 3);
        expectedSchema.put("Reserves.H", 4);
        Assert.assertEquals(expectedSchema, op.getSchema());

    }

    @Test
    public void getJoinCondition(){
        Assert.assertEquals("Join Condition", "Sailors.A = Reserves.G", op.getJoinCondition().toString());
    }
}