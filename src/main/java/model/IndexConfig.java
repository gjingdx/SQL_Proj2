package model;

import util.Catalog;

public class IndexConfig {
    public boolean isClustered;
    public String indexFile;
    public String tableName;
    public String columnName;
    public String schemaName;
    public int order;

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