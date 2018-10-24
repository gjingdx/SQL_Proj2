package operator;

import io.BinaryTupleReader;
import io.BinaryTupleWriter;
import io.TupleReader;
import io.TupleWriter;
import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PhysicalExternalSortOperator extends PhysicalSortOperator {
    private final String id = UUID.randomUUID().toString().substring(0, 8); // the identifier for the temp file serial

    private List<TupleReader> buffer; // (blocksize - 1) tuple readers to read several temp files(runs)
    private BinaryTupleWriter outputBuffer; // output buffer to merge sort
    int blockSize;
    int index = 0; // index of run
    int prePass = 0;
    String finalTemp; // the name of sorted temp file
    TupleReader tr; // tuple reader to read the final temp file

    /**
     * used for simply test skipping the logical plan tree
     * @param operator
     * @param plainSelect
     */
    public PhysicalExternalSortOperator(PhysicalOperator operator, PlainSelect plainSelect) {
        super(operator, plainSelect);
        init();
    }

    /**
     * used by physical plan builder
     * @param logSortOp
     * @param physChildren
     */
    public PhysicalExternalSortOperator(SortOperator logSortOp, Deque<PhysicalOperator> physChildren) {
        super(logSortOp, physChildren);
        init();

    }

    /**
     * used by smj
     * @param order
     * @param physChildren
     */
    public PhysicalExternalSortOperator(List<OrderByElement> order, Deque<PhysicalOperator> physChildren) {
        super(order, physChildren);
        init();
    }

    /**
     * implements external merge sort
     * write the final sorted tuple list into the temp file named "temp_{id}_{pass}_0"
     */
    private void init() {
        new File(Catalog.getInstance().getTempPath()).mkdirs();
        this.blockSize = Catalog.getInstance().getSortBlockSize();
        buffer = new ArrayList<>(blockSize - 1);
        try {
            firstPass();
            mergeSort();
        } catch(IOException e) {
            e.printStackTrace();
        }
        finalTemp = getTempFileName(id, prePass, 0);
        tr = new BinaryTupleReader(finalTemp);
    }

    private void firstPass() throws IOException {
        index = 0;
        while (true) {
            int tupleCount = blockSize
                    * ((Constants.PAGE_SIZE - 2 * Constants.INT_SIZE)
                    / (schema.size() * Constants.INT_SIZE));
            List<Tuple> tupleList = new ArrayList<>();
            for (int i = 0; i < tupleCount; ++i) {
                Tuple tuple = physChild.getNextTuple();
                if (tuple == null) break;
                tupleList.add(tuple);
            }
            if (tupleList.size() == 0) {
                break;
            }
            Collections.sort(tupleList, new TupleComparator());
            TupleWriter tupleWriter = new BinaryTupleWriter(
                getTempFileName(id, 0, index), schema.size());
            index++;
            for (Tuple tuple : tupleList) {
                tupleWriter.writeNextTuple(tuple);
            }
            tupleWriter.finish();
        }
    }

    private void mergeSort() {
        int indexTemp = index;
        prePass = 0;
        while (indexTemp > 1) {
            index = 0;

            for (int i = 0; i < indexTemp; i += (blockSize - 1)) {
                buffer = new ArrayList<>();
                for (int j = 0; j < blockSize - 1 && (j < indexTemp - i); ++j) {
                    buffer.add(
                            new BinaryTupleReader(getTempFileName(id, prePass, i + j))
                    );
                }
                outputBuffer = new BinaryTupleWriter(getTempFileName(id, prePass + 1, index), schema.size());
                while (buffer.size() > 0) {
                    // find the minimum tuple
                    Tuple minimum_tuple = null;
                    int pos = -1;
                    for (int j = 0; j < buffer.size(); ++j) {
                        Tuple tuple = buffer.get(j).readNextTuple();
                        if (tuple == null) {
                            buffer.remove(j);
                            j--;
                            continue;
                        }
                        if (minimum_tuple == null) {
                            minimum_tuple = tuple;
                            pos = j;
                            continue;
                        }
                        if (new TupleComparator().compare(minimum_tuple, tuple) == 1) {
                            minimum_tuple = tuple;
                            pos = j;
                        }
                    }

                    outputBuffer.writeNextTuple(minimum_tuple);
                    // reset all unused buffer page
                    for (int j = 0; j < buffer.size(); ++j) {
                        if (j == pos) {
                            continue;
                        }
                        buffer.get(j).moveBack();
                    }
                }
                outputBuffer.finish();
                index++;
            }
            indexTemp = index;
            deletePrePassExtraTemp(prePass);
            prePass += 1;
        }
    }

    private String getTempFileName(String id, int pass, int index) {
        return Catalog.getInstance().getTempPath() +"temp_" + id + '_' + pass + '_' + index;
    }

    private void deletePrePassExtraTemp(int pass) {
        File[] files = new File(Catalog.getInstance().getTempPath()).listFiles();
        for (File file : files) {
            if (file.getName().contains(id + '_' + pass + '_')) {
                if (!file.delete()) {
                    System.out.print("Fail to delet pre passed file: " +file.getName());
                }
            }
        }
    }
    
    /**
     * read the final sorted temp
     */
    @Override
    public Tuple getNextTuple() {
        return tr.readNextTuple();
    }

    @Override
    public void reset() {
        tr.reset();
    }

    /**
     * make a stamp to record tuple reader
     */
    @Override
    public void recordTupleReader() {
        tr.recordPosition();
    }

    /**
     * revert to the record tuple reader
     */
    @Override
    public void revertToRecord() {
        tr.revertToPosition();
    }

    @Override
    public void closeTupleReader(){
        tr.close();
    }
}