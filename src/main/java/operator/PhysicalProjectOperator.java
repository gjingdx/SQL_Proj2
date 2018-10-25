package operator;

import logical.operator.ProjectOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalProjectOperator extends PhysicalOperator {

    //Deque<PhysicalOperator> physOpChildren;
    PhysicalOperator prevPhysicalOp;
    List<SelectItem> selectItems;
    Map<String, Integer> currentSchema;

    /**
     * Constructor of PhysicalProjectOperator
     *
     * @param physicalOp  previous (child) operator
     * @param plainSelect plain sql sentence
     */
    @SuppressWarnings("unchecked")
    public PhysicalProjectOperator(PhysicalOperator physicalOp, PlainSelect plainSelect) {
        prevPhysicalOp = physicalOp;
        selectItems = plainSelect.getSelectItems();
        // yet did not handle cases: select A,D from S, B
        if (selectItems.get(0).toString() == "*") {
            currentSchema = physicalOp.getSchema();
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
     * init PhysicalProjectOperator
     *
     * @param logicalProjOp
     * @param child
     */
    public PhysicalProjectOperator(ProjectOperator logicalProjOp, PhysicalOperator child) {
        //this.physOpChildren = physChildren;
        prevPhysicalOp = child;
        this.selectItems = logicalProjOp.getSelectItems();
        this.currentSchema = logicalProjOp.getSchema();
        if (selectItems.get(0).toString() == "*") {
            currentSchema = prevPhysicalOp.getSchema();
        } else {
            currentSchema = new HashMap<>();
            int i = 0;
            for (SelectItem selectItem : selectItems) {
                currentSchema.put(selectItem.toString(), i);
                i++;
            }
        }
    }


    /**
     * @return the next tuple selected by the project operator
     */
    @Override
    public Tuple getNextTuple() {
        Tuple next = prevPhysicalOp.getNextTuple();
        if (next != null && currentSchema != prevPhysicalOp.getSchema()) {
            int[] data = new int[currentSchema.size()];
            for (Map.Entry<String, Integer> entry : currentSchema.entrySet()) {
                data[entry.getValue()] = next.getDataAt(prevPhysicalOp.getSchema().get(entry.getKey()));
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
        prevPhysicalOp.reset();
    }

    /**
     * @return the current schema of project operator
     */
    @Override
    public Map<String, Integer> getSchema() {
        return currentSchema;
    }

}
