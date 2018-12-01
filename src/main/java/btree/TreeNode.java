package btree;

abstract public class TreeNode {
    private int order;

    private int minChildKey;

    public TreeNode(int order) {
        this.order = order;
    }

    abstract public int getMinKey();

    abstract public int getMinChildKey();
}