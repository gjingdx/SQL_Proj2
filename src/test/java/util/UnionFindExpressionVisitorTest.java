package util;

import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import operator.PhysicalScanOperator;
import org.junit.Test;
import util.unionfind.UnionFind;

import java.io.StringReader;

import static org.junit.Assert.*;

public class UnionFindExpressionVisitorTest {

    static UnionFindExpressionVisitor visitor;
    @Test
    public void getUnionFind() throws Exception {
        String statement = "SELECT * FROM Sailors S, Reserve R WHERE S.A > 3 AND S.A = R.E AND R.E < 10;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Expression whereEx = plainSelect.getWhere();
        visitor = new UnionFindExpressionVisitor(new UnionFind());
        whereEx.accept(visitor);
        UnionFind unionFind = visitor.getUnionFind();
        assertEquals(unionFind.connected("S.A", "R.E"), true);
        assertEquals(unionFind.find("S.A").getUpperBound(), Integer.valueOf(9));
    }

}