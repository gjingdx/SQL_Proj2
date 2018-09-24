package util;

import org.junit.Test;

import static org.junit.Assert.*;

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

}