package com.sql.interpreter;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.Operator;
import util.Catalog;

import java.io.FileReader;
import java.io.StringReader;

public class Handler {
    public static void parseSql() {
        try {
            // try
            String inputPath = Catalog.getInstance().getSqlQueriesPath();
            CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath));
            Statement statement;
            int ind = 0;
            while ((statement = parser.Statement()) != null) {
                System.out.println("Read statement: " + statement);
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                System.out.println("Select body is " + select.getSelectBody());
                Operator operator = constructQueryPlan(plainSelect);
                operator.dump(ind);
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }
}
