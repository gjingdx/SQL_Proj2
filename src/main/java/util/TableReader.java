package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;

import model.Tuple;
import model.TupleReader;
import util.Catalog;
import util.Constants;

/**
 * Table Reader, implements the tuple reader and store the buffer
 * Read the table from disk and fetch a tuple
 */
public class TableReader implements TupleReader{
    private File file;
    private RandomAccessFile readerPointer;
    private ByteBuffer bufferPage;
    private int tupleSize;
    private int tupleCount;
    private int tuplePointer;

    public TableReader(String tableName){
        this.file = new File(Catalog.getInstance().getDataPath(tableName));
        
    }

    public TableReader(File file){
        this.file = file;
    }

    public TableReader(String filePath, String fileName){
        this.file = new File(filePath + '\\' + fileName);
    }

    @Override
    public void init(){
        try{
            this.readerPointer = new RandomAccessFile(this.file, "r");
        }catch(FileNotFoundException e){
            System.out.printf("Cannot find file %s!\n", this.file.getName());
        }
        readPage();
    }

    @Override
    public void readPage(){
        try{
            this.bufferPage = ByteBuffer.allocate(Constants.PAGE_SIZE);
            FileChannel inChannel = readerPointer.getChannel();
            int byteRead = inChannel.read(bufferPage);
            if(byteRead == -1){
                this.tupleCount = 0;
                this.tupleSize = 0;
                this.bufferPage = null;
            }
            if(bufferPage != null){
                tupleSize = bufferPage.getInt(0);
                tupleCount = bufferPage.getInt(Constants.INT_SIZE);
                tuplePointer = 2 * Constants.INT_SIZE;
            }
        }catch(IOException e){
            System.out.printf("Unexpected table file format\n");
        }
    }

    @Override
    public Tuple readNextTuple(){
        Tuple tuple = null;
        if(this.tupleCount <= 0){
            return null;
        }
        int [] tupleData = new int[tupleSize];
        if(tuplePointer >= (2+tupleCount*tupleSize) * Constants.INT_SIZE){
            readPage();
            if(bufferPage == null){
                try{
                    readerPointer.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                return null;
            }
        }
        for(int i =0 ; i < tupleSize; ++i){
            tupleData[i] = bufferPage.getInt(tuplePointer);
            tuplePointer += Constants.INT_SIZE;
        }
        tuple = new Tuple(tupleData);
        return tuple;
    }
}