package operator;

import PlanBuilder.PhysicalOperatorVisitor;
import io.BinaryTupleReader;
import logical.operator.ScanOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PhysicalScanOperator
 * Read the table from disk and fetch a tuple
 */
public class PhysicalScanOperator extends PhysicalOperator {
    protected BinaryTupleReader binaryTupleReader;
    protected Map<String, Integer> schema;
    private String tableName;

    /**
     * @param plainSelect is the statement of sql
     * @param tableIndex  is the index of the table in FROM section, start by 0
     */
    public PhysicalScanOperator(PlainSelect plainSelect, int tableIndex) {
        String item;
        if (tableIndex == 0) {
            item = plainSelect.getFromItem().toString();
        } else {
            item = plainSelect.getJoins().get(tableIndex - 1).toString();
        }

        String[] strs = item.split("\\s+");
        if (strs.length < 0) {
            this.binaryTupleReader = null;
            return;
        }
        String tableName = strs[0];
        String aliasName = strs[strs.length - 1];

        Catalog.getInstance().setAliases(item);
        Catalog.getInstance().updateCurrentSchema(aliasName);

        this.schema = Catalog.getInstance().getCurrentSchema();
        binaryTupleReader = new BinaryTupleReader(Catalog.getInstance().getDataPath(tableName));
    }

    /**
     * init PhysicalScanOperator
     *
     * @param logScanOp
     */
    public PhysicalScanOperator(ScanOperator logScanOp) {
        this.schema = logScanOp.getSchema();
        this.binaryTupleReader = logScanOp.getBinaryTupleReader();
        this.tableName = logScanOp.getTable();
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple() {
        try {
            return binaryTupleReader.readNextTuple();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * reset the operator.
     */
    @Override
    public void reset() {
        try {
            binaryTupleReader.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    @Override
    public List<PhysicalOperator> getChildren() {
        return null;
    }

    @Override
    public void accept(PhysicalOperatorVisitor phOpVisitor, int level) {
        phOpVisitor.visit(this, level);
    }
}