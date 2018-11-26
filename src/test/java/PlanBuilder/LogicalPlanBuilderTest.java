package PlanBuilder;

import PlanBuilder.JoinOrder;
import com.sql.interpreter.Handler;
import logical.operator.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import util.UnionFindExpressionVisitor;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.io.StringReader;
import java.util.*;

import static PlanBuilder.LogicalPlanBuilder.constructLogicalPlanTree;

public class LogicalPlanBuilderTest {
    @Test
    public void testExpression() throws Exception{
        Handler.init(new String[0]);
        String statement = "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G and Sailors.A < 100;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();



        // get number of tables used in total which is num of scanOp
        int numTable;
        if (plainSelect.getJoins() == null) {
            numTable = 1;
        } else {
            numTable = 1 + plainSelect.getJoins().size();
        }

        //List<Operator> scanOps = new ArrayList<>(); // list of scan Ops
        // list of select Ops or scan Op that pass to JoinOp
        List<Operator> selectOps = new ArrayList<>();

        UnionFind unionFind = new UnionFind();
        UnionFindExpressionVisitor ufVisitor = new UnionFindExpressionVisitor(unionFind);
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(ufVisitor);
            unionFind = ufVisitor.getUnionFind();
        }
        Set<String> attributes = unionFind.getAttributeSet();
        for (int i = 0; i < numTable; i++) {
            Operator logicOp = new ScanOperator(plainSelect, i);
            //scanOps.add(scan);
            Map<String, Constraints> constraints = new HashMap<>();
            for (String attribute : attributes) {
                if (logicOp.getSchema().containsKey(attribute) && !unionFind.find(attribute).isNull()) {
                    constraints.put(attribute, unionFind.find(attribute));
                }
            }
            if (!constraints.isEmpty()) {
                logicOp = new SelectOperator(logicOp, constraints, plainSelect);
            } else if (numTable == 1 && plainSelect.getWhere() != null) {
                logicOp = new SelectOperator(logicOp, constraints, plainSelect);
            }
            selectOps.add(logicOp);
        }

        Operator logicOp;
        if (selectOps.size() > 1) {
            logicOp = new JoinOperator(selectOps, plainSelect, true);
        } else {
            logicOp = selectOps.get(0);
        }
        if (plainSelect.getSelectItems() != null
                && plainSelect.getSelectItems().size() > 0
                && plainSelect.getSelectItems().get(0).toString() != "*")
            logicOp = new ProjectOperator(logicOp, plainSelect);
        if (plainSelect.getDistinct() != null) {
            logicOp = new SortOperator(logicOp, plainSelect);
            logicOp = new DuplicateEliminationOperator(logicOp);
        } else {
            if (plainSelect.getOrderByElements() != null)
                logicOp = new SortOperator(logicOp, plainSelect);
        }
    }

    @Test
    public void testJoinOrder() throws Exception{
        Handler.init(new String[0]);
        String statement = "SELECT * FROM Sailors S, Reserves R, Boats B Where B.D = R.H and S.A = R.G and S.A < 10 and B.D > 60;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();



        // get number of tables used in total which is num of scanOp
        Operator op = constructLogicalPlanTree(plainSelect);
        int a = 1;
    }
}