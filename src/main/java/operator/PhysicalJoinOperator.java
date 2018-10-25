package operator;

import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;
import util.JoinExpressionVisitor;
import util.SelectExpressionVisitor;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * PhysicalJoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public abstract class PhysicalJoinOperator extends PhysicalOperator {
    protected PhysicalOperator opLeft, opRight;

    //private Deque<PhysicalOperator> physOpChildren;
    protected Expression joinCondition;
    protected Map<String, Integer> schema;
    protected Tuple outerTuple;
    protected Tuple innerTuple;

    /**
     * Init the schema of PhysicalJoinOperator
     *
     * @param opLeft      last operator of outer tuple
     * @param opRight     last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, PlainSelect plainSelect) {
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
        if (expr == null) {
            this.joinCondition = null;
        }
        // join by join condition
        else {
            JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(this.schema);
            expr.accept(joinExpressionVisitor);
            this.joinCondition = joinExpressionVisitor.getExpression();
        }

        outerTuple = null;
        innerTuple = null;
    }

    /**
     * init physical join op using its physical op children and logical join op
     *
     * @param opLeft
     * @param opRight
     * @param logicalJoinOp
     */
    public PhysicalJoinOperator(PhysicalOperator opLeft, PhysicalOperator opRight, JoinOperator logicalJoinOp) {
        this.opLeft = opLeft;
        this.opRight = opRight;
        this.schema = new HashMap<>();
        this.joinCondition = logicalJoinOp.getJoinCondition();
        this.schema = logicalJoinOp.getSchema();

        outerTuple = null;
        innerTuple = null;
    }

    /**
     * init join op
     *
     * @param logicalJoinOp
     * @param leftChild
     * @param rightChild
     */
    public PhysicalJoinOperator(JoinOperator logicalJoinOp, PhysicalOperator leftChild, PhysicalOperator rightChild) {
        this.opLeft = leftChild;
        this.opRight = rightChild;
        this.joinCondition = logicalJoinOp.getJoinCondition();
        this.schema = logicalJoinOp.getSchema();

        outerTuple = null;
        innerTuple = null;
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple() {
        Tuple next = crossProduction();
        // return cross product if there's no selection
        if (joinCondition == null) {
            return next;
        }

        while (next != null) {
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
    public void reset() {
        opLeft.reset();
        opRight.reset();
        innerTuple = null;
        outerTuple = null;
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    /**
     * implement cross production
     *
     * @return result tuple
     */
    protected abstract Tuple crossProduction();

    protected Tuple joinTuple(Tuple outerTuple, Tuple innerTuple) {
        if (outerTuple == null || innerTuple == null) {
            return null;
        }
        int[] newTupleData = new int[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for (int i = 0; i < outerTuple.getDataLength(); i++) {
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for (int i = 0; i < innerTuple.getDataLength(); i++) {
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
    }

}