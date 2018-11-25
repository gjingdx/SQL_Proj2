package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;

import java.util.List;
import java.util.Map;

/**
 * Abstract class for operator
 * Created by Yufu Mo
 */
public abstract class Operator {

    /**
     * @return the current schema of the operator
     */
    public abstract Map<String, Integer> getSchema();

    /**
     * @return an list of children operation
     */
    public abstract List<Operator> getChildren();


    /**
     * Abstract method for accepting PhysicalPlanBuilder visitor,
     * in which the visitor would visit the operator
     *
     * @param visitor PhysicalPlanBuilder visitor to be accepted.
     */
    public abstract void accept(PhysicalPlanBuilder visitor);
}
