package util;

import logical.operator.JoinOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.unionfind.Constraints;
import util.unionfind.UnionFind;

import java.io.StringReader;
import java.util.*;

public class ExpressionBuilder {
    public static Expression buildExpressionFromString(String statement, PlainSelect plainSelect) {
        String fromItem = plainSelect.getFromItem().toString();
        String joinItems = "";
        if (plainSelect.getJoins() != null) {
            joinItems = plainSelect.getJoins().toString();
            joinItems = joinItems.substring(1, joinItems.length() - 1);
            joinItems = "," + joinItems;
        }
        String newStatement = "Select * from " + fromItem + joinItems + " where " + statement + ";";
        PlainSelect newPlainSelect = new PlainSelect();
        try {
            CCJSqlParserManager parserManager = new CCJSqlParserManager();
            newPlainSelect = (PlainSelect) ((Select) parserManager.
                    parse(new StringReader(newStatement))).getSelectBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newPlainSelect.getWhere();
    }

    public static Expression buildExpressionFromUnionFind(UnionFind unionFind, Map<String, Integer> leftSchema, Map<String, Integer> rightSchema, PlainSelect plainSelect) {
        String expressionString = "";
        for(Map.Entry<Constraints, List<String>> entry : unionFind.getUnions().entrySet()) {
            List<String> rightElements = new ArrayList<>();
            List<String> leftElements = new ArrayList<>();
            for (String attr : entry.getValue()) {
                if (leftSchema.containsKey(attr)) {
                    leftElements.add(attr);
                } else if (rightSchema.containsKey(attr)) {
                    rightElements.add(attr);
                }
                if (rightElements.size() > 0 && leftElements.size() > 0) {
                    break;
                }
            }
            if (rightElements.size() > 0 && leftElements.size() > 0) {
                expressionString += " AND " + rightElements.get(0) + " = " + leftElements.get(0);
            }
        }
        if (expressionString.length() == 0) {
            return null;
        }
        expressionString = expressionString.substring(5);
        return buildExpressionFromString(expressionString, plainSelect);
    }

    public static Expression buildExpressionFromTwoChildrenLogicalJoin(JoinOperator op) {
        if (op.getChildren().size() != 2) {
            return null;
        }
        PlainSelect plainSelect = op.getPlainSelect();
        if (plainSelect.getWhere() == null) {
            return null;
        }
        Map<String, Integer> leftSchema = op.getChildren().get(0).getSchema();
        Map<String, Integer> rightSchema = op.getChildren().get(1).getSchema();

        Expression equalExpression = buildExpressionFromUnionFind(op.getUnionFind(), leftSchema, rightSchema, plainSelect);

        JoinConditionExpressionVisitor joinExpressionVisitor = new JoinConditionExpressionVisitor(op.getSchema());
        plainSelect.getWhere().accept(joinExpressionVisitor);
        Expression otherExpression = joinExpressionVisitor.getExpression();
        if (otherExpression == null && equalExpression != null) {
            return equalExpression;
        }
        if (otherExpression != null && equalExpression == null) {
            return otherExpression;
        }
        if (otherExpression == null && equalExpression == null) {
            return null;
        }
        return new AndExpression(otherExpression, equalExpression);
    }
}
