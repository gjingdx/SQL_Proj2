package btree;

import io.BinaryTupleReader;
import io.BinaryTupleWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BPlusTree {
    String btFile;
    BinaryTupleReader binTupReader;
    List<TreeNode> leafLayer;


    public BPlusTree(String file) {
        this.btFile = file;
        this.binTupReader = new BinaryTupleReader(btFile);
        this.leafLayer = new ArrayList<>();

    }
}
