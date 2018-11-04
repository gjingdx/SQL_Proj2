package btree;

import java.util.List;

class LeafNode extends TreeNode {
    List<DataEntry> dataEntries;
    boolean hasParent;
    
    public LeafNode(int order) {
        super(order);
    }

    public LeafNode(int order, List<DataEntry> dataEntries) {
        super(order);
        this.dataEntries = dataEntries;
    }

    @Override
    public int getMinKey() {
        return dataEntries.get(0).getKey();
    }

    public List<DataEntry> getDataEntries() {
        return dataEntries;
    }
}