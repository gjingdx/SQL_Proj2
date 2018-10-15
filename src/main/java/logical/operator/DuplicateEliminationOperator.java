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
    private Map<String, Integer> schema;
    private Operator op;

    /**
     * Constructor to initiate the operator using the sorted list in sort operator.
     * Eliminate duplicates with a sorted list.
     * @param operator assuming it's sort operator
     */
    public DuplicateEliminationOperator(Operator operator) {
        this.schema = operator.getSchema();
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
