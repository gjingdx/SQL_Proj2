package util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This visitor help extract the left and right order 
 * from equaill the join condition
 * @author xl664 Xinhe Li
 */
public class SortJoinExpressionVisitor implements ExpressionVisitor {
    private Stack<Column> columnStack;
    private Map<String, Integer> rightSchema;
    private Map<String, Integer> leftSchema;

    /**
     * Constructor give the schema of right and left tuple
     *
     * @param Map<String, Integer>
     */
    public SortJoinExpressionVisitor(Map<String, Integer> rightSchema, Map<String, Integer> leftSchema) {
        columnStack = new Stack<>();
        this.rightSchema = rightSchema;
        this.leftSchema = leftSchema;
    }

    /**
     * extract the the Orders
     * @return a list of order. list[0] refers to the right operator, whereas list[1] to left
     */
    public List<List<OrderByElement>> getOrders() {
        List<List<OrderByElement>> ret = new ArrayList<>();
        ret.add(new ArrayList<>());
        ret.add(new ArrayList<>());
        if (columnStack.size() % 2 != 0) return ret;
        int s = columnStack.size();
        for (int i = 0; i < s; i += 2) {
            Column e1 = columnStack.pop();
            Column e2 = columnStack.pop();

            if ((rightSchema.containsKey(e1.toString()) &&
                    leftSchema.containsKey(e2.toString()))
                    ) {
                OrderByElement order1 = new OrderByElement();
                order1.setAsc(true);
                order1.setExpression(e1);

                OrderByElement order2 = new OrderByElement();
                order2.setAsc(true);
                order2.setExpression(e2);

                ret.get(1).add(order1);
                ret.get(0).add(order2);
            } else if (leftSchema.containsKey(e1.toString()) &&
                    rightSchema.containsKey(e2.toString())) {
                OrderByElement order1 = new OrderByElement();
                order1.setAsc(true);
                order1.setExpression(e1);

                OrderByElement order2 = new OrderByElement();
                order2.setAsc(true);
                order2.setExpression(e2);

                ret.get(0).add(order1);
                ret.get(1).add(order2);
            }
        }
        return ret;
    }

    /**
     * method visit the long value
     * indicates no valid for sort element
     */
    @Override
    public void visit(LongValue node) {
        columnStack.pop();
    }

    /**
     * visit method for the column node.
     * possible as an order
     *
     * @param the expression node.
     */
    @Override
    public void visit(Column node) {
        columnStack.push(node);
    }

    /**
     * method that visit the and expression node.
     * traverse the children nodes
     */
    @Override
    public void visit(AndExpression node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);
    }

    /**
     * visit method for the equals to node.
     * @param an equals to expression node.
     */
    @Override
    public void visit(EqualsTo node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);
    }

    @Override
    public void visit(OrExpression node) {
    }

    @Override
    public void visit(GreaterThan node) {
    }

    @Override
    public void visit(GreaterThanEquals node) {
    }

    @Override
    public void visit(MinorThan node) {
    }

    @Override
    public void visit(MinorThanEquals node) {
    }

    @Override
    public void visit(NotEqualsTo node) {
    }

    @Override
    public void visit(Parenthesis node) {

    }

    @Override
    public void visit(NullValue node) {


    }

    @Override
    public void visit(Function node) {


    }

    @Override
    public void visit(InverseExpression node) {


    }

    @Override
    public void visit(JdbcParameter node) {


    }

    @Override
    public void visit(DoubleValue node) {


    }

    @Override
    public void visit(DateValue node) {


    }

    @Override
    public void visit(TimeValue node) {


    }

    @Override
    public void visit(TimestampValue node) {


    }


    @Override
    public void visit(StringValue node) {


    }

    @Override
    public void visit(Addition node) {


    }

    @Override
    public void visit(Division node) {


    }

    @Override
    public void visit(Multiplication node) {


    }

    @Override
    public void visit(Subtraction node) {


    }

    @Override
    public void visit(Between node) {


    }

    @Override
    public void visit(InExpression node) {


    }

    @Override
    public void visit(IsNullExpression node) {


    }

    @Override
    public void visit(LikeExpression node) {


    }

    @Override
    public void visit(SubSelect node) {


    }

    @Override
    public void visit(CaseExpression node) {


    }

    @Override
    public void visit(WhenClause node) {


    }

    @Override
    public void visit(ExistsExpression node) {


    }

    @Override
    public void visit(AllComparisonExpression node) {


    }

    @Override
    public void visit(AnyComparisonExpression node) {


    }

    @Override
    public void visit(Concat node) {


    }

    @Override
    public void visit(Matches node) {


    }

    @Override
    public void visit(BitwiseAnd node) {


    }

    @Override
    public void visit(BitwiseOr node) {


    }

    @Override
    public void visit(BitwiseXor node) {


    }

}