package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import model.Tuple;
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
    private PlainSelect plainSelect;
    private Map<String, Integer> schema;
    Tuple outerTuple;
    Tuple innerTuple;
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
        this.plainSelect = plainSelect;
        this.schema = new HashMap<>();
        schema.putAll(opLeft.getSchema());
        for (Map.Entry<String, Integer> entry : opRight.getSchema().entrySet()) {
            schema.put(entry.getKey(), entry.getValue() + opLeft.getSchema().size());
        }
        Catalog.getInstance().setCurrentSchema(schema);

        outerTuple = null;
        innerTuple = null;

        Expression expr = this.plainSelect.getWhere();

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
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        Tuple next = crossProduction();
        
        while(next != null){
            SelectExpressionVisitor sv = new SelectExpressionVisitor(next, this.getSchema());
            joinCondition.accept(sv);
            if (sv.getResult()) {
                break;
            }
            next = crossProduction();
        }
        return next;
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset(){
        opLeft.reset();
        opRight.reset();
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }

    /**
     * implement cross production
     * @return result tuple
     */
    private Tuple crossProduction(){
        // update outer tuple and inner tuple
        if(outerTuple == null && innerTuple == null){
            outerTuple = opLeft.getNextTuple();
            innerTuple = opRight.getNextTuple();
        }
        else{
            innerTuple = opRight.getNextTuple();
            if(innerTuple == null){
                opRight.reset();
                outerTuple = opLeft.getNextTuple();
                innerTuple = opRight.getNextTuple();
            }
        }
        if(innerTuple == null || outerTuple == null){
            return null;
        }

        // Concentrate Tuple
        int[] newTupleData = new int[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for(int i = 0; i < outerTuple.getDataLength(); i++){
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for(int i = 0; i < innerTuple.getDataLength(); i++){
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
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

    public PlainSelect getPlainSelect() {
        return plainSelect;
    }

    public Tuple getOuterTuple() {
        return outerTuple;
    }

    public Tuple getInnerTuple() {
        return innerTuple;
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

}