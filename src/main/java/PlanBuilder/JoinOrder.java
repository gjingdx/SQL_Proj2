package PlanBuilder;

import logical.operator.Operator;
import logical.operator.StatVisitor;
import model.ColumnStat;
import model.Histogram;
import model.TableStat;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.Catalog;
import util.JoinExpressionVisitor;
import util.UnionFindExpressionVisitor;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A tool to get optimal join order
 */
public class JoinOrder {
    List<Operator> joinChildren;
    Map<Set<Integer>, OrderInfo> store;
    List<Set<String>> childrenSchemaKeys;
    PlainSelect plainSelect;
    List<TableStat> tableStats;
    Map<String, Integer> columnToTableId;
    UnionFind unionFind;

    Map<Set<Integer>, Long> storeProduce = new HashMap<>();

    public JoinOrder(List<Operator> joinChildren, PlainSelect plainSelect) {
        this.joinChildren = new ArrayList<>(joinChildren);
        this.plainSelect = plainSelect;
        if (plainSelect.getWhere() == null) {
            unionFind = new UnionFind();
        } else {
          unionFind = getUnionFindFromExpression(plainSelect.getWhere());
        }
        this.childrenSchemaKeys = new ArrayList<>();
        for (Operator op : joinChildren) {
            childrenSchemaKeys.add(op.getSchema().keySet());
        }

        StatVisitor statVisitor = new StatVisitor();
        for (Operator op : joinChildren) {
            statVisitor.visit(op);
        }
        this.tableStats = statVisitor.getTableStat();
        this.columnToTableId = statVisitor.getColumnNameToTableId();

        //init dp
        store = new HashMap<>();
        for (int i = 0; i<joinChildren.size(); i++) {
            ArrayList<Integer> order = new ArrayList<>();
            order.add(i);
            store.put(new HashSet<>(Arrays.asList(i)), new OrderInfo(0, order));
        }
    }

    /**
     * predict an optimal left deep join order
     * @return
     */
    public List<Integer> getOrder() {
        Set finalSet = new HashSet<>();
        for (int i = 0; i < joinChildren.size(); i++) {
            finalSet.add(i);
        }
        // start dp
        helper(finalSet);
        return store.get(finalSet).order;
    }

    public int getV(ColumnStat cs) {
        return cs.maxValue - cs.minValue + 1;
    }

    /**
     * calculate the count of tuple after the join of several tables
     * @param set
     * @return
     */
    public long getProducedCount (Set<Integer> set) {
        double numerator = 1.0;
        for (int i : set) {
            numerator *= tableStats.get(i).getCount();
        }

        double denominator = 1.0;
        if (plainSelect.getWhere() == null) {
            return (int)numerator;
        }
        UnionFind tempUnionFind = refineUnionFind(set);
        for(Map.Entry<Constraints, List<String>> entry : tempUnionFind.getUnions().entrySet()) {
            List<String> columnList = entry.getValue();
            Constraints constraints = entry.getKey();
            double decial = 1.0;
            if (columnList.size() > 1) {
                for (String column : columnList) {
                    int tableId = columnToTableId.get(column);
                    decial = Math.max(getV(tableStats.get(tableId).getStat(column)), decial);
                }
                denominator *= decial;
            }
        }
        storeProduce.put(new HashSet<>(set), (long)Math.ceil(numerator / denominator));
        return (long)Math.ceil(numerator / denominator);
        //return (t1.count * t2.count) / Math.max(getV(t1), getV(t2));
    }

    public long getProducedCountByHistogram (Set<Integer> set) {
        double numerator = 1.0;
        for (int i : set) {
            numerator *= tableStats.get(i).getCount();
        }

        double probability = 1.0;
        if (plainSelect.getWhere() == null) {
            return (int)numerator;
        }

        UnionFind tempUnionFind = refineUnionFind(set);
        for(Map.Entry<Constraints, List<String>> entry : tempUnionFind.getUnions().entrySet()) {
            List<String> columnList = entry.getValue();
            Constraints constraints = entry.getKey();
            if (columnList.size() > 1) {
                int tableId = columnToTableId.get(columnList.get(0));
                int max = tableStats.get(tableId).getStat(columnList.get(0)).maxValue;
                int min = tableStats.get(tableId).getStat(columnList.get(0)).minValue;
                List<Histogram> histograms = new ArrayList<>();
                for (String column : columnList) {
                    String tableColumn = columnWithAliasToColumnWithTable(column);
                    Histogram histogram = Catalog.getInstance().getHsitogram(tableColumn);
                    histograms.add(histogram);
                }
                if (histograms.size() > 0) {
                    for (int key = min; key<= max; key++) {
                        double temp = 1.0;
                        for (int i = 0; i < histograms.size(); i ++) {
                            temp *= histograms.get(i).getProbability(key);
                        }
                        probability *= temp;
                    }
                }
            }
        }

        long ret = (long)Math.ceil(numerator * probability);
        storeProduce.put(new HashSet<>(set), ret);
        //return (long)Math.ceil(numerator / denominator);
        if (ret == 0) {
            int a =1;
        }
        return ret;
    }

    private String columnWithAliasToColumnWithTable (String s) {
        String []splits = s.split("\\.");
        String alias = splits[0];
        String column = splits[1];
        return Catalog.getInstance().getTableNameFromAlias(alias) + "." + column;
    }

    /**
     * dynamic programming method recursive helper
     * @param originSet
     * @return
     */
    private OrderInfo helper(Set<Integer> originSet) {
        Set<Integer> set = new HashSet<>(originSet);
        if (store.containsKey(set)) {
            return store.get(set);
        }
        int cost = Integer.MAX_VALUE;
        ArrayList<Integer> content = new ArrayList<>();
        OrderInfo ret = new OrderInfo(cost, new ArrayList<>());
        for(int key : set) {
            content.add(key);
        }
        for (int i=0; i<content.size();i++) {
            // remove one of them as the last to join
            set.remove(content.get(i));
            OrderInfo temp = helper(set);
            long producedCount = getProducedCountByHistogram(set);
            if (ret.cost > temp.cost + producedCount) {
                ret.cost = temp.cost + producedCount;
                ret.order = new ArrayList<>(temp.order);
                ret.order.add(content.get(i));
            }
            set.add(content.get(i));
        }

        store.put(set, ret);
        return ret;
    }

    /**
     * used to record the cost and order solution
     */
    public class OrderInfo {
        public long cost;
        public ArrayList<Integer> order;
        public OrderInfo(long cost, ArrayList<Integer> order) {
            this.cost = cost;
            this.order = new ArrayList<>(order);
        }
    }

    private Map<String, Integer> mergeColumnToTableId(Set<Integer> set) {
        Map <String, Integer> schema = new HashMap<>();
        for (int i : set) {
            // no need to update the map value, since will not use
            schema.putAll(joinChildren.get(i).getSchema());
        }
        return schema;
    }

    /**
     * get the overall schema related to the set
     * @param set
     * @return
     */
    private Map<String, Integer> mergeSchema(Set<Integer> set) {
        Map <String, Integer> schema = new HashMap<>();
        for (int i : set) {
            // no need to update the map value, since will not use
            schema.putAll(joinChildren.get(i).getSchema());
        }
        return schema;
    }

    private Expression getRelatedExpression(Set<Integer> set, Expression originalExpression) {
        JoinExpressionVisitor jVisitor = new JoinExpressionVisitor(mergeSchema(set));
        originalExpression.accept(jVisitor);
        return jVisitor.getExpression();
    }

    /**
     * extract the union find from an expression
     * @param expression
     * @return UnionFind
     */
    public UnionFind getUnionFindFromExpression(Expression expression) {
        UnionFind unionFind = new UnionFind();
        UnionFindExpressionVisitor ufVisitor = new UnionFindExpressionVisitor(unionFind);
        expression.accept(ufVisitor);
        unionFind = ufVisitor.getUnionFind();
        return unionFind;
    }

    /**
     * given a set of table indexes
     * refine the UnionFind to only related columns
     */
    private UnionFind refineUnionFind(Set<Integer> set) {
        UnionFind ret = new UnionFind();
        Map<String, Integer> schema = mergeSchema(set);
        for(Map.Entry<Constraints, List<String>> entry : unionFind.getUnions().entrySet()) {
            List<String> newElements = new ArrayList<>();
            for (String attr : entry.getValue()) {
                if (schema.containsKey(attr)) {
                    newElements.add(attr);
                }
            }
            if (newElements.size() == 0) continue;
            for (String attr : newElements) {
                ret.createElement(attr);
                ret.setAttr(attr, entry.getKey());
            }
            for (int i = 1; i < newElements.size(); i++) {
                ret.union(newElements.get(0), newElements.get(i));
            }
        }
        return ret;
    }

}
