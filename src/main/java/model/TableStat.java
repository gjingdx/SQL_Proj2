package model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TableStat record some basic stats of a table
 */
public class TableStat {
    private String alias; // alias of the table
    private long count; // count of tuple
    Map<String, ColumnStat> fieldStatSchema; // a map to store column info

    public TableStat(String alias) {
        this.alias = alias;
        fieldStatSchema = new HashMap<>();
    }

    public String getAlias() {
        return this.alias;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return this.count;
    }

    public void setFieldStatSchema (String schemaKey, ColumnStat columnStat) {
        fieldStatSchema.put(schemaKey, columnStat);
    }

    /**
     * set the tableStat from config
     * @param data specific format of string from Stats.txt
     */
    public void paserFromStatString (String data) {
        String [] splits = data.split("\\s+");
        if (splits.length < 2) {
            throw new IllegalArgumentException();
        }
        this.count = Integer.valueOf(splits[1]);
        for (int i = 2; i< splits.length; i++) {
            String [] info = splits[i].split(",");
            String schemaKey = alias + "." + info[0];
            int minValue = Integer.valueOf(info[1]);
            int maxValue = Integer.valueOf(info[2]);
            fieldStatSchema.put(schemaKey, new ColumnStat(minValue, maxValue));
        }
    }

    public ColumnStat getStat (String schemaKey) {
        if (!fieldStatSchema.containsKey(schemaKey)) {
            return null;
        }
        return fieldStatSchema.get(schemaKey);
    }

    public void removeColumn(String schemaKey) {
        if (fieldStatSchema.containsKey(schemaKey)) {
            fieldStatSchema.remove(schemaKey);
        }
    }

    public void resetColumnData(String schemaKey, Integer minValue, Integer maxValue) {
        if (minValue != null) {
            fieldStatSchema.get(schemaKey).minValue = minValue;
        }
        if (maxValue != null) {
            fieldStatSchema.get(schemaKey).maxValue = maxValue;
        }
    }
}
