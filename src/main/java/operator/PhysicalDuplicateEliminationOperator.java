package operator;

import logical.operator.DuplicateEliminationOperator;
import model.Tuple;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Distinct operator
 * Created by Yufu Mo
 */
public class PhysicalDuplicateEliminationOperator extends PhysicalOperator {

    // stores tuples
    private List<Tuple> tupleList;
    private int currentIndex;
    private Map<String, Integer> schema;
    private PhysicalOperator physChild;

    /**
     * Constructor to initiate the operator using the sorted list in sort operator.
     * Eliminate duplicates with a sorted list.
     * @param operator assuming it's sort operator
     */
    public PhysicalDuplicateEliminationOperator(PhysicalOperator operator) {
        currentIndex = 0;
        this.schema = operator.getSchema();
        this.tupleList = new ArrayList<>();
        if (operator instanceof PhysicalSortOperator) {
            List<Tuple> sortedList = ((PhysicalSortOperator) operator).getTupleList();

            // initiate tupleList and eliminate duplicates
            if (sortedList.size() > 1) {
                tupleList.add(sortedList.get(0));
                for (int i = 1; i < sortedList.size(); i++) {
                    if (!sortedList.get(i).equals(sortedList.get(i - 1))) {
                        tupleList.add(sortedList.get(i));
                    }
                }
            }
        }
    }

    public PhysicalDuplicateEliminationOperator(DuplicateEliminationOperator logDupEliOp, Deque<PhysicalOperator> physChildren) {
        currentIndex = 0;
        this.schema = logDupEliOp.getSchema();
        this.tupleList = new ArrayList<>();
        this.physChild = physChildren.pop();
        if (physChild instanceof PhysicalSortOperator) {
            List<Tuple> sortedList = ((PhysicalSortOperator) physChild).getTupleList();

            // initiate tupleList and eliminate duplicates
            if (sortedList.size() > 1) {
                tupleList.add(sortedList.get(0));
                for (int i = 1; i < sortedList.size(); i++) {
                    if (!sortedList.get(i).equals(sortedList.get(i - 1))) {
                        tupleList.add(sortedList.get(i));
                    }
                }
            }
        }
    }

    /**
     * method that gets the next tuple.
     * @return the next tuple.
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
     * method that reset the operator.
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


}