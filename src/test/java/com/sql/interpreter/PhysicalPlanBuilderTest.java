package com.sql.interpreter;

import logical.operator.*;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.*;
import org.junit.Test;
import util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class PhysicalPlanBuilderTest {
    /**
     * test visit(ScanOperator logScanOp)
     */
    @Test
    public void visit() throws Exception {

        String statement = "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator logScanOp = new ScanOperator(plainSelect, 0);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        //logScanOp.accept(physPB);
        physPB.visit(logScanOp);

        PhysicalOperator physOp = physPB.getPhysOpChildren().peekLast();


        // read expected result from disk
        ArrayList<String> expectedResult = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(Constants.DATA_PATH + "Sailors_humanreadable"));
        String line;
        while((line = br.readLine())!=null){
            expectedResult.add(line);
        }
        br.close();
        // get scanOperator result
        ArrayList<String> outputResult = new ArrayList<>();
        Tuple tuple;
        while((tuple = physOp.getNextTuple()) != null){
            outputResult.add(tuple.toString());
        }

    }


    /**
     * test visit(SelectOperator logSelectOp)
     */
    @Test
    public void visit1() throws Exception{
        String statement = "SELECT * FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator logScanOp = new ScanOperator(plainSelect, 0);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        SelectOperator logSelectOp = new SelectOperator(logScanOp, plainSelect);
        logSelectOp.accept(physPB);
        PhysicalOperator physSelectOp = physPB.getPhysOpChildren().peek();

        Tuple tuple = physSelectOp.getNextTuple();
        while(tuple != null){
            assertEquals(9, tuple.getDataAt(1));
            System.out.println(tuple);
            tuple = physSelectOp.getNextTuple();
        }
    }

    /**
     * test visit(JoinOperator logicalJoinOp)
     */
    @Test
    public void visit2() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves, Boats Where Boats.D = Reserves.H and Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        ScanOperator op1 = new ScanOperator(plainSelect, 0);
        ScanOperator op2 = new ScanOperator(plainSelect, 1);
        JoinOperator logJoinOp = new JoinOperator(op1, op2, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(logJoinOp);
        PhysicalOperator physJoinOp= physPB.getPhysOpChildren().peek();
        Tuple tuple;
        ArrayList<String> outputStrings = new ArrayList<>();
        while((tuple = physJoinOp.getNextTuple()) !=null){
            outputStrings.add(tuple.toString());
        }
        System.out.println(outputStrings);
        //ssertEquals(expectedResult, outputStrings);
    }

    /**
     * visit(ProjectOperator logicalProjOp)
     */
    @Test
    public void visit3() throws Exception{

        String statement = "SELECT BT.E, BT.F FROM Boats AS BT WHERE BT.E = 9;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        Operator scanOp = new ScanOperator(plainSelect, 0);
        Operator selectOp = new SelectOperator(scanOp, plainSelect);
        ProjectOperator projectOp = new ProjectOperator(selectOp, plainSelect);

        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(projectOp);
        PhysicalOperator physProjOp = physPB.getPhysOpChildren().peek();

        Tuple tuple = physProjOp.getNextTuple();
        while(tuple != null){
            assertEquals(9, tuple.getDataAt(0));
            System.out.println(tuple.getDataAt(0));
            tuple = physProjOp.getNextTuple();
        }
    }

    /**
     * visit(SortOperator logSortOp)
     */
    @Test
    public void visit4() throws Exception{
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator logScanOp = new ScanOperator(plainSelect, 0);

        SortOperator logSortOp = new SortOperator(logScanOp, plainSelect);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(logSortOp);
        PhysicalOperator physSortOp = physPB.getPhysOpChildren().peek();
        Tuple tuple = physSortOp.getNextTuple();
        long last = Long.MIN_VALUE;
        while(tuple != null){
            long cur = tuple.getDataAt(2);
            assertEquals(true, last <= cur);
            tuple = physSortOp.getNextTuple();
        }
    }

    /**
     * visit(DuplicateEliminationOperator logDupElimOp)
     */
    @Test
    public void visit5() throws Exception{
        String statement = "SELECT * FROM Boats AS BT ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator logScanOp = new ScanOperator(plainSelect, 0);
        Operator logSortOp = new SortOperator(logScanOp, plainSelect);
        DuplicateEliminationOperator logDupOp = new DuplicateEliminationOperator(logSortOp);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        physPB.visit(logDupOp);
        PhysicalOperator physDupOp = physPB.getPhysOpChildren().peek();

        Tuple tuple = physDupOp.getNextTuple();
        Tuple last = new Tuple(new int[0]);
        while(tuple != null){
            assertNotSame(last, tuple);
            System.out.println(tuple);
            last = tuple;
            tuple = physDupOp.getNextTuple();
        }
    }
}