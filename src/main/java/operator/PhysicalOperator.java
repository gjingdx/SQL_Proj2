package operator;

import PlanBuilder.PhysicalOperatorVisitor;
import io.BinaryTupleWriter;
import io.TupleWriter;
import model.Tuple;
import util.Catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for operator
 * Created by Yufu Mo
 */
public abstract class PhysicalOperator {

    /**
     * get the next tuple of the operator's output
     * return null if the operator has no more output
     *
     * @return the next tuple of the operator's output
     */
    public abstract Tuple getNextTuple();

    /**
     * reset the operator's state and start returning its output again from the
     * beginning
     */
    public abstract void reset();

    /**
     * @return the current schema of the operator
     */
    public abstract Map<String, Integer> getSchema();

    public abstract List<PhysicalOperator> getChildren();

    public abstract void accept(PhysicalOperatorVisitor phOpVisitor, int level);

    public void dump(int i) {
        String path = Catalog.getInstance().getOutputPath() + i;
        TupleWriter tupleWriter = new BinaryTupleWriter(path, getSchema().size());
        Tuple tuple = getNextTuple();
        System.out.println("operator schema:" + getSchema());
        while (tuple != null) {
            tupleWriter.writeNextTuple(reorderTuple(tuple));
            tuple = getNextTuple();
        }
        // finish
        tupleWriter.finish();
    }

    public Tuple reorderTuple(Tuple tuple) {
        int[] data = new int[tuple.getDataLength()];
        Set<String> attributeSet = new HashSet<>();
        int ind = 0;
        for(String attribute : Catalog.getInstance().getAttributeOrder()) {
            attributeSet.add(attribute);
            if (getSchema().containsKey(attribute)) {
                //System.out.println(attribute);
                data[ind] = tuple.getDataAt(getSchema().get(attribute));
                ind++;
            }
        }
        for (String att : getSchema().keySet()) {
            if (!attributeSet.contains(att)) {
                System.out.println("^^^^^^^^^^^^^^&&&&&&&");
                data[ind] = tuple.getDataAt(getSchema().get(att));
                ind++;
            }
        }
        return new Tuple(data);
    }

}
