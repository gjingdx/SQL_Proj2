package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.JoinExpressionVisitor;
import util.unionfind.Constraints;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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

    public SelectOperator(Operator op, String attribute, Constraints constraint, PlainSelect plainSelect) {
        this.prevOp = op;
        this.currentSchema = op.getSchema();
        String fromItem = plainSelect.getFromItem().toString();
        String joinItems = plainSelect.getJoins().toString();
        String newStatement = "Select * from " + fromItem + "," + joinItems;

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect newPlainSelect = null;
        if (constraint.getLowerBound() != null) {
            newStatement += "where" + attribute + ">=" + constraint.getEquality().toString();
        } else if (constraint.getUpperBound() != null) {
            newStatement += "where" + attribute + "<=" + constraint.getEquality().toString();
        } else if (constraint.getEquality() != null) {
            newStatement += "where" + attribute + "=" + constraint.getEquality().toString();
        }
        try {
            newPlainSelect = (PlainSelect) ((Select) parserManager.
                    parse(new StringReader(newStatement))).getSelectBody();
        } catch (Exception e ) {
            e.printStackTrace();
        }

        this.expression = newPlainSelect.getWhere();
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
    public List<Operator> getChildren() {
        if (this.prevOp == null) {
            return null;
        } else {
            List<Operator> children= new ArrayList<>();
            children.add(this.prevOp);
            return children;
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
