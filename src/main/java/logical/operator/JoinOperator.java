package logical.operator;

import PlanBuilder.JoinOrder;
import PlanBuilder.LogicalOperatorVisitor;
import PlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;
import util.ExpressionBuilder;
import util.JoinConditionExpressionVisitor;
import util.JoinExpressionVisitor;
import util.unionfind.UnionFind;

import java.util.*;

/**
 * PhysicalJoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class JoinOperator extends Operator {
    private List<Operator> prevOps;
    private Map<String, Integer> schema;
    Expression joinCondition;
    private PlainSelect plainSelect;

    public JoinOperator(List<Operator> prevOps, PlainSelect plainSelect, boolean orderFlag) {
        this.prevOps = prevOps;
        this.plainSelect = plainSelect;
        if (orderFlag) { sortPrevOps(); }

        this.schema = new HashMap<>();

        int schemaIndexBase = 0;
        for (Operator op : this.prevOps) {
            for (Map.Entry<String, Integer> entry : op.getSchema().entrySet()) {
                schema.put(entry.getKey(), entry.getValue() + schemaIndexBase);
            }
            schemaIndexBase += op.getSchema().size();
        }
        Catalog.getInstance().setCurrentSchema(schema);

        // get joinCondition
        Expression expr = plainSelect.getWhere();

        if (expr == null) {
            this.joinCondition = null;
        } else {
            JoinConditionExpressionVisitor joinExpressionVisitor = new JoinConditionExpressionVisitor(this.schema);
            expr.accept(joinExpressionVisitor);
            this.joinCondition = joinExpressionVisitor.getExpression();
        }

        if (!orderFlag) {
            joinCondition = ExpressionBuilder.buildExpressionFromTwoChildrenLogicalJoin(this);
        }
    }

    private List<Integer> getJoinOrder() {
        List<Integer> joinOrder = new ArrayList<>();
        JoinOrder jo = new JoinOrder(prevOps, plainSelect);
        return jo.getOrder();
    }

    public UnionFind getUnionFind () {
        if (plainSelect.getWhere() == null) {
            return new UnionFind();
        }
        return new JoinOrder(getChildren(), plainSelect).getUnionFindFromExpression(plainSelect.getWhere());
    }

    private void sortPrevOps() {
        List<Integer> joinOrder = getJoinOrder();
        List<Operator> temp = new ArrayList<>();
        for (int i : joinOrder) {
            temp.add(prevOps.get(i));
        }
        prevOps = temp;
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

//    /**
//     * method to get children
//     */
//    @Override
//    public Operator[] getChildren() {
//        if (this.opRight == null || opLeft == null) {
//            return null;
//        } else {
//            return new Operator[]{this.opLeft, this.opRight};
//        }
//    }


    public List<Operator> getChildren() {
        return prevOps;
    }

    public Expression getJoinCondition() {
        return this.joinCondition;
    }

    public PlainSelect getPlainSelect() {
        return plainSelect;
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(LogicalOperatorVisitor visitor) {
        visitor.visit(this);
    }
}