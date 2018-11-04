package btree;

import java.util.ArrayList;
import java.util.List;

class IndexNode extends TreeNode {
    List<Integer> keys;
    List<Integer> childrenAddresses;
    List<TreeNode> children;

    public IndexNode(int order) {
        super(order);
    }

    @Override
    public int getMinKey() {
        return keys.get(0);
    }

    public List<Integer> getChildrenAddresses() {
        return childrenAddresses;
    }

    public List<Integer> getKeys() {
        return keys;
    }
}