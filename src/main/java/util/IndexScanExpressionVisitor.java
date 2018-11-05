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
 *
 * @author xl664 Xinhe Li
 */
public class IndexScanExpressionVisitor implements ExpressionVisitor {
    private Stack<Column> columnStack;
    private Stack<Long> longStack;
    private String columnName;
    private String tableName;
    private int highKey;
    private int lowKey;

    /**
     * Constructor give the schema of right and left tuple
     *
     * @param rightSchema
     */
    public IndexScanExpressionVisitor(String tableName, String columnName) {
        columnStack = new Stack<>();
        longStack = new Stack<>();
        this.columnName = columnName;
        this.tableName = tableName;
        this.highKey = Integer.MIN_VALUE;
        this.lowKey = Integer.MAX_VALUE;
    }

    private boolean isValidColumn(Column c) {
        String cTableName = Catalog.getInstance().getTableNameFromAlias(c.getTable().toString());
        String cColumnName = c.getColumnName().toString();
        return cTableName.equals(tableName)
            && cColumnName.equals(columnName);
    }

    /**
     * extract the the Orders
     *
     * @return a list of order. list[0] refers to the right operator, whereas list[1] to left
     */
    public int getHighKey() {
        if (highKey == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return this.highKey;
    }

    public int getLowKey() {
        if (lowKey == Integer.MAX_VALUE) {
            return Integer.MIN_VALUE;
        }
        return this.lowKey;
    }

    public boolean isValid() {
        return !(highKey == Integer.MIN_VALUE && lowKey == Integer.MAX_VALUE);
    }

    /**
     * method visit the long value
     * indicates no valid for sort element
     */
    @Override
    public void visit(LongValue node) {
        longStack.push(node.getValue());
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
     *
     * @param an equals to expression node.
     */
    @Override
    public void visit(EqualsTo node) {
    }

    @Override
    public void visit(OrExpression node) {
    }

    @Override
    public void visit(GreaterThan node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);

        if (columnStack.size() == 2) {
            columnStack.pop();
            columnStack.pop();
        }
        else if (columnStack.size() == 1) {
            Column c = columnStack.pop();
            long value = longStack.pop();
            if (isValidColumn(c)) {
                this.lowKey = Math.min(lowKey, (int) (value + 1));
            }
        }
        else {
            longStack.pop();
            longStack.pop();
        }
    }

    @Override
    public void visit(GreaterThanEquals node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);

        if (columnStack.size() == 2) {
            columnStack.pop();
            columnStack.pop();
        }
        else if (columnStack.size() == 1) {
            Column c = columnStack.pop();
            long value = longStack.pop();
            if (isValidColumn(c)) {
                this.lowKey = Math.min(lowKey, (int) (value + 0));
            }
        }
        else {
            longStack.pop();
            longStack.pop();
        }
    }

    @Override
    public void visit(MinorThan node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);

        if (columnStack.size() == 2) {
            columnStack.pop();
            columnStack.pop();
        }
        else if (columnStack.size() == 1) {
            Column c = columnStack.pop();
            long value = longStack.pop();
            if (isValidColumn(c)) {
                this.highKey = Math.max(highKey, (int) (value - 1));
            }
        }
        else {
            longStack.pop();
            longStack.pop();
        }
    }

    @Override
    public void visit(MinorThanEquals node) {
        node.getLeftExpression().accept(this);
        node.getRightExpression().accept(this);

        if (columnStack.size() == 2) {
            columnStack.pop();
            columnStack.pop();
        }
        else if (columnStack.size() == 1) {
            Column c = columnStack.pop();
            long value = longStack.pop();
            if (isValidColumn(c)) {
                this.highKey = Math.max(highKey, (int) (value - 0));
            }
        }
        else {
            longStack.pop();
            longStack.pop();
        }
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