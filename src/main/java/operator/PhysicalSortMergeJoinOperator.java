package operator;

import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalSortMergeJoinOperator extends PhysicalJoinOperator {
    //private Expression joinCondition;
    //private Map<SouterTupleing, Integer> schema;
    List<OrderByElement> leftOrder;
    List<OrderByElement> rightOrder;

    /**
     * Init the schema of JoinOperator
     *
     * @param opLeft      last operator of outer tuple
     * @param opRight     last operator of inner tuple
     */
    public PhysicalSortMergeJoinOperator(JoinOperator logicalJoinOp, PhysicalSortOperator opLeft, PhysicalSortOperator opRight) {
        super(opLeft, opRight, logicalJoinOp);
        this.leftOrder = opLeft.getOrder();
        this.rightOrder = opRight.getOrder();
        init();
    }

    @Override
    public void reset() {
        opRight.reset();
        opLeft.reset();
        init();
    }

    private void init(){
        ((PhysicalSortOperator)opRight).recordTupleReader();
        outerTuple = opLeft.getNextTuple();
        innerTuple = opRight.getNextTuple();
    }
    
    /**
     * implements sort merge join
     * @return the next joined tuple
     */
    @Override
    protected Tuple crossProduction() {
        // search for equal
        try{
            while (outerTuple != null && innerTuple != null){
                if (new TupleComparator().compare(outerTuple, innerTuple) < 0) {
                    outerTuple = opLeft.getNextTuple();
                }
                if (new TupleComparator().compare(outerTuple, innerTuple) > 0) {
                    ((PhysicalSortOperator)opRight).recordTupleReader();
                    innerTuple = opRight.getNextTuple();
                    continue;
                }

                Tuple ret = joinTuple(outerTuple, innerTuple);
                innerTuple = opRight.getNextTuple();

                if (innerTuple == null || new TupleComparator().compare(outerTuple, innerTuple) != 0) {
                    outerTuple = opLeft.getNextTuple();
                    ((PhysicalSortOperator)opRight).revertToRecord();
                    innerTuple = opRight.getNextTuple();
                }

                if (ret != null) {
                    return ret;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try{
            ((PhysicalSortOperator)opLeft).closeTupleReader();
            ((PhysicalSortOperator)opRight).closeTupleReader();
        } catch (Exception e) {
            System.out.println("Fail to close the some temp file");
        }
        return null;
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    /**
     * comparator to sort tuples
     */
    protected class TupleComparator implements Comparator<Tuple> {
        @Override
        public int compare(Tuple t1, Tuple t2) {
            if (leftOrder.size() == rightOrder.size()) {
                for (int i = 0; i < leftOrder.size(); i++) {
                    int index1 = schema.get(leftOrder.get(i).toString());
                    int index2 = schema.get(rightOrder.get(i).toString());
                    index2 -= opLeft.getSchema().size();
                    if (t1.getDataAt(index1) > t2.getDataAt(index2)) {
                        return 1;
                    }
                    if (t1.getDataAt(index1) < t2.getDataAt(index2)) {
                        return -1;
                    }
                }
            }
            return 0;
        }
    }

}