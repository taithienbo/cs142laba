package types;

import java.util.ArrayList;
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
import ast.WhileLoop;
import crux.Symbol;


public class TypeChecker implements CommandVisitor 
{

	private HashMap<Command, Type> typeMap;
	
	// for performing matching arguments when calling functions 
	private ArrayList<FunctionDefinition> functions;

	private StringBuffer errorBuffer;

	// We can return static predefined types in Crux instead of
	// instantiating new objects 
	private IntType intType; 
	private FloatType floatType;
	private BoolType boolType; 
	private VoidType voidType;

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

	
	private String getInvalidVarTypeError(VariableDeclaration var)
	{
		return "Variable " + var.symbol().name()
				+ " has invalid type " + var.symbol().type() + ".";
	}
	
	
	private String getInvalidArrayTypeError(ArrayDeclaration arrayDec)
	{
		return "Array " + arrayDec.symbol().name() + " has invalid base type " 
					+ arrayDec.symbol().type() + ".";
	}

	private String getInvalidIfElseConditionError(Type unexpectedType)
	{
		return "IfElseBranch requires bool condition not " + unexpectedType + ".";
	}


	private String getInvalidWhileConditionError(Type unexpectedType)
	{
		return "Whileloop requires bool condition not " + unexpectedType + ".";
	}
	
	
	private String getNotMatchingReturn(Symbol funcName, Type expected)
	{
		return  "Function " + funcName.name() + " returns " + 
					funcName.type().toString() 
					+ " not " + expected.toString() + ".";
	}


	private String getNotAllPathReturnError(String funcName)
	{
		return "Not all paths in function " + funcName + " have a return.";
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
		functions = new ArrayList<FunctionDefinition>();
		intType = new IntType();
		floatType = new FloatType();
		boolType = new BoolType();
		voidType = new VoidType();
	}

	
	private void reportError(int lineNum, int charPos, String message)
	{
		errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
		errorBuffer.append("[" + message + "]" + "\n");
	}

	
	private void put(Command node, Type type)
	{
		if (type instanceof ErrorType) 
			reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
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
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(LiteralBool node) 
	{
		put(node, visitRetrieveType(node));
	}

	@Override
	public void visit(LiteralFloat node) 
	{
		put(node, floatType);
	}

	
	@Override
	public void visit(LiteralInt node) 
	{
		put(node, intType);
	}

	
	@Override
	public void visit(VariableDeclaration node)
	{
		// ensure variable's type is valid
				Type varType = node.symbol().type();
				
				if ((varType instanceof IntType) 
						|| (varType instanceof FloatType)
						|| (varType instanceof BoolType))
					put(node, varType);
				else
					put(node, new ErrorType(getInvalidVarTypeError(node)));
	}

	
	@Override
	public void visit(ArrayDeclaration node) 
	{
		// ensure array's type is valid
		Type arrayType = node.symbol().type();
		
		if ((arrayType instanceof IntType) 
				|| (arrayType instanceof FloatType)
				|| (arrayType instanceof BoolType))
			put(node, arrayType);
		else
			put(node, new ErrorType(getInvalidArrayTypeError(node)));
	}


	@Override
	public void visit(FunctionDefinition node)
	{
		Symbol funcSymbol = node.function();

		if (funcSymbol.name().equals("main") && 
				!(funcSymbol.type() instanceof VoidType))
			put(node, new ErrorType(getInvalidMainFunctionSignatureError()));

		if (!(funcSymbol.type() instanceof VoidType))
		{
			AllPathReturnChecker hasReturnChecker = new AllPathReturnChecker();
			node.accept(hasReturnChecker);

			if (!hasReturnChecker.hasReturn(node))
				put(node, new ErrorType
						(getNotAllPathReturnError(funcSymbol.name())));		
			else
				visitExpectCorrectReturnType(node);
		}
		else
			node.body().accept(this);
		
		put(node, funcSymbol.type());
	}
	
	
	private void visitExpectCorrectReturnType(FunctionDefinition node)
	{
		Iterator<Statement> i = node.body().iterator();
		while (i.hasNext())
		{
			Statement statement = i.next();
			statement.accept(this);
			
			if (statement instanceof Return)
			{
				Type returnType = visitRetrieveType(((Return) statement).argument());
				if (!node.function().type().equivalent(returnType))
					put((Command) statement,
							new ErrorType(getNotMatchingReturn(node.symbol(), returnType)));
			
			}
		}
	}


	@Override
	public void visit(Comparison node)
	{
		put(node, visitRetrieveType(node));
	}


	private Type visitRetrieveType(Expression e)
	{
		if (e instanceof Addition)
		{
			Addition addition = (Addition) e;
			return visitRetrieveType(addition.leftSide())
					.add(visitRetrieveType(addition.rightSide()));
		}
		else if (e instanceof AddressOf)
			return ((AddressOf) e).symbol().type();
		else if (e instanceof Call)
			return ((Call) e).function().type();
		else if (e instanceof Comparison)
		{
			Comparison comparison = (Comparison) e;
			return visitRetrieveType(comparison.leftSide())
					.compare(visitRetrieveType(comparison.rightSide()));
		}
		else if (e instanceof Dereference)
			return visitRetrieveType(((Dereference) e).expression());
		else if (e instanceof Division)
		{
			Division division = (Division) e;
			return visitRetrieveType(division.leftSide())
					.div(visitRetrieveType(division.rightSide()));
		}
		else if (e instanceof Error)
			return ((ast.Error) e).symbol().type();
		else if (e instanceof FunctionDefinition)
			return (((FunctionDefinition) e).function().type());
		else if (e instanceof IfElseBranch)
		{
			// not sure what to return here 
		}
		else if (e instanceof Index)
			return visitRetrieveType(((Index) e).base());
		else if (e instanceof LiteralBool)
			return boolType;
		else if (e instanceof LiteralFloat)
			return floatType;
		else if (e instanceof LiteralInt)
			return intType;
		else if (e instanceof LogicalAnd)
		{
			LogicalAnd logicalAnd = (LogicalAnd) e;
			return visitRetrieveType(logicalAnd.leftSide())
					.and(visitRetrieveType(logicalAnd.rightSide()));
		}
		else if (e instanceof LogicalNot)
		{
			LogicalNot logicalNot = (LogicalNot) e;
			return visitRetrieveType(logicalNot.expression()).not();
		}
		else if (e instanceof LogicalOr)
		{
			LogicalOr logicalOr = (LogicalOr) e;
			return visitRetrieveType(logicalOr.leftSide())
					.and(visitRetrieveType(logicalOr.rightSide()));
		}
		else if (e instanceof Multiplication)
		{
			Multiplication multiplication = (Multiplication) e;
			return visitRetrieveType(multiplication.leftSide())
					.mul(visitRetrieveType(multiplication.rightSide()));
		}
		else if (e instanceof Subtraction)
		{
			Subtraction subtraction = (Subtraction) e;
			return visitRetrieveType(subtraction.leftSide())
					.sub(visitRetrieveType(subtraction.rightSide()));
		}

		return null;


	}


	@Override
	public void visit(Addition node) 
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(Subtraction node)
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(Multiplication node)
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(Division node) 
	{
		put(node, visitRetrieveType(node));
	}

	@Override
	public void visit(LogicalAnd node) 
	{
		put(node, visitRetrieveType(node));;
	}


	@Override
	public void visit(LogicalOr node)
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(LogicalNot node) 
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(Dereference node)
	{
		put(node, visitRetrieveType(node));
	}


	@Override
	public void visit(Index node)
	{
		put(node, visitRetrieveType(node));
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
