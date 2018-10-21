package util;

import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.expression.operators.conditional.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * This visitor help extract related expression
 * accroding to the key of a schema hash map
 * @author xl664 Xinhe Li
 *
 */
public class SortJoinExpressionVisitor implements ExpressionVisitor {
	private Stack<Column> columnStack;
	private Map<String, Integer> rightSchema;
	private Map<String, Integer> leftSchema;
	/**
	 * Constructor give the schema of current tuple
	 * @param Map<String, Integer>
	 */
	public SortJoinExpressionVisitor(Map<String, Integer> rightSchema, Map<String, Integer> leftSchema) {
		columnStack = new Stack<>();
		this.rightSchema = rightSchema;
		this.leftSchema = leftSchema;
	}
	
	/** 
	 * extract the final expression
	 * @return the expression"
	 */
	public List<List<OrderByElement>> getOrders(){
		List<List<OrderByElement>> ret = new ArrayList<>();
		ret.add(new ArrayList<>());
		ret.add(new ArrayList<>());
		if(columnStack.size() % 2!=0)return ret;
		int s = columnStack.size();
		for(int i = 0; i<s; i+=2){
			Column e1 = columnStack.pop();
			Column e2 = columnStack.pop();

			if((rightSchema.containsKey(e1.toString())&&
				leftSchema.containsKey(e2.toString()))
			){
				OrderByElement order1 = new OrderByElement();
				order1.setAsc(true);
				order1.setExpression(e1);
				
				OrderByElement order2 = new OrderByElement();
				order2.setAsc(true);
				order2.setExpression(e2);

				ret.get(1).add(order1);
				ret.get(0).add(order2);
			}
			else if(leftSchema.containsKey(e1.toString())&&
			rightSchema.containsKey(e2.toString())){
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
	 * method visit the long value.
	 * push a true value on the second stack since this node
	 * indicates a cross product.
	 */
	@Override
	public void visit(LongValue node) {
		columnStack.pop();
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
		columnStack.push(node);
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
	}

	/**
	 * method that visit the OR expression node.
	 * only wehn left expression and right expression both are valid
	 * return the node, else return null
	 */
	@Override
	public void visit(OrExpression node) {	
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

	/**
	 * visit method for the greater than node.
	 * @param an greater expression node.
	 */
	@Override
	public void visit(GreaterThan node) {
	}

	/**
	 * visit method for the greater than equals node.
	 * @param an greater than equals expression node.
	 */
	@Override
	public void visit(GreaterThanEquals node) {
	}

	/**
	 * visit method for the minor than node.
	 * @param a minor than expression node.
	 */
	@Override
	public void visit(MinorThan node) {
	}

	/**
	 * visit method for the minor than equals node.
	 * @param a minor than equals expression node.
	 */
	@Override
	public void visit(MinorThanEquals node) {
	}

	/**
	 * visit method for the equals to node.
	 * @param an not equals to expression node.
	 */
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