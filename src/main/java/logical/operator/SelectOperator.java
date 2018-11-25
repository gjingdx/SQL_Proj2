package logical.operator;

import PlanBuilder.PhysicalPlanBuilder;
import model.TableStat;
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
    private TableStat tableStat;
    private Map<String, Constraints> constraintsMap;

    /**
     * Constructor of Select Operator
     * used when operator be not Scan Operator
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

        if (operator instanceof ScanOperator) {
            tableStat = ((ScanOperator) operator).getTableStat();
        }
    }

    /**
     * used when child op be Scan Operator
     *
     * @param op
     * @param constraints
     * @param plainSelect
     */
    public SelectOperator(Operator op, Map<String, Constraints> constraints, PlainSelect plainSelect) {
        if (!(op instanceof ScanOperator)) {
            throw new IllegalStateException();
        }
        this.prevOp = op;
        this.currentSchema = op.getSchema();
        this.constraintsMap = constraints;
        this.tableStat = ((ScanOperator) op).getTableStat();
        double rate = 1.0; // used to update tableStat count

        String fromItem = plainSelect.getFromItem().toString();
        String joinItems = plainSelect.getJoins().toString();
        joinItems = joinItems.substring(1, joinItems.length() - 1);
        String newStatement = "Select * from " + fromItem + ", " + joinItems + " where ";

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect newPlainSelect = null;
        for (String attribute : constraints.keySet()) {
            int originMin = tableStat.getStat(attribute).minValue;
            int originMax = tableStat.getStat(attribute).maxValue;

            Constraints constraint = constraints.get(attribute);
            if (constraint.getLowerBound() != null) {
                newStatement += attribute + ">=" + constraint.getLowerBound().toString() + " AND ";
                tableStat.getStat(attribute).minValue = Math.max(originMin, constraint.getLowerBound());
            }
            if (constraint.getUpperBound() != null) {
                newStatement += attribute + " <= " + constraint.getUpperBound().toString() + " AND ";
                tableStat.getStat(attribute).maxValue = Math.min(originMax, constraint.getUpperBound());
            }
            if (constraint.getEquality() != null) {
                newStatement += attribute + " = " + constraint.getEquality().toString() + " AND ";
            }

            rate *= (double)(tableStat.getStat(attribute).maxValue - tableStat.getStat(attribute).minValue)
                    / (double)(originMax - originMin);
        }
        newStatement = newStatement.substring(0, newStatement.length() - 5);
        System.out.println(newStatement);
        try {
            newPlainSelect = (PlainSelect) ((Select) parserManager.
                    parse(new StringReader(newStatement))).getSelectBody();
        } catch (Exception e ) {
            e.printStackTrace();
        }
        this.expression = newPlainSelect.getWhere();
        tableStat.setCount((int)Math.ceil(tableStat.getCount() * rate));
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

    public Map<String, Constraints> getConstraints() {
        return constraintsMap;
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

    public TableStat getTableStat() {
        return this.tableStat;
    }

    public Map<String, Integer> getColumnNameToTableId() {
        if (!(prevOp instanceof ScanOperator))
            return null;
        return ((ScanOperator) prevOp).getColumnNameToTableId();
    }
}
