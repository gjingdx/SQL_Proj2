package operator;

import PlanBuilder.PhysicalOperatorVisitor;
import logical.operator.SelectOperator;
import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.JoinExpressionVisitor;
import util.SelectExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PhysicalSelectOperator
 *
 * @author Jing Guo
 */
public class PhysicalSelectOperator extends PhysicalOperator {

    private PhysicalOperator prevOp;
    private Expression expression;
    private Map<String, Integer> currentSchema;

    /**
     * Constructor of PhysicalSelectOperator
     *
     * @param operator    previous (child) operator
     * @param plainSelect plain sql sentence
     */
    public PhysicalSelectOperator(PhysicalOperator operator, PlainSelect plainSelect) {
        this.prevOp = operator;
        this.currentSchema = operator.getSchema();
        this.expression = plainSelect.getWhere();

        JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(this.currentSchema);
        expression.accept(joinExpress);
        expression = joinExpress.getExpression();
    }

    /**
     * init PhysicalSelectOperator
     *
     * @param logSelectOp
     * @param child
     */
    public PhysicalSelectOperator(SelectOperator logSelectOp, PhysicalOperator child) {
        this.prevOp = child;
        this.expression = logSelectOp.getExpression();
        this.currentSchema = logSelectOp.getSchema();

        JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(this.currentSchema);
        expression.accept(joinExpress);
        expression = joinExpress.getExpression();
    }

    public Expression getExpression() {
        return expression;
    }

    /**
     * @return the next tuple filtered by the Select PhysicalOperator
     */
    @Override
    public Tuple getNextTuple() {
        Tuple next = prevOp.getNextTuple();
        if (expression != null) {
            while (next != null) {
                SelectExpressionVisitor sv = new SelectExpressionVisitor(next, prevOp.getSchema());
                expression.accept(sv);
                if (sv.getResult()) {
                    break;
                }
                next = prevOp.getNextTuple();
            }
        }
        return next;
    }

    /**
     * reset the select operator would be resetting the previous operator
     */
    @Override
    public void reset() {
        prevOp.reset();
    }

    /**
     * @return the schema of select operator which is the same with the previous schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return currentSchema;
    }

    @Override
    public List<PhysicalOperator> getChildren() {
        List<PhysicalOperator> children = new ArrayList<>();
        children.add(prevOp);
        return children;
    }

    @Override
    public void accept(PhysicalOperatorVisitor phOpVisitor, int level) {
        phOpVisitor.visit(this, level);
    }
}
