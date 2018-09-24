package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.*;
import operator.Operator;
import operator.ScanOperator;
import model.Tuple;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;

import static org.junit.Assert.*;

public class SelectExpressionVisitorTest {

    static SelectExpressionVisitor visitor;
    //Catalog catalog = Catalog.getInstance();
    //HashMap<String, Integer> myschema = new HashMap<>();
    //private static final String queriesFile = "Samples/samples/input/queries.sql";



    /**
     * test visit(EqualsTo equalsTo)
     * meanwhile test visit(Column column) and visit(LongValue longValue)
     */
    @Test
    public void visit() {
        String statement = "SELECT * FROM Sailors S WHERE S.A = 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) == 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select expression successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * test visit(NotEqualsTo notEqualsTo);
     */
    @Test
    public void visit1() {
        String tableName = "Sailors";
        String tableFolder = "Samples/samples/input/db/data/";
        File file = new File(tableFolder + tableName);

        String statement = "SELECT * FROM Sailors S WHERE S.A != 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) != 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select expression successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * test visit(GreaterThan greaterThan)
     */
    @Test
    public void visit2() {
        String statement = "SELECT * FROM Sailors S WHERE S.A > 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) > 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select greaterThan successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * test visit(GreaterThanEquals greaterThanEquals)
     */
    @Test
    public void visit3() {
        String statement = "SELECT * FROM Sailors S WHERE S.A >= 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) >= 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select greaterThan successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * test visit(MinorThan minorThan)
     */
    @Test
    public void visit4() {
        String statement = "SELECT * FROM Sailors S WHERE S.A < 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) < 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select greaterThan successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * visit(MinorThanEquals minorThanEquals)
     */
    @Test
    public void visit5() {
        String statement = "SELECT * FROM Sailors S WHERE S.A <= 3;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) <= 3) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select greaterThan successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * visit(AndExpression andExpression)
     */
    @Test
    public void visit6() {
        String statement = "SELECT * FROM Sailors S WHERE S.A > 3 and S.A < 5;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try{
            PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
            Operator op = new ScanOperator(plainSelect, 0);
            Catalog catalog = Catalog.getInstance();
            System.out.println(catalog.getCurrentSchema());
            Tuple currentTuple = op.getNextTuple();
            FromItem fromItem = plainSelect.getFromItem();
            System.out.println("From item is " + fromItem);
            Expression whereEx = plainSelect.getWhere();
            System.out.println("Where expression is " + whereEx);
            int index = op.getSchema().get("S.A");
            System.out.println(index);
            while(currentTuple != null){
                visitor = new SelectExpressionVisitor(currentTuple, op.getSchema());
                if (whereEx != null) {
                    whereEx.accept(visitor);
                    System.out.println(currentTuple.getDataAt(index));
                    if (currentTuple.getDataAt(index) > 3 && currentTuple.getDataAt(index) < 5) {
                        assertEquals(true, visitor.getResult());
                    } else {
                        assertEquals(false, visitor.getResult());
                    }

                }
                System.out.println("select expression successfully");
                currentTuple = op.getNextTuple();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}