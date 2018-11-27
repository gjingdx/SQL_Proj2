package util.unionfind;

import util.Constants;

import java.util.*;

/**
 * Union find class creating sets of Constraints and merge
 * the constraints
 *
 * @author Yufu Mo ym445
 */
public class UnionFind {
    private Map<String, Constraints> elements;
    private Map<Constraints, Constraints> father;
    int size;
    int count;

    public UnionFind() {
        count = 0;
        elements = new HashMap<>();
        father = new HashMap<>();
    }

    /**
     * union two constraints together
     *
     * @param p
     * @param q
     */
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

    /**
     * helper method for creating new element
     *
     * @param attr
     */
    public void createElement(String attr) {
        Constraints ele = new Constraints(count);
        elements.put(attr, ele);
        father.put(ele, ele);
        count++;
        size++;
    }

    /**
     * find the corresponding father constraint of this attribute
     * update the father map
     *
     * @param attr
     * @return
     */
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

    public int getUnionCount() {
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
    public Set<String> getAttributeSet() {
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


    /**
     * returns the list of information of each connected components in the
     * format of string.
     * @return list of string
     */
    public List<String> printUnions() {
        List<String> result = new ArrayList<>();
        Map<Constraints, List<String>> parentMap = new HashMap<>();
        for (String attr : getAttributeSet()) {
            Constraints curr = find(attr);
            if (!parentMap.containsKey(curr)) {
                parentMap.put(curr, new ArrayList<>());
            }
            parentMap.get(curr).add(attr);
        }

        for (Constraints e : parentMap.keySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[[");
            for (String attr : parentMap.get(e)) {
                sb.append(attr + ", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("], equals ");
            sb.append(e.getEquality());
            sb.append(", min ");
            sb.append(e.getLowerBound());
            sb.append(", max ");
            sb.append(e.getUpperBound());
            sb.append("]");
            result.add(sb.toString());
        }

        return result;
    }

    public void setAttr(String attr, Constraints constraints) {
        find(attr).setLowerBound(constraints.getLowerBound());
        find(attr).setUpperBound(constraints.getUpperBound());
        find(attr).setEquality(constraints.getEquality());
        find(attr).setId(constraints.getId());
    }
}
