package logical.operator;

import org.junit.Test;
import junit.framework.Assert;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class ScanOperatorTest{

    @Test
    public void testReadFile() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator op = new ScanOperator(plainSelect, 0);
        Assert.assertNull(op.getChildren());

        Map<String, Integer> expectedSchema = new HashMap<>();
        expectedSchema.put("Sailors.A", 0);
        expectedSchema.put("Sailors.B", 1);
        expectedSchema.put("Sailors.C", 2);
        Assert.assertEquals(op.getSchema(), expectedSchema);
    }
}