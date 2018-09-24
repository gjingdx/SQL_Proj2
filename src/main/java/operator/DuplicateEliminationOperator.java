package operator;

import model.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distinct operator
 * Created by Yufu Mo  ym445
 */
public class DuplicateEliminationOperator extends Operator{

    // stores tuples
    private List<Tuple> tupleList;
    private int currentIndex;
    private Map<String, Integer> schema;

    /**
     * Constructor to initiate the operator using the sorted list in sort operator.
     * Eliminate duplicates with
     * @param operator assuming it's sort operator
     */
    public DuplicateEliminationOperator(Operator operator) {
        currentIndex = 0;
        this.schema = operator.getSchema();
        this.tupleList = new ArrayList<>();
        if (operator instanceof SortOperator) {
            List<Tuple> sortedList = ((SortOperator) operator).getTupleList();

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
