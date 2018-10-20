package operator;

import com.sql.interpreter.Handler;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.Constants.SortMethod;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;


public class PhysicalExternalSortOperatorTest {

    public PhysicalExternalSortOperatorTest(){
        Catalog.getInstance().setSortBlockSize(50);
        Catalog.getInstance().setSortMethod(SortMethod.EXTERNAL);
    }
    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats BT, Sailors S ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator physSortOp = Handler.constructPhysicalQueryPlan(plainSelect);
        
        Tuple tuple = physSortOp.getNextTuple();
        long last = Long.MIN_VALUE;
        while(tuple != null){
            long cur = tuple.getDataAt(2);
            assertEquals(true, last <= cur);
            tuple = physSortOp.getNextTuple();
        }
    }
}