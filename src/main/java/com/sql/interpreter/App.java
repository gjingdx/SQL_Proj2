package com.sql.interpreter;

import util.Catalog;

/**
 * Read a local SQL file which contains several queries
 * Run line by line and output the results into separated files
 */
public class App {
    /**
     * Entrance of the project
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            String[] configs;
            if (args.length == 0) {
                configs = Handler.parserInterpreterConfig("Samples/samples-2/interpreter_config_file.txt");
            }
            else{
                configs = Handler.parserInterpreterConfig(args[0]);
            } 
            Handler.init(configs);
            if (Catalog.getInstance().isBuildIndex()) {
                Handler.buildIndexes();
            }
            if (Catalog.getInstance().isEvaluateSQL()) {
                Handler.parseSql();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
