package operator;

import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.*;

/**
 * PhysicalSortOperator
 * created by Yufu Mo
 */
public class PhysicalSortOperator extends PhysicalOperator {

    // stores tuples
    private List<Tuple> tupleList;
    private int currentIndex;
    private Map<String, Integer> schema;
    private PhysicalOperator physChild;
    private List<OrderByElement> order;

    /**
     * Constructor
     * read all tuples, store them in a list and sort them
     * @param operator
     * @param plainSelect
     */
    public PhysicalSortOperator(PhysicalOperator operator, PlainSelect plainSelect) {
        tupleList = new ArrayList<>();
        this.schema = operator.getSchema();

        // initialize the list
        Tuple tuple = operator.getNextTuple();
        while(tuple != null) {
            tupleList.add(tuple);
            tuple = operator.getNextTuple();
        }

        Collections.sort(tupleList, new TupleComparator());
        operator.reset();
    }

    public PhysicalSortOperator(SortOperator logSortOp, Deque<PhysicalOperator> physChildren) {
        //this.tupleList = logSortOp.getTupleList();
        this.currentIndex = 0;
        this.order = logSortOp.getOrder();
        this.schema = logSortOp.getSchema();
        this.physChild = physChildren.pop();

        tupleList = new ArrayList<>();
        // initialize the list
        Tuple tuple = physChild.getNextTuple();
        while(tuple != null) {
            tupleList.add(tuple);
            tuple = physChild.getNextTuple();
        }

        Collections.sort(tupleList, new TupleComparator());
        physChild.reset();
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple() {
        // TODO Auto-generated method stub
        Tuple tuple = null;
        if (currentIndex < tupleList.size()) {
            tuple = tupleList.get(currentIndex);
        }
        currentIndex++;
        return tuple;

    }

    /**
     * reset the operator.
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        currentIndex = 0;
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }

    /**
     * For distinct operator
     * @return sorted Tuple list
     */
    public List<Tuple> getTupleList() {
        return tupleList;
    }

    /**
     * comparator to sort tuples
     */
    class TupleComparator implements Comparator<Tuple> {

        @Override
        public int compare(Tuple t1, Tuple t2) {
            // TODO Auto-generated method stub
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
            for (int i = 0; i < schema.size(); i++){
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
}
