package util;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class CatalogTest {
    Catalog instance = Catalog.getInstance();

    @Test
    public void setAliases() throws Exception {
    }

    @Test
    public void getInstance() throws Exception {
        assertEquals("Samples/samples/input/db/data/Sailors", instance.getDataPath("Sailors"));
        instance.updateCurrentSchema("Sailors");
        assertEquals(new Integer(1), instance.getCurrentSchema().get("Sailors.B"));
    }

    @Test
    public void getCurrentSchema() throws Exception {
//        assertEquals(0, instance.getCurrentSchema().size());
    }

    @Test
    public void setCurrentSchema() throws Exception {
    }

    @Test
    public void setNewSchema() throws Exception {

    }

    @Test
    public void setNewSchema1() throws Exception {
    }

    @Test
    public void getDataPath() throws Exception {
        assertEquals("Samples/samples/input/db/data/Sailors", instance.getDataPath("Sailors"));
    }

    @Test
    public void testStatsParser() throws Exception {
        Catalog.getInstance().parserStats();
    }

    @Test
    public void testSetAttributeOrder() throws Exception {
        String statement = "SELECT S.A, S.B, R.G FROM Sailors S, Reserves WHERE S.A = R.G and S.A < 100;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();

        Catalog.getInstance().setAttributeOrder(plainSelect);
    }

}