package com.sql.interpreter;

import util.Catalog;

/**
 * Read a local sql file which contains several queries
 * Run line by line and output the results into seperated files
 */
public class App {
	public static void main(String[] args) {
        if (args != null && args.length == 2) {
            Catalog.getInstance().setInputPath(args[0]);
            Catalog.getInstance().setOutputPath(args[1]);
        }
        Handler.init();
        Handler.parseSql();
	}
}
