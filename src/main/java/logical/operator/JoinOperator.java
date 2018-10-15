package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PhysicalJoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class JoinOperator extends Operator{
    private Operator opLeft, opRight;
    private Map<String, Integer> schema;
    Expression joinCondition;

    /**
     * Init the schema of PhysicalJoinOperator
     * @param opLeft last operator of outer tuple
     * @param opRight last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public JoinOperator(Operator opLeft, Operator opRight, PlainSelect plainSelect){
        this.opLeft = opLeft;
        this.opRight = opRight;
        this.schema = new HashMap<>();
        schema.putAll(opLeft.getSchema());
        for (Map.Entry<String, Integer> entry : opRight.getSchema().entrySet()) {
            schema.put(entry.getKey(), entry.getValue() + opLeft.getSchema().size());
        }
        Catalog.getInstance().setCurrentSchema(schema);

        Expression expr = plainSelect.getWhere();
        // return cross product if there's no selection
        if(expr == null){
            this.joinCondition = null;
        }
        // join by join condition
        else{
            JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(this.schema);
            expr.accept(joinExpressionVisitor);
            this.joinCondition = joinExpressionVisitor.getExpression();
        }
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }

    /**
     * method to get children
     */
    @Override
    public Operator[] getChildren(){
        if(this.opRight == null || opLeft == null){
            return null;
        }
        else{
            return new Operator[] {this.opLeft, this.opRight};
        }
    }

    public Expression getJoinCondition() {
        return this.joinCondition;
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

}