package model;

import util.Catalog;

public class IndexConfig {
    public boolean isClustered; // is a clustered index
    public String indexFile; // index file location
    public String tableName; // related table
    public String columnName; // indexed column
    public String schemaName; // table.column
    public int order; // size of order

    // parser a line of index_info.txt
    public IndexConfig(String config) {
        String [] configs = config.split("\\s+");
        this.schemaName = configs[0] + "."+ configs[1];
        this.columnName = configs[1];
        this.tableName = configs[0];
        this.order = Integer.valueOf(configs[3]);
        this.isClustered = configs[2].equals("1");
        this.indexFile = Catalog.getInstance().getIndexFile(schemaName);
    }
}