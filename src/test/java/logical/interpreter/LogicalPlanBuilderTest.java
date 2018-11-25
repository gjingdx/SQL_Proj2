package logical.interpreter;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LogicalPlanBuilderTest {
    @Test
    public void testExpression() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
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

        System.out.println("numTable: " + numTable);
        //List<Operator> scanOps = new ArrayList<>(); // list of scan Ops
        // list of select Ops or scan Op that pass to JoinOp
        List<Operator> selectOps = new ArrayList<>();

        UnionFind unionFind = new UnionFind();
        UnionFindExpressionVisitor ufVisitor = new UnionFindExpressionVisitor(unionFind);
        plainSelect.getWhere().accept(ufVisitor);
        unionFind = ufVisitor.getUnionFind();
        Set<String> attributes = unionFind.getAttributeSet();
        System.out.println(attributes.toString());
        for (int i = 0; i < numTable; i++) {
            Operator logicOp = new ScanOperator(plainSelect, i);
            //scanOps.add(scan);
            for (String attribute : attributes) {
                if (logicOp.getSchema().containsKey(attribute)) {
                    Constraints constraints = unionFind.find(attribute);
                    System.out.println("attribute: " + attribute + " lower: " + constraints.getLowerBound()
                        + " Upper: " + constraints.getUpperBound() + " Equality: " + constraints.getEquality());
                    logicOp = new SelectOperator(logicOp, attribute, unionFind.find(attribute), plainSelect);
                }
            }
            selectOps.add(logicOp);
        }

    }
}