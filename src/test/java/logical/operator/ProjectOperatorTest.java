package logical.operator;

import junit.framework.Assert;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class ProjectOperatorTest {
    ProjectOperator op;

    public ProjectOperatorTest() throws Exception {
        String statement = "SELECT BT.E, BT.F FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        Operator scanOp = new ScanOperator(plainSelect, 0);
        Operator selectOp = new SelectOperator(scanOp, plainSelect);
        op = new ProjectOperator(selectOp, plainSelect);
    }

    @Test
    public void getChildrenTest() {
        Assert.assertEquals("Children: ", 1, op.getChildren().size());
    }

    @Test
    public void getSchema() {
        Map<String, Integer> expectedSchema = new HashMap<>();
        expectedSchema.put("BT.E", 0);
        expectedSchema.put("BT.F", 1);
        Assert.assertEquals(expectedSchema, op.getSchema());

    }
}