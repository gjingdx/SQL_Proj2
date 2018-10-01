package util;

import java.util.Map;
import java.util.Stack;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * This visitor help extract related expression
 * accroding to the key of a schema hash map
 * @author xl664 Xinhe Li
 *
 */
public class JoinExpressionVisitor implements ExpressionVisitor {
	private Stack<Expression> expressionStack;

	private Map<String, Integer> schemaMap;
	
	/**
	 * Constructor give the schema of current tuple
	 * @param Map<String, Integer>
	 */
	public JoinExpressionVisitor(Map<String, Integer> schemaMap) {
		expressionStack = new Stack<>();
		this.schemaMap = schemaMap;
	}
	
	/** 
	 * extract the final expression
	 * @return the expression"
	 */
	public Expression getExpression(){
		if(expressionStack.size()==1){
			return expressionStack.peek();
		}
		else{
			return null;
		}
	}

	/**
	 * method visit the long value.
	 * push a true value on the second stack since this node
	 * indicates a cross product.
	 */
	@Override
	public void visit(LongValue node) {
		
		expressionStack.push(node);
	}

	/**
	 * visit method for the column node.
	 * judge if the column is in the key set of the schema
	 * if true push the node into the stack
	 * else push null
	 * @param the expression node.
	 */
	@Override
	public void visit(Column node) {
		String field = node.getWholeColumnName();
		String fieldSplit[] = field.split("\\.");
		if(fieldSplit.length == 2){
			// Sailers.A
			String tableAlias = fieldSplit[0], fieldName = fieldSplit[1];
			String schemaKey = tableAlias+'.'+fieldName;
			if(schemaMap.containsKey(schemaKey)){
				expressionStack.push(node);
			}
			else{
				expressionStack.push(null);
			}

		}
		else if(fieldSplit.length == 1){
			// A
			// need to find which table the field belongs to
			String schemaKey = fieldSplit[0] + '.' + fieldSplit[0];
			if(schemaMap.containsKey(schemaKey)){
				expressionStack.push(node);
			}
			else{
				expressionStack.push(null);
			}
		}
	}

	/**
	 * method that visit the and expression node.
	 * if both left and right expression is valid, show this expression valid
	 * if only one of them valid, only remain that expression
	 * if both invalid, return null
	 */
	@Override
	public void visit(AndExpression node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft != null && expRight != null){
			Expression temp = new AndExpression(expLeft, expRight);
			expressionStack.push(temp);
		}
		else if(expLeft != null){
			expressionStack.push(expLeft);
		}
		else if(expRight != null){
			expressionStack.push(expRight);
		}
		else{
			expressionStack.push(null);
		}

	}

	/**
	 * method that visit the OR expression node.
	 * only wehn left expression and right expression both are valid
	 * return the node, else return null
	 */
	@Override
	public void visit(OrExpression node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft != null && expRight != null){
			Expression temp = new AndExpression(expLeft, expRight);
			expressionStack.push(temp);
		}
		else{
			expressionStack.push(null);
		}
		
	}

	/**
	 * visit method for the equals to node.
	 * @param an equals to expression node.
	 */
	@Override
	public void visit(EqualsTo node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
	}

	/**
	 * visit method for the greater than node.
	 * @param an greater expression node.
	 */
	@Override
	public void visit(GreaterThan node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
	}

	/**
	 * visit method for the greater than equals node.
	 * @param an greater than equals expression node.
	 */
	@Override
	public void visit(GreaterThanEquals node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
	}

	/**
	 * visit method for the minor than node.
	 * @param a minor than expression node.
	 */
	@Override
	public void visit(MinorThan node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
	}

	/**
	 * visit method for the minor than equals node.
	 * @param a minor than equals expression node.
	 */
	@Override
	public void visit(MinorThanEquals node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
	}

	/**
	 * visit method for the equals to node.
	 * @param an not equals to expression node.
	 */
	@Override
	public void visit(NotEqualsTo node) {
		node.getLeftExpression().accept(this);
		node.getRightExpression().accept(this);

		Expression expLeft = expressionStack.pop(), expRight = expressionStack.pop();
		if(expLeft!=null && expRight!=null){
			expressionStack.push(node);
		}
		else{
			expressionStack.push(null);
		}
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