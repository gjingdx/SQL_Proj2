package operator;

import com.sql.interpreter.Handler;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import util.Catalog;
import util.Constants.JoinMethod;
import util.Constants.SortMethod;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;


public class PhysicalSMJTest {

    public PhysicalSMJTest() throws Exception {
        Handler.init(new String[0]);
        Catalog.getInstance().setSortBlockSize(50);
        Catalog.getInstance().setSortMethod(SortMethod.EXTERNAL);
        Catalog.getInstance().setJoinMethod(JoinMethod.SMJ);
    }

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Reserves R, Sailors S WHERE R.G = S.A;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op = Handler.constructPhysicalQueryPlan(plainSelect);

        Tuple tuple = op.getNextTuple();
        long last = Long.MIN_VALUE;
        while (tuple != null) {
            long cur = tuple.getDataAt(op.getSchema().get("R.G"));
            assertEquals(true, last <= cur);
            assertEquals(tuple.getDataAt(op.getSchema().get("R.G")), tuple.getDataAt(op.getSchema().get("S.A")));
            last = cur;
            tuple = op.getNextTuple();
        }
    }
}