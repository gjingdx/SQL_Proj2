package operator;

import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * PhysicalSortOperator
 * created by Yufu Mo
 */
public abstract class PhysicalSortOperator extends PhysicalOperator {

    // stores tuples
    protected List<Tuple> tupleList;
    protected Map<String, Integer> schema;
    protected PhysicalOperator physChild;
    protected List<OrderByElement> order;

    /**
     * used for simply test skipping the logical plan tree
     *
     * @param operator
     * @param plainSelect
     */
    public PhysicalSortOperator(PhysicalOperator operator, PlainSelect plainSelect) {
        tupleList = new ArrayList<>();
        this.schema = operator.getSchema();
    }

    /**
     * used by physical plan builder
     *
     * @param logSortOp
     * @param child
     */
    public PhysicalSortOperator(SortOperator logSortOp, PhysicalOperator child) {
        this.order = logSortOp.getOrder();
        this.schema = logSortOp.getSchema();
        this.physChild = child;
    }

    /**
     * used before SMJ
     *
     * @param order
     * @param child
     */
    public PhysicalSortOperator(List<OrderByElement> order, PhysicalOperator child) {
        this.physChild = child;
        this.schema = physChild.getSchema();
        this.order = order;
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public abstract Tuple getNextTuple();

    /**
     * reset the operator.
     */
    @Override
    public abstract void reset();

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    /**
     * For distinct operator
     *
     * @return sorted Tuple list
     */
    public List<Tuple> getTupleList() {
        return tupleList;
    }

    /**
     * comparator to sort tuples
     */
    protected class TupleComparator implements Comparator<Tuple> {

        @Override
        public int compare(Tuple t1, Tuple t2) {
            // sort tuples from the order from sql query.
            if (order != null) {
                for (int i = 0; i < order.size(); i++) {
                    String column = order.get(i).toString();
                    int index = schema.get(column);
                    if (t1.getDataAt(index) > t2.getDataAt(index)) {
                        return 1;
                    }
                    if (t1.getDataAt(index) < t2.getDataAt(index)) {
                        return -1;
                    }
                }
            }


            // for tie breaker
            // sort tuples by the order of columns.
            for (int i = 0; i < schema.size(); i++) {
                if (t1.getDataAt(i) > t2.getDataAt(i)) {
                    return 1;
                }
                if (t1.getDataAt(i) < t2.getDataAt(i)) {
                    return -1;
                }
            }
            return 0;
        }
    }

    /**
     * get the order
     *
     * @return a list of OderByElement
     */
    public List<OrderByElement> getOrder() {
        return order;
    }

    /**
     * record the tupleReader position
     */
    public abstract void recordTupleReader();

    /**
     * revert the tuple reader to the record potision
     */
    public abstract void revertToRecord() throws Exception;

    /**
     * close the tuple readers
     */
    public abstract void closeTupleReader() throws Exception;
}
