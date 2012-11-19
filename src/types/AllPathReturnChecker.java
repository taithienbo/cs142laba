package types;

import java.util.HashMap;
import java.util.Iterator;

import ast.Addition;
import ast.AddressOf;
import ast.ArrayDeclaration;
import ast.Assignment;
import ast.Call;
import ast.Command;
import ast.CommandVisitor;
import ast.Comparison;
import ast.DeclarationList;
import ast.Dereference;
import ast.Division;
import ast.Error;
import ast.Expression;
import ast.ExpressionList;
import ast.FunctionDefinition;
import ast.IfElseBranch;
import ast.Index;
import ast.LiteralBool;
import ast.LiteralFloat;
import ast.LiteralInt;
import ast.LogicalAnd;
import ast.LogicalNot;
import ast.LogicalOr;
import ast.Multiplication;
import ast.Return;
import ast.Statement;
import ast.StatementList;
import ast.Subtraction;
import ast.VariableDeclaration;
import ast.WhileLoop;

public class AllPathReturnChecker implements CommandVisitor
{
	private HashMap<Command, Boolean> allPathReturnMap;
	
	public AllPathReturnChecker()
	{
		allPathReturnMap = new HashMap<Command, Boolean>();
	}
	
	@Override
	public void visit(ExpressionList node) 
	{
		Iterator<Expression> i = node.iterator();
		while (i.hasNext())
			i.next().accept(this);
	}

	public void put(Command node, Boolean value)
	{
		allPathReturnMap.put(node, value);
	}
	
	
	@Override
	public void visit(DeclarationList node) 
	{
		put(node, false);
	}

	@Override
	public void visit(StatementList node) 
	{
		Iterator<Statement> i = node.iterator();
		while (i.hasNext())
			i.next().accept(this);
	}

	@Override
	public void visit(AddressOf node) 
	{
		put(node, false);
	}
	
	@Override
	public void visit(LiteralBool node) 
	{
		put(node, false);	
	}

	@Override
	public void visit(LiteralFloat node) 
	{
		put(node, false);
	}

	@Override
	public void visit(LiteralInt node) 
	{
		put(node, false);	
	}

	@Override
	public void visit(VariableDeclaration node) 
	{
		put(node, false);
	}
	
	@Override
	public void visit(ArrayDeclaration node)
	{
		put(node, false);
		
	}

	public boolean hasReturn(Command node)
	{
		return allPathReturnMap.get(node);
	}
	
	@Override
	public void visit(FunctionDefinition node) 
	{
		put(node, visitRetrieveHasReturn(node));
	}
	
	private boolean visitRetrieveHasReturn(FunctionDefinition funcDef)
	{
		Iterator<Statement> i = funcDef.body().iterator();
		while (i.hasNext())
			if (visitRetrieveHasReturn(i.next()))
				return true;
		return false;
	}
	
	private boolean visitRetrieveHasReturn(Statement statement)
	{
		return statement instanceof Return;
	}
	
	private boolean visitRetrieveHasReturn(StatementList statementList)
	{
		Iterator<Statement> i = statementList.iterator();
		while (i.hasNext())
			if (visitRetrieveHasReturn(i.next()))
				return true;
		return false;
	}
	
	private boolean visitRetrieveHasReturn(IfElseBranch ifElse)
	{
		if (visitRetrieveHasReturn(ifElse.thenBlock()))
			return true;
		return visitRetrieveHasReturn(ifElse.thenBlock());
	}
	
	@Override
	public void visit(Addition node) 
	{
		put(node, false);	
	}

	@Override
	public void visit(Subtraction node) 
	{
		put(node, false);
	}

	@Override
	public void visit(Multiplication node) 
	{
		put(node, false);
	}

	@Override
	public void visit(Division node)
	{
		put(node, false);
	}

	@Override
	public void visit(LogicalAnd node) 
	{
		put(node, false);
	}

	@Override
	public void visit(LogicalOr node) 
	{
		put(node, false);
	}

	@Override
	public void visit(LogicalNot node) 
	{
		put(node, false);
	}

	@Override
	public void visit(Comparison node)
	{
		put(node, false);	
	}

	@Override
	public void visit(Dereference node) 
	{
		put(node, false);	
	}

	@Override
	public void visit(Index node) 
	{
		put(node, false);	
	}

	@Override
	public void visit(Assignment node) 
	{
		put(node, false);
	}

	@Override
	public void visit(Call node)
	{
		put(node, false); 
	}

	@Override
	public void visit(IfElseBranch node) 
	{
		put(node, visitRetrieveHasReturn(node));
	}

	
	@Override
	public void visit(WhileLoop node) 
	{
		put(node, visitRetrieveHasReturn(node.body()));
	}

	@Override
	public void visit(Return node) 
	{
		put(node, true);
	}

	@Override
	public void visit(Error node)
	{
		put(node, false);
		
	}
}
