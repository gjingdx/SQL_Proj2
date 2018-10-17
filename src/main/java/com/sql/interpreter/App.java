package com.sql.interpreter;

/**
 * Read a local SQL file which contains several queries
 * Run line by line and output the results into separated files
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
