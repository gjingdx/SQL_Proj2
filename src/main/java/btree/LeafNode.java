package btree;

import java.util.ArrayList;

class LeafNode extends TreeNode {
    ArrayList<DataEntry> dataEntries;
    boolean hasParent;
    
    public LeafNode(int order) {
        super(order);
    }

    @Override
    public int getMinKey() {
        return dataEntries.get(0).getKey();
    }
}