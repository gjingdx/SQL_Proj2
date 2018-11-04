package btree;

import java.util.ArrayList;

class IndexNode extends TreeNode {
    ArrayList<Integer> keys;
    ArrayList<Integer> children_pointers;
    ArrayList<TreeNode> children;

    public IndexNode(int order) {
        super(order);
    }

    @Override
    public int getMinKey() {
        return keys.get(0);
    }
}