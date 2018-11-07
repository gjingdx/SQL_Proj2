package util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.Stack;

/**
 * This visitor help extract the low high key
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

    private ArrayList<Integer> equalKey;

    /**
     * Constructor give the columnName and tableName
     *
     * @param tableName
     * @param columnName
     */
    public IndexScanExpressionVisitor(String tableName, String columnName) {
        columnStack = new Stack<>();
        longStack = new Stack<>();
        equalKey = new ArrayList<>();
        this.columnName = columnName;
        this.tableName = tableName;
        this.highKey = Integer.MIN_VALUE;
        this.lowKey = Integer.MAX_VALUE;
    }

    /**
     * judge whether a column is the index column
     * 
     * @param c
     * @return boolean
     */
    private boolean isValidColumn(Column c) {
        String cTableName = Catalog.getInstance().getTableNameFromAlias(c.getTable().toString());
        String cColumnName = c.getColumnName().toString();
        return cTableName.equals(tableName)
            && cColumnName.equals(columnName);
    }

    /**
     * get the high key
     *
     * @return MAX_INT if none
     */
    public int getHighKey() {
        if (!equalKey.isEmpty()) {
            return equalKey.get(0);
        }
        if (highKey == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return this.highKey;
    }

    /**
     * get the low key
     * 
     * @return MIN_INT if none
     */
    public int getLowKey() {
        if (!equalKey.isEmpty()) {
            return equalKey.get(0);
        }
        if (lowKey == Integer.MAX_VALUE) {
            return Integer.MIN_VALUE;
        }
        return this.lowKey;
    }

    /**
     * whether has valid low and high key
     * 
     * @return boolean
     */
    public boolean isValid() {
        return !(highKey == Integer.MIN_VALUE && lowKey == Integer.MAX_VALUE && equalKey.isEmpty());
    }

    /**
     * method visit the long value
     */
    @Override
    public void visit(LongValue node) {
        longStack.push(node.getValue());
    }

    /**
     * visit method for the column node.
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
                this.equalKey.add((int) (value));
            }
        }
        else {
            longStack.pop();
            longStack.pop();
        }
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