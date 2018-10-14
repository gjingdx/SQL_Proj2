package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import model.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distinct operator
 * Created by Yufu Mo
 */
public class DuplicateEliminationOperator extends Operator{

    // stores tuples
    private List<Tuple> tupleList;
    private int currentIndex;
    private Map<String, Integer> schema;
    private Operator op;

    /**
     * Constructor to initiate the operator using the sorted list in sort operator.
     * Eliminate duplicates with a sorted list.
     * @param operator assuming it's sort operator
     */
    public DuplicateEliminationOperator(Operator operator) {
        currentIndex = 0;
        this.schema = operator.getSchema();
        this.tupleList = new ArrayList<>();
        if (operator instanceof SortOperator) {
            this.op = operator;
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

    /**
     * method to get children
     */
    @Override
    public Operator[] getChildren(){
        if(this.op == null){
            return null;
        }
        else{
            return new Operator[] {this.op};
        }
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

}
