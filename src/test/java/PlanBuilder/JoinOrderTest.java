package PlanBuilder;

import PlanBuilder.JoinOrder;
import logical.operator.Operator;
import logical.operator.ScanOperator;
import logical.operator.SelectOperator;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import util.UnionFindExpressionVisitor;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.io.StringReader;
import java.util.*;


public class JoinOrderTest {
    @Test
    public void testJoinOrder() throws Exception {
        String statement = "SELECT * FROM Sailors S, Reserves R, Boats B Where B.D = R.H and S.A = R.G and S.A < 10 and B.D > 60;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();

        // get number of tables used in total which is num of scanOp
        Operator op = LogicalPlanBuilder.constructLogicalPlanTree(plainSelect);

    }
}