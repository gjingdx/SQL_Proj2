package util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import model.Tuple;

import java.util.*;


public class SelectExpressionVisitor implements ExpressionVisitor {

    private Map<String, Integer> currentSchema;
    private Deque<Long> data;
    private Deque<Boolean> values;
    private Tuple currentTuple;

    public SelectExpressionVisitor(Tuple tuple, Map<String, Integer> schema) {
        currentSchema = schema;
        data = new LinkedList<>();
        values = new LinkedList<>();
        currentTuple = tuple;
    }

    /**
     * @return result of the expression
     */
    public boolean getResult() {
        if (values.isEmpty()) {
            return true;
        }
        return values.peekFirst();
    }

    @Override
    public void visit(AndExpression andExpression) {
        // Todo
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
        boolean rightValue = values.removeFirst();
        boolean leftValue = values.removeFirst();
        values.addFirst(leftValue && rightValue);
    }

    @Override
    public void visit(Column column) {
        // Todo
        String columnName = column.getWholeColumnName();
        // int ind = catalog.getIndexOfColumn(columnName);
        int ind = currentSchema.get(columnName);
        data.push(currentTuple.getDataAt(ind));
    }

    @Override
    public void visit(LongValue longValue) {
        // Todo
        data.push(longValue.getValue());
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        // Todo
        equalsTo.getLeftExpression().accept(this);
        equalsTo.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue == rightValue);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        // Todo
        notEqualsTo.getLeftExpression().accept(this);
        notEqualsTo.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue != rightValue);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        // Todo
        greaterThan.getLeftExpression().accept(this);
        greaterThan.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue > rightValue);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        // todo
        greaterThanEquals.getLeftExpression().accept(this);
        greaterThanEquals.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue >= rightValue);

    }

    @Override
    public void visit(MinorThan minorThan) {
        // Todo
        minorThan.getLeftExpression().accept(this);
        minorThan.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue < rightValue);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        // Todo
        minorThanEquals.getLeftExpression().accept(this);
        minorThanEquals.getRightExpression().accept(this);
        long rightValue = data.removeFirst();
        long leftValue = data.removeFirst();
        values.addFirst(leftValue <= rightValue);
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
}
