package util.unionfind;

import java.util.*;

public class UnionFind {
    private Map<String, Constraints> elements;
    private Map<Constraints, Constraints> father;
    int size;
    int count;

    public UnionFind() {
        count = 0;
        elements = new HashMap<>();
    }

    private void union(Constraints p, Constraints q) {
        // precondition: p and q are not null
        if (p == null || q == null) {
            throw new NullPointerException();
        }
        if (p == q) {
            return;
        }

        q.merge(p);
        p.merge(q);
        if (q.getId() > p.getId()) {
            father.put(q, p);
        }
        else {
            father.put(p, q);
        }
        size--;
    }

    public void union(String attr1, String attr2) {
        union(find(attr1), find(attr2));
    }

    // helper method for creating new element.
    private void createElement(String attr) {
        Constraints ele = new Constraints(count);
        elements.put(attr, ele);
        father.put(ele, ele);
        count++;
        size++;
    }

    public Constraints find(String attr) {
        if (!elements.containsKey(attr)) {
            createElement(attr);
        }

        Constraints e = elements.get(attr);
        while (e != father.get(e)) {
            father.put(e, father.get(father.get(e)));
            e = father.get(e);
        }
        return e;
    }

    public int unionCount() {
        return size;
    }

    /**
     * Returns true if the two attributes are connnected.
     */
    public boolean connected(String attr1, String attr2) {
        return find(attr1) == find(attr2);
    }

    /**
     * Returns the set of all attributes in this union-find.
     * @return set of all attributes in the union-find.
     */
    public Set<String> attributeSet() {
        return elements.keySet();
    }

    /**
     * Returns the list of uf elemets which represents the connected componenets.
     *
     * @return UF list.
     */
    public Map<Constraints, List<String>> getUnions() {
        Map<Constraints, List<String>> map = new HashMap<>();
        for (String attr : elements.keySet()) {
            Constraints parent = father.get(elements.get(attr));
            map.computeIfAbsent(parent, a -> new ArrayList<>());
            map.get(parent).add(attr);
        }
        return map;
    }
}
