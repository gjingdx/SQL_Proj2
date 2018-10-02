package com.sql.interpreter;

import operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.Constants;
import util.JoinExpressionVisitor;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Handler class to parse sql, construct query plan and handle initialization
 * Created by Yufu Mo
 */
public class Handler {

    public static void init(String[] args) {
        String outputPath = Constants.OUTPUT_PATH;
        if (args != null && args.length == 2) {
            if (args[0].charAt(args[0].length() - 1)== '/') {
                args[0] = args[0].substring(0, args[0].length() - 1);
            }
            if (args[1].charAt(args[1].length() - 1) != '/') {
                args[1] = args[1] + "/";
            }
            outputPath = args[1];
            Constants.inputPath = args[0];
            Constants.DATA_PATH = Constants.inputPath + "/db/data/";
            Constants.SCHEMA_PATH = Constants.inputPath + "/db/schema.txt";
            Constants.OUTPUT_PATH = args[1];
            Constants.SQLQURIES_PATH = Constants.inputPath + "/queries.sql";
            System.out.println("Constants.inputPath init");
            System.out.println(Constants.inputPath);
        }
        new File(outputPath).mkdirs();
        final File[] files = new File(outputPath).listFiles();
        for(File f : files){
            f.delete();
        }
        outputPath += "query";
        Catalog.getInstance().setOutputPath(outputPath);
    }

    public static void parseSql() {
        try {
            // try
            String inputPath = Catalog.getInstance().getSqlQueriesPath();
            CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath));
            Statement statement;
            int ind = 1;
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
    *
    *           distinct  
    *              |
    *             sort
    *              |
    *             join
    *           /      \ 
    *         join    scan
    *        /    \
    *   select   select
    *      |       |
    *    scan     scan
    *
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
        if(hasRelatedExpression(opLeft.getSchema(), plainSelect)){
            opLeft = new SelectOperator(opLeft, plainSelect);
        }

        for(int i = 1; i < tableCount; ++i){
            Operator opRight = new ScanOperator(plainSelect, i);
            if(hasRelatedExpression(opRight.getSchema(), plainSelect)){
                opRight = new SelectOperator(opRight, plainSelect);
            }
            opLeft = new JoinOperator(opLeft, opRight, plainSelect);
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

    private static boolean hasRelatedExpression(Map<String, Integer> schemaMap, PlainSelect plainSelect){
        Expression originExpression = plainSelect.getWhere();
        if(originExpression == null){
            return false;
        }
        JoinExpressionVisitor joinExpressionVisitor = new JoinExpressionVisitor(schemaMap);
        originExpression.accept(joinExpressionVisitor);
        return joinExpressionVisitor.getExpression() != null;
    }
}
