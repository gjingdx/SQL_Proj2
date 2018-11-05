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
    private List<TreeNode> leafLayer;
    private List<DataEntry> dataEntries;
    private Serializer serializer;


    public BPlusTree(String file, int attribute, int order, String indexFile) throws IOException {
        this.btFile = file;
        this.attribute = attribute;
        this.order = order;
        this.binTupReader = new BinaryTupleReader(btFile);
        this.leafLayer = new ArrayList<>();
        this.serializer = new Serializer(indexFile);

        generateDataEntries();
        generateLeafLayer();
        List<TreeNode> indexLayer = leafLayer;
        while (indexLayer.size() != 1) {
            indexLayer = generateIndexLayer(indexLayer);
        }
        serializer.serialize(indexLayer.get(0));
        serializer.finish(order);
        binTupReader.close();

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


    private void generateLeafLayer() {
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
            // remove the second last leaf node and split the entries with the last one
            LeafNode secondLast = (LeafNode) leafLayer.remove(leafLayer.size()-1);
            int numEntry = (2 * order + leafEntries.size())/2;
            List<DataEntry> secondLastEntries = secondLast.dataEntries;
            List<DataEntry> lastEntries = secondLastEntries.subList(numEntry, secondLastEntries.size());
            lastEntries.addAll(leafEntries);
            secondLastEntries = secondLastEntries.subList(0, numEntry);
            // add the last two leaf nodes
            leafLayer.add(new LeafNode(order, secondLastEntries));
            leafLayer.add(new LeafNode(order, lastEntries));
        }

    }

    private List<TreeNode> generateIndexLayer(List<TreeNode> prevLayer) {
        List<TreeNode> indexLayer = new ArrayList<>();
        List<Integer> keys = new ArrayList<>();
        List<TreeNode> children = new ArrayList<>();
        List<Integer> childrenAddresses = new ArrayList<>();
        int count = 0;
        for(TreeNode prevNode: prevLayer) {
            children.add(prevNode);
            int address = serializer.serialize(prevNode);
            childrenAddresses.add(address);

            if (count == 0) {
                count++;
                continue;
            }
            if (count < 2 * order) {
                keys.add(prevNode.getMinKey());
                count++;
                continue;
            }
            if (count == 2 * order) {
                IndexNode indexNode = new IndexNode(order, keys, children, childrenAddresses);
                indexLayer.add(indexNode);

                keys.clear();
                children.clear();
                childrenAddresses.clear();
                count = 0;
            }


        }
        if (children != null) {
            if (keys.size() < order) {
                IndexNode secondLast = (IndexNode) indexLayer.remove(indexLayer.size()-1);
                List<Integer> secondLastKeys = secondLast.getKeys();
                int numKey = (secondLastKeys.size() + keys.size())/2;
                List<Integer> lastKeys = new ArrayList<>();
                if (numKey + 1 < 2 * order) {
                    lastKeys = secondLastKeys.subList(numKey+1, secondLastKeys.size());
                }
                lastKeys.add(children.get(0).getMinKey());
                lastKeys.addAll(keys);

                List<TreeNode> secondLastChildren = secondLast.getChildren();
                List<TreeNode> lastChildren = secondLastChildren.subList(numKey+1, secondLastChildren.size());
                lastChildren.addAll(children);
                secondLastChildren = secondLastChildren.subList(0, numKey+1);

                List<Integer> secondLastChildrenAddress = secondLast.getChildrenAddresses();
                List<Integer> lastChidrenAddress = secondLastChildrenAddress.subList(numKey+1, secondLastChildrenAddress.size());
                lastChidrenAddress.addAll(childrenAddresses);
                secondLastChildrenAddress = secondLastChildrenAddress.subList(0, numKey+1);


                indexLayer.add(new IndexNode(order, secondLastKeys, secondLastChildren, secondLastChildrenAddress));
                indexLayer.add(new IndexNode(order, lastKeys, lastChildren, lastChidrenAddress));
            } else {
                indexLayer.add(new IndexNode(order, keys, children, childrenAddresses));
            }

        }

        return indexLayer;
    }
}
