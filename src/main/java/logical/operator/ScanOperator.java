package logical.operator;

import PlanBuilder.LogicalOperatorVisitor;
import PlanBuilder.PhysicalPlanBuilder;
import io.BinaryTupleReader;
import model.TableStat;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * PhysicalScanOperator
 * Read the table from disk and fetch a tuple
 */
public class ScanOperator extends Operator {
    private Operator op;
    private BinaryTupleReader binaryTupleReader;
    private Map<String, Integer> schema;
    private TableStat tableStat;
    private String table;
    private int tableIndex;


    /**
     * @param plainSelect is the statement of sql
     * @param tableIndex  is the index of the table in FROM section, start by 0
     */
    public ScanOperator(PlainSelect plainSelect, int tableIndex) {
        this.op = null;
        this.tableIndex = tableIndex;
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
        table = tableName;

        String aliasName = strs[strs.length - 1];

        Catalog.getInstance().setAliases(item);
        Catalog.getInstance().updateCurrentSchema(aliasName);

        this.schema = Catalog.getInstance().getCurrentSchema();

        tableStat = new TableStat(aliasName);
        tableStat.paserFromStatString(Catalog.getInstance().getStatsConfig(tableName));

        try {
            binaryTupleReader = new BinaryTupleReader(Catalog.getInstance().getDataPath(tableName));
        } catch (Exception e) {

        }
    }

    /**
     * get the schema
     */
    @Override
    public Map<String, Integer> getSchema() {
        return this.schema;
    }

    public BinaryTupleReader getBinaryTupleReader() {
        return binaryTupleReader;
    }

    public String getTable() {
        return table;
    }

    /**
     * method to get children
     */
    @Override
    public List<Operator> getChildren() {
        if (this.op == null) {
            return null;
        } else {
            List<Operator> children= new ArrayList<>();
            children.add(this.op);
            return children;
        }
    }

    @Override
    public void accept(PhysicalPlanBuilder visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(LogicalOperatorVisitor visitor) {
        visitor.visit(this);
    }

    public TableStat getTableStat() {
        return this.tableStat;
    }

    public Map<String, Integer> getColumnNameToTableId() {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<String, Integer> entry : this.schema.entrySet()) {
            map.put(entry.getKey(), tableIndex);
        }
        return map;
    }
}