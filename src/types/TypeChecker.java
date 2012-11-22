package types;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
import crux.SymbolTable;


public class TypeChecker implements CommandVisitor 
{
	private HashMap<Command, Type> typeMap;

	// for performing matching arguments when calling functions 
	private LinkedHashMap<Symbol, Type> functions;

	private StringBuffer errorBuffer;

	// We can return static predefined types in Crux instead of
	// instantiating new objects 
	private IntType intType; 
	private FloatType floatType;
	private BoolType boolType; 

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

	private String getHasInvalidArgument(Symbol func, int pos, ErrorType error)
	{
		return "Function " + func.name() 
				+ " has an error in argument in position " 
				+ pos + ": " + error.getMessage();
	}
	
	private String getHasVoidArgumentError(Symbol func, int pos)
	{
		return "Function " + func.name() 
				+ " has a void argument in position " + pos + ".";
	}
	
	private String getInvalidMainFunctionSignatureError()
	{
		return "Function main has invalid signature.";
	}

	private String getInvalidArrayBaseTypeError(String name, Type base)
	{
		return  "Array " + name + " has invalid base type " + base + ".";
	}

	private String getInvalidVarTypeError(VariableDeclaration var)
	{
		return "Variable " + var.symbol().name()
				+ " has invalid type " + var.symbol().type() + ".";
	}

	private String getInvalidIfElseConditionError(Type unexpectedType)
	{
		return "IfElseBranch requires bool condition not " + unexpectedType + ".";
	}

	private String getInvalidWhileConditionError(Type unexpectedType)
	{
		return "WhileLoop requires bool condition not " + unexpectedType + ".";
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
			typeList.append(s.type());

		return typeList;
	}

	public TypeChecker()
	{
		typeMap = new HashMap<Command, Type>();
		errorBuffer = new StringBuffer();

		functions = new LinkedHashMap<Symbol, Type>();
		intType = new IntType();
		floatType = new FloatType();
		boolType = new BoolType();

		initializePredefinedFunctions();
	}

	private void initializePredefinedFunctions()
	{
		for (Symbol s : SymbolTable.getPredifinedSymbols())
		{	
			TypeList args = tryResolvvePredefinedFuncArgs(s);
			Type returnType = s.type();

			put(s, new FuncType(args, returnType));
		}
	}

	private TypeList tryResolvvePredefinedFuncArgs(Symbol s)
	{
		TypeList args = new TypeList();
		// printInt() and printFloat() , printBool() require IntType, FloatType
		// and BoolType as arguments accordingly.
		// readInt(), readFloat() and println() require no argument

		if (s.name().equals("printInt"))
			args.append(intType);
		else if (s.name().equals("printFloat"))
			args.append(floatType);
		else if (s.name().equals("printBool"))
			args.append(boolType);

		return args;
	}

	private void reportError(int lineNum, int charPos, String message)
	{
		errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
		errorBuffer.append("[" + message + "]" + "\n");
	}

	private void put(Command node, Type type)
	{
		typeMap.put(node, type);

		if (type instanceof ErrorType) 
			reportError(node.lineNumber(), node.charPosition(), 
					((ErrorType)type).getMessage());
	}

	// helper methods to store functionDefinitions for performing checking on
	// function calls
	private void put(Symbol sym, Type args)
	{
		functions.put(sym, args);
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
		TypeList types = new TypeList();

		for (Expression exp : node)
		{
			exp.accept(this);
			types.append(getType((Command) exp));
		}

		// types of expressionList compose of the types of all the expressions 
		// in the list 
		put(node, types);
	}

	@Override
	public void visit(DeclarationList node) 
	{
		for (Declaration declaration : node)
			declaration.accept(this);
	}

	@Override
	public void visit(StatementList node)
	{
		for (Statement statement : node)
			statement.accept(this);

		// DeclarationList should has no type
	}

	@Override
	public void visit(AddressOf node) 
	{
		// type of AddressOf models the address of whatever the symbol node holds
		// (EX: var a = : int;
		// type of AddressOf A is Address(int).
		put(node, new AddressType(node.symbol().type()));
	}

	@Override
	public void visit(LiteralBool node) 
	{
		put(node, boolType);
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
		// A variable cannot be declared with type VoidType		
		put(node, node.symbol().type() instanceof VoidType 
				? new ErrorType(getInvalidVarTypeError(node))
		: node.symbol().type());
	}

	@Override
	public void visit(ArrayDeclaration node) 
	{
		// ensure ArrayDeclaration is valid (base type is not VoidType)
		Type base = tryResolveBaseType(node.symbol().type());

		put(node, ! (base instanceof VoidType) ? base 
				: new ErrorType
				(getInvalidArrayBaseTypeError(node.symbol().name(), base)) );
	}

	// helper function to parse the predefined base type of the array
	// EX: array[3, int] returns int
	// EX: array[3, array[3,float]] returns float
	private Type tryResolveBaseType(Type type)
	{
		if (!(type instanceof ArrayType))
			return type;

		ArrayType array = (ArrayType) type;
		return tryResolveBaseType(array.base());
	}

	private void expectValidArguments(FunctionDefinition node)
	{
		List<Symbol> symbols = node.arguments();
		for (int i = 0; i < symbols.size(); i++)
		{
			Type type = symbols.get(i).type();
			
			if (type instanceof ErrorType)
					put(node, new ErrorType
							(this.getHasInvalidArgument(node.function(), i, (ErrorType) type)));
			else if (type instanceof VoidType)
				put(node, new ErrorType
						(this.getHasVoidArgumentError(node.function(), i)));
		}
	}
	
	@Override
	public void visit(FunctionDefinition node)
	{
		Symbol funcSymbol = node.function();
		
		expectValidArguments(node);
		TypeList funcArgs =  getTypeListFromSymbols(node.arguments());
		FuncType funcType = new FuncType(funcArgs, funcSymbol.type());
		
		// associate FunctionDefinition with FuncType
		put(node, funcType);

		// associate Function symbol with FuncType into a separate map for
		// efficient function calls checking
		put(funcSymbol, funcType);

		boolean isVoidType = funcSymbol.type() instanceof VoidType;
		
		// make sure the return type of the main function is VoidTYpe
		if (funcSymbol.name().equals("main") && !isVoidType)
			put(node, new ErrorType(getInvalidMainFunctionSignatureError()));

		if (!isVoidType && !allPathReturn(node))
			put(node, new ErrorType
					(getNotAllPathReturnError(funcSymbol.name())));		
		else
			visitExpectCorrectReturnType(node);
	}

	private boolean allPathReturn(FunctionDefinition node)
	{
		AllPathReturnChecker hasReturnChecker = new AllPathReturnChecker();
		node.accept(hasReturnChecker);

		return hasReturnChecker.hasReturn(node);
	}

	private void visitExpectCorrectReturnType(FunctionDefinition node)
	{
		for (Statement statement : node.body())
		{			
			// if statement is a Return Statement, then the Return type is
			// whatever type that can be derived from its arguments. 
			statement.accept(this);

			if (statement instanceof Return)
			{
				Return returnState = (Return) statement;
				// retrieve returnType from arguments since we do not associate
				// a type to a Return node 
				Type expectedReturnType =  getType( (Command) returnState.argument()); 
				Type declaredReturnType = node.function().type();
				
				if (! (declaredReturnType instanceof VoidType) && 
						! declaredReturnType.equivalent(expectedReturnType))
					put((Command) statement,
							new ErrorType(getNotMatchingReturn(node.symbol(), expectedReturnType)));
			}
		}
	}

	@Override
	public void visit(Comparison node)
	{
		// call visit retrieve type to visit the left and right side of
		// Comparison, retrieve and associate their types, and then determine
		// and associate the type of Comparison 
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftSide = getType((Command) node.leftSide());
		Type rightSide = getType((Command) node.rightSide());

		put(node, leftSide.compare(rightSide));
	}

	@Override
	public void visit(Addition node) 
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.add(rightType));
	}

	@Override
	public void visit(Subtraction node)
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.sub(rightType));
	}

	@Override
	public void visit(Multiplication node)
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.mul(rightType));
	}

	@Override
	public void visit(Division node) 
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.div(rightType));
	}

	@Override
	public void visit(LogicalAnd node) 
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.and(rightType));
	}

	@Override
	public void visit(LogicalOr node)
	{
		node.leftSide().accept(this);
		node.rightSide().accept(this);

		Type leftType = getType((Command) node.leftSide());
		Type rightType = getType((Command) node.rightSide());

		put(node, leftType.or(rightType));
	}

	@Override
	public void visit(LogicalNot node) 
	{
		node.expression().accept(this);
		Command booleanExp = (Command) node.expression();

		put(node, getType(booleanExp).not());
	}

	@Override
	public void visit(Dereference node)
	{
		// type of Dereference is whatever type is derived from dereferencing  the Expression it holds
		node.expression().accept(this);
		put(node, getType((Command) node.expression()).deref());
	}

	@Override
	public void visit(Index node)
	{
		// type of Index is whatever type is derived by doing 
		// base.index(amount)
		Command base = (Command) node.base();
		Command amount = (Command) node.amount();

		base.accept(this);
		amount.accept(this);
		
		put(node, getType(base).index(getType(amount)));
	}

	@Override
	public void visit(Assignment node) 
	{
		node.source().accept(this);
		node.destination().accept(this);
		
		Type source = getType((Command) node.source());
		Type destination = tryResolveAddressBaseType
				(getType((Command) node.destination()));
	
		put(node, destination.assign(source));
	}

	private Type tryResolveAddressBaseType(Type address)
	{
		if ( ! (address instanceof AddressType))
			return address;
		return tryResolveAddressBaseType(((AddressType) address).base());
	}

	@Override
	public void visit(Call node) 
	{
		// a function should have been declared already before it can be called

		node.arguments().accept(this);

		// check to see if the arguments match
		Type declaredReturn = tryResolveFunctionType(node.function());
		Type expectedReturn = getType((Command) node.arguments());

		put(node, declaredReturn.call(expectedReturn));
	}

	private Type tryResolveFunctionType(Symbol funcName)
	{
		return functions.get(funcName);
	}

	@Override
	public void visit(IfElseBranch node) 
	{
		node.condition().accept(this);
		node.thenBlock().accept(this);
		node.elseBlock().accept(this);

		Type condition = getType((Command) node.condition());

		// if type of condition is not BoolType, reports an error 
		// otherwise, there is no need to associate IfElseBranch with a type 
		if(!(condition instanceof BoolType))
			put(node, new ErrorType(getInvalidIfElseConditionError(condition)));
	}

	@Override
	public void visit(WhileLoop node) 
	{
		node.condition().accept(this);
		Type condition = getType((Command) node.condition());

		if (!(condition instanceof BoolType))
			put(node, new ErrorType(getInvalidWhileConditionError(condition)));

		node.body().accept(this);

		// there is no need to associate WhileLoop with a type if there was 
		// no type error in the condition of WhileLoop
	}

	@Override
	public void visit(Return node) 
	{
		node.argument().accept(this);

		// the code below is commented out to avoid duplicating error reports
		// this is to be consistent with the output file 
		// type of Return is type of whatever Expression it holds
		//put(node, getType((Command) node.argument()));
	}

	@Override
	public void visit(ast.Error node) 
	{
		put(node, new ErrorType(node.message()));
	}

	@Override
	public void visit(Command node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Expression node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Declaration node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Statement node) {
		// TODO Auto-generated method stub
		
	}
}
