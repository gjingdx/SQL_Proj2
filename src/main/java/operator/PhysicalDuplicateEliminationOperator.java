package operator;

import PlanBuilder.PhysicalOperatorVisitor;
import logical.operator.DuplicateEliminationOperator;
import model.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distinct operator
 * Created by Yufu Mo
 */
public class PhysicalDuplicateEliminationOperator extends PhysicalOperator {

    // stores tuples
    private List<Tuple> tupleList;
    private Map<String, Integer> schema;
    private PhysicalOperator physChild;
    private Tuple prevTuple;
    private PhysicalOperator operator;

    /**
     * Constructor to initiate the operator using the sorted list in sort operator.
     * Eliminate duplicates with a sorted list.
     *
     * @param operator assuming it's sort operator
     */
    public PhysicalDuplicateEliminationOperator(PhysicalOperator operator) {
        this.schema = operator.getSchema();
        this.operator = operator;
        this.prevTuple = null;
    }

    public PhysicalDuplicateEliminationOperator(DuplicateEliminationOperator logDupEliOp, PhysicalOperator child) {
        this.prevTuple = null;
        this.physChild = child;
        this.operator = physChild;
        this.schema = operator.getSchema();
//        if (physChild instanceof PhysicalSortOperator) {
//
//        }
    }

    /**
     * method that gets the next tuple.
     *
     * @return the next tuple.
     */
    @Override
    public Tuple getNextTuple() {
        // TODO Auto-generated method stub
        Tuple tuple = operator.getNextTuple();

        while (tuple != null && tuple.equals(prevTuple)) {

            tuple = operator.getNextTuple();
        }
        prevTuple = tuple;
        return tuple;
    }

    /**
     * method that reset the operator.
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        operator.reset();
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    @Override
    public List<PhysicalOperator> getChildren() {
        List<PhysicalOperator> children = new ArrayList<>();
        children.add(physChild);
        return children;
    }

    @Override
    public void accept(PhysicalOperatorVisitor phOpVisitor, int level) {
        phOpVisitor.visit(this, level);
    }


}
