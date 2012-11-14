package types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import ast.Addition;
import ast.AddressOf;
import ast.ArrayDeclaration;
import ast.Assignment;
import ast.Call;
import ast.Command;
import ast.CommandVisitor;
import ast.Comparison;
import ast.Declaration;
import ast.DeclarationList;
import ast.Dereference;
import ast.Division;
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
import ast.Visitable;
import ast.WhileLoop;
import crux.Symbol;


public class TypeChecker implements CommandVisitor 
{

	private HashMap<Command, Type> typeMap;
	private StringBuffer errorBuffer;

	/* Useful error strings:
	 *
	 * "Function " + func.name() + " has a void argument in position " + pos + "."
	 * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
	 *
	 * "Function main has invalid signature."
	 *
	 * "Not all paths in function " + currentFunctionName + " have a return."
	 *
	 * "IfElseBranch requires bool condition not " + condType + "."
	 * "WhileLoop requires bool condition not " + condType + "."
	 *
	 * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
	 *
	 * "Variable " + varName + " has invalid type " + varType + "."
	 * "Array " + arrayName + " has invalid base type " + baseType + "."
	 */


	private String getInvalidMainFunctionSignatureError()
	{
		return "Function main has invalid signature.";
	}


	private String getIncorrectFunctionReturnType(Symbol func, Type unexpectedReturnType)
	{
		return "Function " + func + " returns " + unexpectedReturnType + " not " + func.type() + ".";
	}


	private String getInvalidIfElseConditionError(Type unexpectedType)
	{
		return "IfElseBranch requires bool condition not " + unexpectedType + ".";
	}


	private String getInvalidWhileConditionError(Type unexpectedType)
	{
		return "Whileloop requires bool condition not " + unexpectedType + ".";
	}



	private TypeList getTypeListFromSymbols(List<Symbol> symbols)
	{
		TypeList typeList = new TypeList();
		for (Symbol s : symbols)
			typeList.add(s.type());
		return typeList;
	}




	public TypeChecker()
	{
		typeMap = new HashMap<Command, Type>();
		errorBuffer = new StringBuffer();
	}

	private void reportError(int lineNum, int charPos, String message)
	{
		errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
		errorBuffer.append("[" + message + "]" + "\n");
	}

	private void put(Command node, Type type)
	{
		if (type instanceof ErrorType) 
		{
			reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
		}
		typeMap.put(node, type);
	}

	public Type getType(Command node)
	{
		return typeMap.get(node);
	}

	public boolean check(Command ast)
	{
		ast.accept(this);
		return !hasError();
	}

	public boolean hasError()
	{
		return errorBuffer.length() != 0;
	}

	public String errorReport()
	{
		return errorBuffer.toString();
	}

	@Override
	public void visit(ExpressionList node) 
	{
		Iterator<Expression> i = node.iterator();
		while (i.hasNext())
		{
			Expression exp = i.next();
			exp.accept(this);
		}
	}

	@Override
	public void visit(DeclarationList node) 
	{
		Iterator<Declaration> i = node.iterator();
		while (i.hasNext())
		{
			Declaration declaration = i.next();
			declaration.accept(this);
		}
	}

	@Override
	public void visit(StatementList node)
	{
		Iterator<Statement> i = node.iterator();
		while (i.hasNext())
		{
			Statement statement = i.next();
			statement.accept(this);
		}
	}


	@Override
	public void visit(AddressOf node) 
	{
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(LiteralBool node) 
	{
		put(node, new BoolType());
	}

	@Override
	public void visit(LiteralFloat node) 
	{
		put(node, new FloatType());
	}

	@Override
	public void visit(LiteralInt node) 
	{
		put(node, new IntType());
	}

	@Override
	public void visit(VariableDeclaration node)
	{

		put(node, node.symbol().type());
	}

	@Override
	public void visit(ArrayDeclaration node) 
	{
		put(node, node.symbol().type());
	}

	@Override
	public void visit(FunctionDefinition node)
	{
		Symbol funcSymbol = node.function();

		if (funcSymbol.name().equals("main") && !(funcSymbol.type() instanceof VoidType))
			put(node, new ErrorType(getInvalidMainFunctionSignatureError()));
		else 
			put(node, getTypeListFromSymbols(node.arguments()));

		// otherwise, check that the function actually returns correct type in
		// all possible paths 
		// check the current level/scope, if there is no return statement
		// then check if there is an if statement, if so there must be a 
		// non-empty else statement, otherwise report that the function
		// needs to return a result 
		visitExpectCorrectReturnType(node);		
	}


	private Type visitGetIfElseReturnType(IfElseBranch node)
	{

	}


	private void visitExpectCorrectReturnType(FunctionDefinition node)
	{

		Iterator<Statement> i = node.body().iterator();
		while (i.hasNext())
		{
			Statement statement = i.next();
			if (statement instanceof IfElseBranch)
			{
				Type ifElseReturnType = visitGetIfElseReturnType
						((IfElseBranch)statement);
				if (ifElseReturnType == null)
				{
					if (! i.hasNext())
					{
						// report that function is missing a return type
					}
				}
				else
				{
					if (!ifElseReturnType.equivalent(node.function().type()))
							{
								// report that function returns incorrect type
							}
				}
			}
			// continue parsing other statements 
		}
	}


	@Override
	public void visit(Comparison node)
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.compare(rightType));
	}

	@Override
	public void visit(Addition node) 
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.add(rightType));
	}

	@Override
	public void visit(Subtraction node)
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.sub(rightType));
	}

	@Override
	public void visit(Multiplication node)
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.mul(rightType));
	}

	@Override
	public void visit(Division node) 
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.div(rightType));
	}

	@Override
	public void visit(LogicalAnd node) 
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.and(rightType));
	}

	@Override
	public void visit(LogicalOr node)
	{
		Type leftType = visitRetrieveType(node.leftSide());
		Type rightType = visitRetrieveType(node.rightSide());

		put(node, leftType.or(rightType));
	}

	@Override
	public void visit(LogicalNot node) 
	{
		put(node, new BoolType());
	}

	@Override
	public void visit(Dereference node)
	{
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Index node)
	{
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Assignment node) 
	{
		Type source = visitRetrieveType(node.source());
		Type des = visitRetrieveType(node.destination());

		put(node, des.assign(source));
	}

	@Override
	public void visit(Call node) 
	{
		// check that call actually calls an existing function, and the arguments 
		// match with the function's signature 	

		visitExpectMatchingArguments(getFunctionDefinition(node.function()), node.arguments());

	}


	private void visitExpectMatchingArguments(FunctionDefinition function, ExpressionList arguments)
	{
		Type functionType = getType(function);
		if (functionType instanceof TypeList)
		{
			Iterator<Type> i = ((TypeList) functionType).iterator();
			//	while(i.hasNext())
			//	{

			//	}
		}

	}


	private FunctionDefinition getFunctionDefinition(Symbol function)
	{		
		for (Command command : typeMap.keySet())
		{
			if (command instanceof FunctionDefinition 
					&& ((FunctionDefinition) command).function() == function)
				return (FunctionDefinition) command;
		}

		return null;
	}

	@Override
	public void visit(IfElseBranch node) 
	{
		Type condition = visitRetrieveType(node.condition());

		if(!(condition instanceof BoolType))
			put(node, new ErrorType(getInvalidIfElseConditionError(condition)));

		node.thenBlock().accept(this);
		node.elseBlock().accept(this);

	}

	@Override
	public void visit(WhileLoop node) 
	{
		Type condition = visitRetrieveType(node.condition());
		if (!(condition instanceof BoolType))
			put(node, new ErrorType(getInvalidWhileConditionError(condition)));
		node.body().accept(this);
	}

	@Override
	public void visit(Return node) 
	{
		node.argument().accept(this);
	}

	@Override
	public void visit(ast.Error node) 
	{
		put(node, new ErrorType(node.message()));
	}


}
