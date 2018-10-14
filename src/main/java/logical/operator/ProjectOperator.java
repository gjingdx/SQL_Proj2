package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectOperator extends Operator {

    private Operator prevOp;
    private List<SelectItem> selectItems;
    private Map<String, Integer> currentSchema;

    /**
     * Constructor of PhysicalProjectOperator
     * @param operator previous (child) operator
     * @param plainSelect plain sql sentence
     */
    @SuppressWarnings("unchecked")
	public ProjectOperator(Operator operator, PlainSelect plainSelect) {
        prevOp = operator;
        selectItems = plainSelect.getSelectItems();
        // yet did not handle cases: select A,D from S, B
        if (selectItems.get(0).toString() == "*") {
            currentSchema = operator.getSchema();
        } else {
            currentSchema = new HashMap<>();
            int i = 0;
            for (SelectItem selectItem : selectItems) {
                currentSchema.put(selectItem.toString(),
                        i);
                i++;
            }
        }
    }

    /**
     * @return the next tuple selected by the project operator
     */
    @Override
    public Tuple getNextTuple() {
        Tuple next = prevOp.getNextTuple();
        if (next != null && currentSchema != prevOp.getSchema()) {
            int[] data = new int[currentSchema.size()];
            for (Map.Entry<String, Integer> entry : currentSchema.entrySet()){
                data[entry.getValue()] = next.getDataAt(prevOp.getSchema().get(entry.getKey()));
            }
            next = new Tuple(data);
        }
        return next;
    }

    /**
     * reset the project operator would be resetting the previous operator
     */
    @Override
    public void reset() {
        prevOp.reset();
    }

    /**
     * @return the current schema of project operator
     */
    @Override
    public Map<String, Integer> getSchema() {
        return currentSchema;
    }

    /**
     * method to get children
     */
    @Override
    public Operator[] getChildren(){
        if(this.prevOp == null){
            return null;
        }
        else{
            return new Operator[] {this.prevOp};
        }
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    /**
     * Abstract method for accepting PhysicalPlanBuilder visitor,
     * in which the visitor would visit the operator
     * @param visitor PhysicalPlanBuilder visitor to be accepted.
     */
    public void accept(PhysicalPlanBuilder visitor){
        visitor.visit(this);
    }
}
