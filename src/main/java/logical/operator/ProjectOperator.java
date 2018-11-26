package logical.operator;

import PlanBuilder.LogicalOperatorVisitor;
import PlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project operator
 * @author Jing Guo
 */
public class ProjectOperator extends Operator {

    private Operator prevOp;
    private List<SelectItem> selectItems;
    private Map<String, Integer> currentSchema;

    /**
     * Constructor of PhysicalProjectOperator
     *
     * @param operator    previous (child) operator
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
    public List<Operator> getChildren() {
        if (this.prevOp == null) {
            return null;
        } else {
            List<Operator> children= new ArrayList<>();
            children.add(this.prevOp);
            return children;
        }
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    /**
     * Abstract method for accepting PhysicalPlanBuilder visitor,
     * in which the visitor would visit the operator
     *
     * @param visitor PhysicalPlanBuilder visitor to be accepted.
     */
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(LogicalOperatorVisitor visitor) {
        visitor.visit(this);
    }
}
