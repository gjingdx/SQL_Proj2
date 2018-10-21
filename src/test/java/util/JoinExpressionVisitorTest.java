package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public class JoinExpressionVisitorTest {

    @Test
    public void testExtractExpression() throws Exception {
        Map<String, Integer> schemaMap = new HashMap<>();
        schemaMap.put("Sailors.A", 0);
        schemaMap.put("Sailors.B", 1);
        schemaMap.put("Sailors.C", 2);

        schemaMap.put("Reserves.G", 3);
        schemaMap.put("Reserves.H", 4);

        Catalog.getInstance().setCurrentSchema(schemaMap);
        Catalog.getInstance().setAliases("Sailors");
        Catalog.getInstance().setAliases("Boats");
        Catalog.getInstance().setAliases("Reserves");

        String statement = "SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Boats.D And Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Expression expr = plainSelect.getWhere();

        JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(schemaMap);
        expr.accept(joinExpress);
        Expression output = joinExpress.getExpression();
        String expectedExpressioin = "Sailors.A = Reserves.G";
        Assert.assertEquals(expectedExpressioin, output.toString());
    }

}