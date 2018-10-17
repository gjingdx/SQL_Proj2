package com.sql.interpreter;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import org.junit.Test;

import java.io.StringReader;
import org.junit.Assert;

public class HandlerTest extends Handler {

    @Test
    public void parseSqlTest() {
    }

    @Test
    public void constructQueryPlan() throws Exception{
        String statement = "SELECT S.A, S.B, Reserves.G, Boats.D FROM Sailors AS S, Reserves, Boats WHERE Reserves.H = Boats.D And S.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op = Handler.constructPhysicalQueryPlan(plainSelect);
        op.dump(1);
    }

    @Test
    public void parserConfigTest(){
        int[][] config = parserConfig();
        int[][] expected =new int[2][2];
        expected[0][0] = 0;
        expected[0][1] = 0;
        expected[1][0] = 0;
        expected[1][1] = 0;
        Assert.assertArrayEquals(expected[0], config[0]);
        Assert.assertArrayEquals(expected[1], config[1]);
    }
}

