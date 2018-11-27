package util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import util.unionfind.UnionFind;

import java.util.Deque;
import java.util.LinkedList;


/**
 * An implementation of ExpressionVisitor
 * traverse the expression tree, union two columns when encountering
 * equalto, update Constraints when encountering = <= >= > <
 *
 * @author Yufu Mo ym445
 */
public class UnionFindExpressionVisitor implements ExpressionVisitor {

    private Deque<Object> stack;
    private UnionFind unionFind;

    public UnionFindExpressionVisitor(UnionFind unionFind) {
        stack = new LinkedList<>();
        this.unionFind = unionFind;
    }

    /**
     * visit method for AndExpression expression
     * 1. left and right sides accepts the visitor
     * 2. get the right and left results accordingly (because of stack sequence)
     * 3. push the evaluation of the expression using the results of the two sides
     *
     * @param andExpression
     */
    @Override
    public void visit(AndExpression andExpression) {
        // Todo
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
    }

    /**
     * visit method for Column expression
     * by getting the data in the current tuple of the certain column
     * and pushing it to data stack.
     *
     * @param column
     */
    @Override
    public void visit(Column column) {
        // Todo
        String columnName = column.getWholeColumnName();
        stack.push(columnName);
    }

    /**
     * visit method for LongValue expression
     * by pushing the long value of the expression to the data stack
     *
     * @param longValue
     */
    @Override
    public void visit(LongValue longValue) {
        // Todo
        stack.push(longValue.getValue());
    }

    /**
     * visit method for EqualsTo expression
     * Union the left and right column or update the equality constraints
     * @param equalsTo
     */
    @Override
    public void visit(EqualsTo equalsTo) {
        // Todo
        equalsTo.getLeftExpression().accept(this);
        equalsTo.getRightExpression().accept(this);
        Object right = stack.pop();
        Object left = stack.pop();
        if (!(left instanceof String)) {
            return;
        }
        if (right instanceof String) {
            unionFind.union((String) right, (String) left);
        }
        else if (right instanceof Long) {
            String col = (String) left;
            int val = (int) ((Long) right).longValue();
            unionFind.find(col).setEquality(val);
        }
    }

    /**
     * visit method for NotEqualsTo expression
     *
     * @param notEqualsTo
     */
    @Override
    public void visit(NotEqualsTo notEqualsTo) {
    }

    /**
     * visit method for GreaterThan expression
     * update the constraints
     *
     * @param greaterThan
     */
    @Override
    public void visit(GreaterThan greaterThan) {
        // Todo
        greaterThan.getLeftExpression().accept(this);
        greaterThan.getRightExpression().accept(this);
        Object right = stack.pop();
        Object left = stack.pop();
        if (right instanceof Long) {
            String col = (String) left;
            int val = (int) ((Long) right).longValue();
            unionFind.find(col).setLowerBound(val + 1);
        }
    }

    /**
     * visit method for GreaterThanEquals expression
     * update the constraints
     *
     * @param greaterThanEquals
     */
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        // todo
        greaterThanEquals.getLeftExpression().accept(this);
        greaterThanEquals.getRightExpression().accept(this);
        Object right = stack.pop();
        Object left = stack.pop();
        if (right instanceof Long) {
            String col = (String) left;
            int val = (int) ((Long) right).longValue();
            unionFind.find(col).setLowerBound(val);
        }

    }

    /**
     * visit method for MinorThan expression
     * update the constraints
     *
     * @param minorThan
     */
    @Override
    public void visit(MinorThan minorThan) {
        // Todo
        minorThan.getLeftExpression().accept(this);
        minorThan.getRightExpression().accept(this);
        Object right = stack.pop();
        Object left = stack.pop();
        if (right instanceof Long) {
            String col = (String) left;
            int val = (int) ((Long) right).longValue();
            unionFind.find(col).setUpperBound(val - 1);
        }
    }

    /**
     * visit method for MinorThanEquals expression
     * update the constraints
     *
     * @param minorThanEquals
     */
    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        // Todo
        minorThanEquals.getLeftExpression().accept(this);
        minorThanEquals.getRightExpression().accept(this);
        Object right = stack.pop();
        Object left = stack.pop();
        if (right instanceof Long) {
            String col = (String) left;
            int val = (int) ((Long) right).longValue();
            unionFind.find(col).setUpperBound(val);
        }
    }


    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {

    }

    @Override
    public void visit(InverseExpression inverseExpression) {

    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {

    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Addition addition) {

    }

    @Override
    public void visit(Division division) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }

    @Override
    public void visit(Between between) {

    }

    @Override
    public void visit(InExpression inExpression) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {

    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(CaseExpression caseExpression) {

    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {

    }

    @Override
    public void visit(Matches matches) {

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

    }

    public UnionFind getUnionFind() {
        return unionFind;
    }
}
