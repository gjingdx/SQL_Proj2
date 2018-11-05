package btree;

import java.util.ArrayList;
import java.util.List;

class IndexNode extends TreeNode {
    List<Integer> keys;
    List<TreeNode> children;
    List<Integer> childrenAddresses;

    public IndexNode(int order) {
        super(order);
    }

    public IndexNode(int order, List<Integer> keys, List<TreeNode> children, List<Integer> childrenAddresses) {
        super(order);
        this.keys = keys;
        this.children = children;
        this.childrenAddresses = childrenAddresses;
    }

    @Override
    public int getMinKey() {
        return keys.get(0);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public List<Integer> getChildrenAddresses() {
        return childrenAddresses;
    }

    public List<Integer> getKeys() {
        return keys;
    }
}