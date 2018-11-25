package logical.operator;

import model.TableStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatVisitor {
    List<TableStat> tableStats;
    Map<String, Integer> columnNameToTableId;

    public StatVisitor() {
        tableStats = new ArrayList<>();
        columnNameToTableId = new HashMap<>();
    }

    public List<TableStat> getTableStat() {
        return  tableStats;
    }

    public Map<String, Integer> getColumnNameToTableId() {
        return columnNameToTableId;
    }

    public void visit(Operator op) {
        if (op instanceof ScanOperator) {
            tableStats.add(((ScanOperator) op).getTableStat());
            columnNameToTableId.putAll(((ScanOperator) op).getColumnNameToTableId());
        }
        else if (op instanceof SelectOperator) {
            tableStats.add(((SelectOperator) op).getTableStat());
            columnNameToTableId.putAll(((SelectOperator) op).getColumnNameToTableId());
        }
    }
}
