package operator;

import java.util.Map;
import net.sf.jsqlparser.statement.select.PlainSelect;

import logical.operator.ScanOperator;
import model.Tuple;
import util.Catalog;
import io.BinaryTableReader;

/**
 * PhysicalScanOperator
 * Read the table from disk and fetch a tuple
 */
public class PhysicalScanOperator extends PhysicalOperator{
    private BinaryTableReader binaryTableReader;
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
            this.binaryTableReader = null;
            return;
        }
        String tableName = strs[0];
        String aliasName = strs[strs.length - 1];

        Catalog.getInstance().setAliases(item);
        Catalog.getInstance().updateCurrentSchema(aliasName);

        this.schema = Catalog.getInstance().getCurrentSchema();
        binaryTableReader = new BinaryTableReader(tableName);
        binaryTableReader.init();
    }

    public PhysicalScanOperator(ScanOperator logScanOp) {

        this.schema = logScanOp.getSchema();
        this.binaryTableReader = logScanOp.getBinaryTableReader();
        binaryTableReader.init();
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple(){
        return binaryTableReader.readNextTuple();
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset(){
        binaryTableReader.init();
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema(){
        return this.schema;
    }
}