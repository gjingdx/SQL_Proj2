package operator;

import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.SelectExpressionVisitor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * JoinOperator
 * it will inherit two tuple from two operators
 * then execute cross production of the two tuples
 */
public class PhysicalSortMergeJoinOperator extends PhysicalOperator {
    private Expression joinCondition;
    private Map<String, Integer> schema;
    List<OrderByElement> leftOrder;
    List<OrderByElement> rightOrder;
    PhysicalExternalSortOperator opRight;
    PhysicalExternalSortOperator opLeft;
    Tuple Tr;
    Tuple Ts;
    Tuple Gs;
    Boolean isInLoop;

    /**
     * Init the schema of JoinOperator
     *
     * @param opLeft      last operator of outer tuple
     * @param opRight     last operator of inner tuple
     * @param plainSelect unused temporally
     */
    public PhysicalSortMergeJoinOperator(JoinOperator logicalJoinOp, PhysicalExternalSortOperator opLeft, PhysicalExternalSortOperator opRight) {
        this.opLeft = opLeft;
        this.opRight = opRight;
        this.leftOrder = opLeft.getOrder();
        this.rightOrder = opRight.getOrder();
        this.joinCondition = logicalJoinOp.getJoinCondition();
        this.schema = logicalJoinOp.getSchema();
        Tr = opLeft.getNextTuple();
        Gs = opRight.getNextTuple();
        Ts = null;
        isInLoop = false;

    }

    @Override
    public void reset() {
        opRight.reset();
        opLeft.reset();
        Tr = opLeft.getNextTuple();
        Gs = opRight.getNextTuple();
    }

    /**
     * implement cross production
     *
     * @return result tuple
     */
    @Override
    public Tuple getNextTuple() {
        Tuple next = crossProduction();
        // return cross product if there's no selection
        if (joinCondition == null) {
            return next;
        }

        while (next != null) {
            SelectExpressionVisitor sv = new SelectExpressionVisitor(next, this.getSchema());
            joinCondition.accept(sv);
            if (sv.getResult()) {
                break;
            }
            next = crossProduction();
        }
        return next;

    }

    protected Tuple crossProduction() {
        // search for equal
        if (Tr == null || Gs == null)
            return null;
        if (new TupleComparator().compare(Tr, Gs) != 0) {
            findNextEqual();
        }
        if (Tr == null || Gs == null)
            return null;
        if (new TupleComparator().compare(Tr, Gs) == 0) {
            Tuple t = joinTuple(Tr, Gs);
            Gs = opRight.getNextTuple();
            return t;
        }
        // Concentrate Tuple
        return null;
    }

    private void findNextEqual() {
        while (Tr != null && Gs != null) {
            if (new TupleComparator().compare(Tr, Gs) < 0) {
                Tr = opLeft.getNextTuple();
                // reset to block
                if (isInLoop) {
                    opRight.setRecordTupleReader();
                    Gs = Ts;
                }
                continue;
            }
            if (new TupleComparator().compare(Tr, Gs) > 0) {
                Gs = opRight.getNextTuple();
                isInLoop = false;
                continue;
            }
            Ts = Gs;
            opRight.recordTupleReader();
            isInLoop = true;
            break;
        }
    }

    private Tuple joinTuple(Tuple outerTuple, Tuple innerTuple) {
        if (outerTuple == null || innerTuple == null) {
            return null;
        }
        int[] newTupleData = new int[outerTuple.getDataLength() + innerTuple.getDataLength()];
        for (int i = 0; i < outerTuple.getDataLength(); i++) {
            newTupleData[i] = outerTuple.getDataAt(i);
        }
        for (int i = 0; i < innerTuple.getDataLength(); i++) {
            newTupleData[i + outerTuple.getDataLength()] = innerTuple.getDataAt(i);
        }
        Tuple tuple = new Tuple(newTupleData);
        return tuple;
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