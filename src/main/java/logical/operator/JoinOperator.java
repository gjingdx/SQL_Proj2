package logical.operator;

import PlanBuilder.JoinOrder;
import PlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;
import util.JoinExpressionVisitor;

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

//    /**
//     * Init the schema of PhysicalJoinOperator
//     *
//     * @param opLeft      last operator of outer tuple
//     * @param opRight     last operator of inner tuple
//     * @param plainSelect unused temporally
//     */
//    public JoinOperator(Operator opLeft, Operator opRight, PlainSelect plainSelect) {
//        this.opLeft = opLeft;
//        this.opRight = opRight;
//        this.schema = new HashMap<>();
//        schema.putAll(opLeft.getSchema());
//        for (Map.Entry<String, Integer> entry : opRight.getSchema().entrySet()) {
//            schema.put(entry.getKey(), entry.getValue() + opLeft.getSchema().size());
//        }
//        Catalog.getInstance().setCurrentSchema(schema);
//
//        Expression expr = plainSelect.getWhere();
//        // return cross product if there's no selection
//        if (expr == null) {
//            this.joinCondition = null;
//        }
//        // join by join condition
//        else {
//            JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(this.schema);
//            expr.accept(joinExpressionVisitor);
//            this.joinCondition = joinExpressionVisitor.getExpression();
//        }
//    }

    public JoinOperator(List<Operator> prevOps, PlainSelect plainSelect) {
        this.prevOps = prevOps;
        this.plainSelect = plainSelect;
        sortPrevOps();

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
        }
        else {
            JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(this.schema);
            expr.accept(joinExpressionVisitor);
            this.joinCondition = joinExpressionVisitor.getExpression();
        }
    }

    private List<Integer> getJoinOrder() {
        List<Integer> joinOrder = new ArrayList<>();
        JoinOrder jo = new JoinOrder(prevOps, plainSelect);
        return jo.getOrder();
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

}