package btree;

abstract public class TreeNode {
    private int order;

    public TreeNode(int order) {
        this.order = order;
    }

    abstract public int getMinKey();


}