package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.JoinExpressionVisitor;

import java.util.Map;


/**
 * Select operator
 *
 * @author Jing Guo
 */
public class SelectOperator extends Operator {

    private Operator prevOp;
    private Expression expression;
    private Map<String, Integer> currentSchema;

    /**
     * Constructor of Select Operator
     *
     * @param operator    previous (child) operator
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
     * @return the schema of select operator which is the same with the previous schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return currentSchema;
    }

    /**
     * method to get children
     */
    @Override
    public Operator[] getChildren() {
        if (this.prevOp == null) {
            return null;
        } else {
            return new Operator[]{this.prevOp};
        }
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }
}
