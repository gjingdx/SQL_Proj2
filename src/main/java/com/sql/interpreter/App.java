package com.sql.interpreter;

import util.Catalog;
import util.Constants;

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
                configs = Handler.parserInterpreterConfig("../project5/interpreter_config_file.txt");
            }
            else{
                configs = Handler.parserInterpreterConfig(args[0]);
            } 
            Handler.init(configs);
            Catalog.getInstance().setJoinMethod(Constants.JoinMethod.SMJ);
            if (Catalog.getInstance().isBuildIndex()) {
                Handler.buildIndexes();
            }
            if (Catalog.getInstance().isEvaluateSQL()) {
                Handler.parseSql();
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
