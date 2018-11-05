package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;


public class IndexScanExpressionVisitorTest {

    @Test
    public void testExtractExpression() throws Exception {
        Catalog.getInstance().setAliases("Sailors");
        Catalog.getInstance().setAliases("Boats");
        Catalog.getInstance().setAliases("Reserves");
        Catalog.getInstance().setAliases("Sailors As S");

        String statement = "SELECT * FROM Sailors as S, Reserves, Boats WHERE S.A = Reserves.G And S.A < 50 And S.A = S.B And S.A >= 10;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Expression expr = plainSelect.getWhere();

        IndexScanExpressionVisitor joinExpress = new IndexScanExpressionVisitor("Sailors", "A");
        expr.accept(joinExpress);
        int highKey = joinExpress.getHighKey();
        int lowKey = joinExpress.getLowKey();

        Assert.assertEquals(49, highKey);
        Assert.assertEquals(10, lowKey);
    }

}