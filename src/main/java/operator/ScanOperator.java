package operator;

import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;
import net.sf.jsqlparser.statement.select.PlainSelect;

import model.Tuple;
import util.Catalog;
import util.Constants;

/**
 * ScanOperator
 * Read the table from disk and fetch a tuple
 */
public class ScanOperator extends Operator{
    private Operator op;
    private File file;
    private RandomAccessFile readerPointer;
    private Map<String, Integer> schema;
    private ByteBuffer buffer;
    private int tupleSize;
    private int tupleCount;
    private int tuplePointer;

    /**
     * 
     * @param plainSelect is the statement of sql
     * @param tableIndex is the index of the table in FROM section, start by 0
     */
    public ScanOperator(PlainSelect plainSelect, int tableIndex){
        this.op = null;
        
        String item;
        if(tableIndex == 0){
            item = plainSelect.getFromItem().toString();
        }
        else{
            item = plainSelect.getJoins().get(tableIndex-1).toString();
        }

        String[] strs = item.split("\\s+");
        if(strs.length < 0){
            this.file = null;
            return;
        }
        String tableName = strs[0];
        String aliasName = strs[strs.length - 1];

        Catalog.getInstance().setAliases(item);
        Catalog.getInstance().updateCurrentSchema(aliasName);

        this.schema = Catalog.getInstance().getCurrentSchema();

        this.file = new File(Catalog.getInstance().getDataPath(tableName));
        try{
            this.readerPointer = new RandomAccessFile(this.file, "r");
        }catch(FileNotFoundException e){
            System.out.printf("Cannot find file %s!\n", this.file.getName());
        }
        readPage();
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        Tuple tuple = null;
        if(this.op != null){
            tuple = this.op.getNextTuple();
        }
        int [] tupleData = new int[tupleSize];
        if(tuplePointer >= (2+tupleCount*tupleSize) * Constants.INT_SIZE){
            readPage();
            if(buffer == null){
                try{
                    readerPointer.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                return null;
            }
        }
        for(int i =0 ; i < tupleSize; ++i){
            tupleData[i] = buffer.getInt(tuplePointer);
            tuplePointer += Constants.INT_SIZE;
        }
        tuple = new Tuple(tupleData);
        return tuple;
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset(){
        try{
            readerPointer.seek(0);
            readPage();
		}catch(IOException e){
			System.out.println("Files not found.");
		}
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }

    private void readPage(){
        try{
            this.buffer = ByteBuffer.allocate(Constants.PAGE_SIZE);
            FileChannel inChannel = readerPointer.getChannel();
            int byteRead = inChannel.read(buffer);
            if(byteRead == -1){
                this.buffer = null;
            }
            if(buffer != null){
                tupleSize = buffer.getInt(0);
                tupleCount = buffer.getInt(Constants.INT_SIZE);
                tuplePointer = 2 * Constants.INT_SIZE;
            }
        }catch(IOException e){
            System.out.printf("Unexpected table file format\n");
        }
    }
}