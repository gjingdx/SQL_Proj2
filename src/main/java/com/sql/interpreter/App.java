package com.sql.interpreter;

import util.Catalog;
import util.Constants;

/**
 * Read a local sql file which contains several queries
 * Run line by line and output the results into seperated files
 */
public class App {
    /**
     * Entrance of the project
     * @param args
     */
    public static void main(String[] args) {
        Handler.init(args);
        Handler.parseSql();
	  }
}
