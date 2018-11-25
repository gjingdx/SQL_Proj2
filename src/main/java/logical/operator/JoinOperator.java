package logical.operator;

import PlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;
import java.util.Map;

/**
 * PhysicalJoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class JoinOperator extends Operator {
    //private Operator opLeft, opRight;
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