package com.sql.interpreter;

import operator.*;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

public class Handler {

    public static void init() {
        String outputPath = Catalog.getInstance().getOutputPath();
        new File(outputPath).mkdirs();
    }

    public static void parseSql() {
        try {
            // try
            String inputPath = Catalog.getInstance().getSqlQueriesPath();
            CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath));
            Statement statement;
            int ind = 0;
            while ((statement = parser.Statement()) != null) {
                System.out.println(ind);
                System.out.println("Read statement: " + statement);
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                System.out.println("Select body is " + select.getSelectBody());
                Operator operator = constructQueryPlan(plainSelect);
                operator.dump(ind);
                ind++;
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

     /**
     * consturct a left deep join query plan
     * @param plainSelect
     * @return
     */
    public static Operator constructQueryPlan(PlainSelect plainSelect){
        int tableCount;
        Operator opLeft;
        if(plainSelect.getJoins() == null){
            tableCount = 1;
        }
        else{
            tableCount = 1 + plainSelect.getJoins().size();
        }
        opLeft = new ScanOperator(plainSelect, 0);
        System.out.println(opLeft.getSchema());
//        System.out.println("opLeft:");
//        opLeft.dump(1);
        for(int i = 1; i < tableCount; ++i){
            Operator opRight = new ScanOperator(plainSelect, i);
//            System.out.println("opRight:");
//            opRight.dump(1);
            opLeft = new JoinOperator(opLeft, opRight, plainSelect);
            System.out.println(opLeft.getSchema());
//            System.out.println("opLeft after join:");
//            opLeft.dump(1);
            if(plainSelect.getWhere() != null){
                opLeft = new SelectOperator(opLeft, plainSelect);
                System.out.println("opLeft after select:");
//                opLeft.dump(1);
            }
        }
        if(tableCount == 1 && plainSelect.getWhere() != null){
            opLeft = new SelectOperator(opLeft, plainSelect);
        }
        opLeft = new ProjectOperator(opLeft, plainSelect);
        if(plainSelect.getDistinct() != null){
            opLeft = new SortOperator(opLeft, plainSelect);
            opLeft = new DuplicateEliminationOperator(opLeft);
        }
        else {
            opLeft = new SortOperator(opLeft, plainSelect);
        }
        return opLeft;
    }
}
