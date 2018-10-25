package logical.operator;

import junit.framework.Assert;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class SelectOperatorTest {
    SelectOperator selectOp;

    public SelectOperatorTest() throws Exception {
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 2;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator scanOp = new ScanOperator(plainSelect, 0);
        selectOp = new SelectOperator(scanOp, plainSelect);
    }

    @Test
    public void getExpressionTest() {
        Assert.assertEquals("Expression: ", "BT.E = 2", selectOp.getExpression().toString());
    }

    @Test
    public void getChildrenTest() {
        Assert.assertEquals("Children: ", 1, selectOp.getChildren().length);
    }

    @Test
    public void getSchema() {
        Map<String, Integer> expectedSchema = new HashMap<>();
        expectedSchema.put("BT.D", 0);
        expectedSchema.put("BT.E", 1);
        expectedSchema.put("BT.F", 2);
        Assert.assertEquals(expectedSchema, selectOp.getSchema());

    }
}