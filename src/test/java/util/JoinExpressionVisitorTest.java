package util;

import operator.ScanOperator;
import operator.SelectOperator;
import operator.JoinOperator;
import operator.Operator;
import model.Tuple;

import org.junit.Assert;
import org.junit.Test;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.parser.CCJSqlParserManager;


public class JoinExpressionVisitorTest{

    @Test
    public void testExtractExpression() throws Exception{
        Map<String, Integer> schemaMap = new HashMap<>();
        schemaMap.put("Sailors.A", 0);
        schemaMap.put("Sailors.B", 1);
        schemaMap.put("Sailors.C", 2);

        schemaMap.put("Reserves.G", 3);
        schemaMap.put("Reserves.H", 4);

        Catalog.getInstance().setCurrentSchema(schemaMap);
        Catalog.getInstance().setAliases("Sailors");
        Catalog.getInstance().setAliases("Boats");
        Catalog.getInstance().setAliases("Reserves");

        //String whereClause = "Sailors.A = Boats.D And Sailors.A = Reserves.G";

        String statement =  "SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Boats.D And Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Expression expr = plainSelect.getWhere();

        JoinExpressionVisitor joinExpress = new JoinExpressionVisitor(schemaMap);
        expr.accept(joinExpress);
        Expression output = joinExpress.getExpression();
        String expectedExpressioin = "Sailors.A = Reserves.G";
        Assert.assertEquals(output.toString(), expectedExpressioin);
    }

    @Test
    /*
    *          select
    *            |
    *           join
    *        /       \
    *      select    scan
    *        |  
    *       join
    *      /    \
    *    scan  scan
    */
    public void testWhereAndJoin() throws Exception{
        String statement =  "SELECT * FROM Sailors, Reserves, Boats WHERE Reserves.H = Boats.D And Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();

        Operator op1 = new ScanOperator(plainSelect, 0);
        Operator op2 = new ScanOperator(plainSelect, 1);
        Operator op3 = new JoinOperator(op1, op2, plainSelect);
        Operator op4 = new SelectOperator(op3, plainSelect);

        Operator op5 = new ScanOperator(plainSelect, 2);
        Operator op6 = new JoinOperator(op4, op5, plainSelect);
        Operator op7 = new SelectOperator(op6, plainSelect);

        Tuple tuple = op7.getNextTuple();
        while(tuple!=null){
            tuple = op7.getNextTuple();
        }
    }
}