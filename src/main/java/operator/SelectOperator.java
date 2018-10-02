package operator;

import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.SelectExpressionVisitor;
import util.JoinExpressionVisitor;

import java.util.Map;

public class SelectOperator extends Operator{

    private Operator prevOp;
    private Expression expression;
    private Map<String, Integer> currentSchema;

    /**
     * Constructor of SelectOperator
     * @param operator previous (child) operator
     * @param plainSelect plain sql sentence
     */
    public SelectOperator(Operator operator, PlainSelect plainSelect) {
        this.prevOp = operator;
        this.currentSchema = operator.getSchema();
        this.expression = plainSelect.getWhere();
        
        JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(this.currentSchema);
        expression.accept(joinExpress);
        expression = joinExpress.getExpression();
    }

    /**
     * @return the next tuple filtered by the Select Operator
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
}
