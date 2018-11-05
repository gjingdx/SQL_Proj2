package btree;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import util.Constants;

public class Deserializer {
    private File file;
    private ByteBuffer bufferPage;
    private RandomAccessFile readerPointer;
    private int lowKey;
    private int highKey;
    private int order;
    private int rootAddress;
    private int leafCount;
    private int intPointer;

    private int ridNo;
    private int ridMaxCount;
    private int entryNo;
    private int entryMaxCount;

    // If there is no bound, set lowKey, highKey as MinInteger, MaxInteger
    public Deserializer(File file, int lowKey, int highKey) {
        this.file = file;
        this.lowKey = lowKey;
        this.highKey = highKey;
        try {
            this.readerPointer = new RandomAccessFile(this.file, "r");
            readHead();
            searchLeafNode(lowKey, rootAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readHead() throws IOException {
        readerPointer.seek(0);
        this.bufferPage = ByteBuffer.allocate(Constants.PAGE_SIZE);
        FileChannel inChannel = readerPointer.getChannel();

        int byteRead = inChannel.read(bufferPage);
        if(byteRead < 3 * Constants.INT_SIZE) {
            throw(new IOException());
        }

        this.rootAddress = bufferPage.getInt(0 * Constants.INT_SIZE);
        this.leafCount = bufferPage.getInt(1 * Constants.INT_SIZE);
        this.order = bufferPage.getInt(2 * Constants.INT_SIZE);
    }

    private void searchLeafNode(int targetKey, int address) throws Exception {
        readerPointer.seek(address * Constants.PAGE_SIZE);
        this.bufferPage = ByteBuffer.allocate(Constants.PAGE_SIZE);
        FileChannel inChannel = readerPointer.getChannel();
        int byteRead = inChannel.read(bufferPage);
        if(byteRead < 1 * Constants.INT_SIZE) {
            throw(new IOException());
        }

        int nodeType = bufferPage.getInt(0);

        if (nodeType == 0) {
            setRidPostion();
            return;
        }
        int nextNodeAddress = -1;
        try{
            int keyCount = bufferPage.getInt(1 * Constants.INT_SIZE);
            int i = 2;
            for (; i <= keyCount + 1; i++) {
                int readKey = bufferPage.getInt(i * Constants.INT_SIZE);
                if (readKey > targetKey) {
                    break;
                }
            }
            int nextNodeAddressPosition = (2 + keyCount + (i - 2)) * Constants.INT_SIZE;
            nextNodeAddress = bufferPage.getInt(nextNodeAddressPosition);
        } catch (Exception e) {
            throw (new Exception("Unexpected format when deserialize an index node:" + e.getMessage()));
        }
        searchLeafNode(targetKey, nextNodeAddress);
        return;
    }

    private void setRidPostion() throws Exception {
        //searchLeafNode(lowKey, rootAddress);
        entryMaxCount = bufferPage.getInt(1 * Constants.INT_SIZE);
        intPointer = 2 * Constants.INT_SIZE;
        for (entryNo = 0; entryNo < entryMaxCount; entryNo++) {
            int readKey = bufferPage.getInt(intPointer);
            int ridCount = bufferPage.getInt(intPointer + Constants.INT_SIZE);
            if (readKey >= lowKey) {
                ridNo = 0;
                ridMaxCount = ridCount;
                intPointer += (2 * Constants.INT_SIZE);
                break;
            }
            intPointer += (2 + ridCount * 2) * Constants.INT_SIZE;
        }
        return;
    }
    
    public Rid getNextRid() {
        if (entryNo >= entryMaxCount) {
            // need read next page
            this.bufferPage = ByteBuffer.allocate(Constants.PAGE_SIZE);
            FileChannel inChannel = readerPointer.getChannel();
            try {
                int byteRead = inChannel.read(bufferPage);
                if (byteRead < Constants.INT_SIZE) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (bufferPage.getInt(0) == 1 || bufferPage.getInt(1 * Constants.INT_SIZE) == 0) {
                return null;
            }
            entryMaxCount = bufferPage.getInt(1 * Constants.INT_SIZE);
            entryNo = 0;
            intPointer = 2 * Constants.INT_SIZE; // read node type and entry count
        }

        if (ridNo == ridMaxCount) {
            if (bufferPage.getInt(intPointer) > highKey) {
                return null;
            }
            ridNo = 0;
            ridMaxCount = bufferPage.getInt(intPointer + Constants.INT_SIZE);
            intPointer += 2 * Constants.INT_SIZE; // read key value and rid count
        }

        Rid rid = new Rid(bufferPage.getInt(intPointer), bufferPage.getInt(intPointer + Constants.INT_SIZE));
        ridNo ++;
        intPointer += Constants.INT_SIZE * 2;
        if (ridNo == ridMaxCount) {
            // update ridNo and ridMaxCount in the next round
            entryNo ++;
        }
        return rid;
    }

    public void reset() throws Exception {
        this.readerPointer = new RandomAccessFile(this.file, "r");
        readHead();
        searchLeafNode(lowKey, rootAddress);
    }
}