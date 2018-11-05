package model;

import util.Catalog;

public class IndexConfig {
    public boolean isClustered;
    public String tableFile;
    public String columnName;
    public int order;

    public IndexConfig(String config) {
        String [] configs = config.split("\\s+");
        this.columnName = configs[1];
        this.order = Integer.valueOf(configs[3]);
        this.isClustered = configs[2].equals("1");
        this.tableFile = Catalog.getInstance().getDataPath(configs[0]);
    }
}