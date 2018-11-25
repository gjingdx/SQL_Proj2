package util.unionfind;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnionFindTest {

    @Test
    public void union() throws Exception {
        UnionFind unionFind = new UnionFind();
        unionFind.createElement("A");
        unionFind.createElement("B");
        unionFind.createElement("C");
        unionFind.createElement("D");
        unionFind.createElement("E");
        unionFind.find("A").setLowerBound(100);
        unionFind.find("B").setLowerBound(150);
        unionFind.find("C").setUpperBound(100);
        unionFind.find("D").setEquality(100);
        unionFind.find("E").setLowerBound(100);
        unionFind.union("A", "B");
        assertEquals(unionFind.find("A"), unionFind.find("B"));
        assertEquals(unionFind.find("A").getLowerBound(), Integer.valueOf(150));
    }

    @Test
    public void find() throws Exception {
        UnionFind unionFind = new UnionFind();
        unionFind.createElement("A");
        unionFind.createElement("B");
        unionFind.createElement("C");
        unionFind.createElement("D");
        unionFind.createElement("E");
        unionFind.find("A").setLowerBound(100);
        unionFind.find("B").setLowerBound(150);
        unionFind.find("C").setUpperBound(100);
        unionFind.find("D").setEquality(100);
        unionFind.find("E").setLowerBound(100);
        unionFind.union("A", "B");
        assertEquals(unionFind.find("A"), unionFind.find("B"));
    }

    @Test
    public void connected() throws Exception {
    }

    @Test
    public void getUnions() throws Exception {
    }

}