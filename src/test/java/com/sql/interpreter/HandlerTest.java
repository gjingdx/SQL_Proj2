package com.sql.interpreter;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

public class HandlerTest extends Handler {

    @Test
    public void parseSqlTest() {
        Map<Set<Integer>, Integer> store = new HashMap<>();
        Set set = new HashSet<>(Arrays.asList("1", "2"));
        store.put(set, 1 );
        if (store.containsKey(new HashSet<>(Arrays.asList("1", "2")))) {
            int a = 1;
        }
    }

    @Test
    public void constructQueryPlan() throws Exception {
        Handler.init(new String[0]);
        String statement = "SELECT S.A, S.B, Reserves.G, Boats.D FROM Sailors AS S, Reserves, Boats WHERE Reserves.H = Boats.D And S.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op = Handler.constructPhysicalQueryPlan(plainSelect);
        //op.dump(1);
    }

    @Test
    public void parserConfigTest() throws Exception {
        Assert.assertTrue(parserPlanBuilderConfig());
    }

    @Test
    public void parserIndexInfoTest() throws Exception {
        parserIndexInfo();
    }
}

