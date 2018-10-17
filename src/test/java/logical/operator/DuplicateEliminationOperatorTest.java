package logical.operator;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;


public class DuplicateEliminationOperatorTest {
    @Test
    public void getChildren() throws Exception {
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);

        Operator sortOp = new SortOperator(op, plainSelect);
        DuplicateEliminationOperator dupOp = new DuplicateEliminationOperator(sortOp);

        Assert.assertEquals(1, dupOp.getChildren().length);
    }

}