package operator;

import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.IOException;

import logical.operator.ScanOperator;
import net.sf.jsqlparser.statement.select.PlainSelect;

import model.Tuple;
import model.TupleReader;
import util.Catalog;
import util.Constants;
import util.TableReader;

/**
 * PhysicalScanOperator
 * Read the table from disk and fetch a tuple
 */
public class PhysicalScanOperator extends PhysicalOperator{
    private TableReader tableReader;
    private Map<String, Integer> schema;

    /**
     * 
     * @param plainSelect is the statement of sql
     * @param tableIndex is the index of the table in FROM section, start by 0
     */
    public PhysicalScanOperator(PlainSelect plainSelect, int tableIndex){
        String item;
        if(tableIndex == 0){
            item = plainSelect.getFromItem().toString();
        }
        else{
            item = plainSelect.getJoins().get(tableIndex-1).toString();
        }

        String[] strs = item.split("\\s+");
        if(strs.length < 0){
            this.tableReader = null;
            return;
        }
        String tableName = strs[0];
        String aliasName = strs[strs.length - 1];

        Catalog.getInstance().setAliases(item);
        Catalog.getInstance().updateCurrentSchema(aliasName);

        this.schema = Catalog.getInstance().getCurrentSchema();
        tableReader = new TableReader(tableName);
        tableReader.init();
    }

    public PhysicalScanOperator(ScanOperator logScanOp) {

        this.schema = logScanOp.getSchema();
        this.tableReader = logScanOp.getTableReader();
        tableReader.init();
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        return tableReader.readNextTuple();
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset(){
        tableReader.init();
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }
}