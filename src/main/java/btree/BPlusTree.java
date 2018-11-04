package btree;

import io.BinaryTupleReader;
import io.BinaryTupleWriter;
import javafx.util.Pair;
import model.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class BPlusTree {
    private String btFile;
    private int attribute;
    private int order;
    private BinaryTupleReader binTupReader;
    private List<LeafNode> leafLayer;
    private List<DataEntry> dataEntries;


    public BPlusTree(String file, int attribute, int order, String indexFile) throws IOException {
        this.btFile = file;
        this.attribute = attribute;
        this.order = order;
        this.binTupReader = new BinaryTupleReader(btFile);
        this.leafLayer = new ArrayList<>();

    }

    private void generateDataEntries() {
        SortedMap<Integer, DataEntry> entryMap = new TreeMap<>();
        try {
            Tuple currTuple = binTupReader.readNextTuple();
            while (currTuple != null) {
                int pageId = binTupReader.getLastReadPageIndex();
                int tupleId = binTupReader.getLastReadTupleInPageIndex();
                int key = currTuple.getDataAt(attribute);
                entryMap.computeIfAbsent(key,
                        dataEntry -> new DataEntry(key, new ArrayList<>()));
                Rid rid = new Rid(pageId, tupleId);
                entryMap.get(key).rids.add(rid);

                currTuple = binTupReader.readNextTuple();
            }
        } catch (Exception e) {
            System.err.println("Failed to read next tuple when generateDataEntry");
        }
    }


    private void CreateLeafLayer() {
        List<DataEntry> leafEntries = new ArrayList<>();

        int count = 0;
        for (DataEntry dataEntry: dataEntries) {
            if (count == 2 * order) {
                LeafNode leafNode = new LeafNode(order, leafEntries);
                leafLayer.add(leafNode);
                leafEntries.clear();
                count = 0;
            }
            leafEntries.add(dataEntry);
            count++;
        }

        // handle the case when the last leafNode is underflow
        if (leafEntries.size() >= order || leafLayer.size() == 0) {
            LeafNode leafNode = new LeafNode(order, leafEntries);
            leafLayer.add(leafNode);
        } else {
            LeafNode secondLast = leafLayer.remove(leafLayer.size()-1);
            int numOfEntry = (2 * order + leafEntries.size())/2;
        }

    }
}
