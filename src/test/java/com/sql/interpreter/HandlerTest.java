package com.sql.interpreter;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.Operator;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class HandlerTest {

    @Test
    public void parseSql() {
    }

    @Test
    public void constructQueryPlan() throws Exception{
        Handler handler = new Handler();
        String statement = "SELECT S.A, S.B, Reserves.G, Boats.D FROM Sailors AS S, Reserves, Boats WHERE Reserves.H = Boats.D And S.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        Operator op = handler.constructQueryPlan(plainSelect);
        op.dump(1);
    }
}

