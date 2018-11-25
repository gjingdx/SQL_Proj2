package PlanBuilder;

import logical.operator.Operator;
import logical.operator.StatVisitor;
import model.ColumnStat;
import model.TableStat;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import util.JoinExpressionVisitor;
import util.UnionFindExpressionVisitor;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.lang.reflect.Array;
import java.util.*;

public class JoinOrder {
    List<Operator> joinChildren;
    Map<Set<Integer>, OrderInfo> store;
    List<Set<String>> childrenSchemaKeys;
    PlainSelect plainSelect;
    List<TableStat> tableStats;
    Map<String, Integer> columnToTableId;

    public JoinOrder(List<Operator> joinChildren, PlainSelect plainSelect) {
        this.joinChildren = new ArrayList<>(joinChildren);
        this.plainSelect = plainSelect;
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

    public int getProducedCount (Set<Integer> set) {
        double numerator = 1.0;
        for (int i : set) {
            numerator *= tableStats.get(i).getCount();
        }

        double denominator = 1.0;
        Expression expression = getRelatedExpression(set, plainSelect.getWhere());
        if (expression == null) {
            return (int)numerator;
        }
        UnionFind unionFind = getUnionFindFromExpression(expression);
        for(Map.Entry<Constraints, List<String>> entry : unionFind.getUnions().entrySet()) {
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
        return (int)Math.ceil(numerator / denominator);
        //return (t1.count * t2.count) / Math.max(getV(t1), getV(t2));
    }

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
            int producedCount = getProducedCount(set);
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

    public class OrderInfo {
        public int cost;
        public ArrayList<Integer> order;
        public OrderInfo(int cost, ArrayList<Integer> order) {
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

    private UnionFind getUnionFindFromExpression(Expression expression) {
        UnionFind unionFind = new UnionFind();
        UnionFindExpressionVisitor ufVisitor = new UnionFindExpressionVisitor(unionFind);
        expression.accept(ufVisitor);
        unionFind = ufVisitor.getUnionFind();
        return unionFind;
    }
}
