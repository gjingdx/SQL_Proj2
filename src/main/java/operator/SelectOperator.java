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

    public SelectOperator(Operator operator, PlainSelect plainSelect) {
        prevOp = operator;
        expression = plainSelect.getWhere();
        if(this.prevOp instanceof JoinOperator){
            JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(operator.getSchema());
            expression.accept(joinExpress);
            expression = joinExpress.getExpression();
        }
        currentSchema = operator.getSchema();
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
