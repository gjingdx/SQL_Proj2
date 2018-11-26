package logical.operator;

import PlanBuilder.LogicalOperatorVisitor;
import PlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SortOperator
 * created by Yufu Mo
 */
public class SortOperator extends Operator {

    // stores tuples
    private Map<String, Integer> schema;
    private Operator operator;
    private List<OrderByElement> order;

    /**
     * Constructor
     * read all tuples, store them in a list and sort them
     *
     * @param operator
     * @param plainSelect
     */
    @SuppressWarnings("unchecked")
    public SortOperator(Operator operator, PlainSelect plainSelect) {
        this.operator = operator;
        this.schema = operator.getSchema();
        this.order = plainSelect.getOrderByElements();
    }

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
    public List<OrderByElement> getOrder() {
        return this.order;
    }

    /**
     * method to get children
     */
    @Override
    public List<Operator> getChildren() {
        if (this.operator == null) {
            return null;
        } else {
            List<Operator> children= new ArrayList<>();
            children.add(this.operator);
            return children;
        }
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(LogicalOperatorVisitor visitor) {
        visitor.visit(this);
    }

}
