package crux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import types.Type;

import ast.AddressOf;
import ast.ArrayDeclaration;
import ast.Assignment;
import ast.Call;
import ast.Command;

import ast.Declaration;
import ast.DeclarationList;
import ast.Dereference;

import ast.Expression;
import ast.ExpressionList;
import ast.FunctionDefinition;
import ast.IfElseBranch;
import ast.Index;
import ast.LiteralBool;
import ast.LiteralBool.Value;
import ast.LiteralFloat;
import ast.LiteralInt;

import ast.LogicalNot;

import ast.Return;
import ast.Statement;
import ast.StatementList;

import ast.VariableDeclaration;
import ast.WhileLoop;
import crux.Token.Kind;


public class Parser 
{
	public static String studentName = "Tai Bo";
	public static String studentID = "53907660";
	public static String uciNetID = "tbo";

	private SymbolTable symbolTable;

	// add the predefined symbols to the table
	private void initSymbolTable()
	{
		symbolTable = new SymbolTable();
		symbolTable.insert("readInt");
		symbolTable.insert("readFloat");
		symbolTable.insert("printBool");	
		symbolTable.insert("printInt");
		symbolTable.insert("printFloat");
		symbolTable.insert("println");	
	}


	private void enterScope()
	{
		symbolTable.increaseDepth();
	}


	private void exitScope()
	{
		symbolTable.decreseDepth();
	}


	private Symbol tryResolveSymbol(Token ident)
	{
		assert(ident.is(Token.Kind.IDENTIFIER));
		String name = ident.lexeme();
		try 
		{
			return symbolTable.lookup(name);
		} 
		catch (SymbolNotFoundError e) 
		{
			String message = reportResolveSymbolError
					(name, ident.lineNumber(), ident.charPosition());
			return new ErrorSymbol(message);
		}
	}


	private String reportResolveSymbolError(String name, int lineNum, int charPos)
	{
		String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")" +
				"[Could not find " + name + ".]";
		errorBuffer.append(message + "\n");
		errorBuffer.append(symbolTable.toString() + "\n");
		return message;
	}


	private Symbol tryDeclareSymbol(Token ident)
	{
		assert(ident.is(Token.Kind.IDENTIFIER));
		String name = ident.lexeme();
		try 
		{
			return symbolTable.insert(name);
		} 
		catch (RedeclarationError re) 
		{
			String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
			return new ErrorSymbol(message);
		}
	}


	private String reportDeclareSymbolError(String name, int lineNum, int charPos)
	{
		String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
		errorBuffer.append(message + "\n");
		errorBuffer.append(symbolTable.toString() + "\n");
		return message;
	}    


	// Helper Methods ==========================================
	private Token expectRetrieve(Token.Kind kind) throws IOException
	{
		Token tok = currentToken;
		if (accept(kind))
			return tok;
		String errorMessage = reportSyntaxError(kind);
		throw new QuitParseException(errorMessage);
		//return ErrorToken(errorMessage);
	}

	
	private Token expectRetrieve(NonTerminal nt) throws IOException
	{
		Token tok = currentToken;
		if (accept(nt))
			return tok;
		String errorMessage = reportSyntaxError(nt);
		throw new QuitParseException(errorMessage);
		//return ErrorToken(errorMessage);
	}

	// Example helper method
	// feel free to make your own
	private Integer expectInteger() throws NumberFormatException, IOException
	{
		String num = currentToken.lexeme();
		if (expect(Token.Kind.INTEGER))
			return Integer.valueOf(num);
		return null;
	}


	// Grammar Rule Reporting ==========================================
	private int parseTreeRecursionDepth = 0;
	private StringBuffer parseTreeBuffer = new StringBuffer();


	public String parseTreeReport()
	{
		return parseTreeBuffer.toString();
	}

	// Error Reporting ==========================================
	private StringBuffer errorBuffer = new StringBuffer();


	private String reportSyntaxError(NonTerminal nt)
	{
		String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")" +
				"[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";

		errorBuffer.append(message + "\n");
		return message;
	}


	private String reportSyntaxError(Token.Kind kind)
	{
		String message = "SyntaxError(" + lineNumber() + "," + charPosition()
				+ ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
		errorBuffer.append(message + "\n");

		return message;
	}


	public String errorReport()
	{
		return errorBuffer.toString();
	}


	public boolean hasError()
	{
		return errorBuffer.length() != 0;
	}


	private class QuitParseException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public QuitParseException(String errorMessage)
		{
			super(errorMessage);
		}
	}


	private int lineNumber()
	{
		return currentToken.lineNumber();
	}


	private int charPosition()
	{
		return currentToken.charPosition();
	}

	// Parser ==========================================
	private Scanner scanner;
	private Token currentToken;


	public Parser(Scanner scanner) throws IOException
	{
		this.scanner = scanner;
		currentToken = scanner.next();
	}


	public Command parse() throws IOException
	{
		initSymbolTable();
		try {
			return program();
		} catch (QuitParseException q) {
			return new ast.Error(lineNumber(), charPosition(), "Could not complete parsing.");
		}
	}


	// Helper Methods ==========================================
	private boolean have(Token.Kind kind)
	{
		return currentToken.is(kind);
	}


	private boolean have(NonTerminal nt)
	{
		return nt.firstSet().contains(currentToken.kind());
	}


	private boolean accept(Token.Kind kind) throws IOException
	{
		if (have(kind)) 
		{
			currentToken = scanner.next();
			return true;
		}
		return false;
	}    


// Typing System ===================================
    
    private Type tryResolveType(String typeStr)
    {
        return Type.getBaseType(typeStr);
    }
    
    	
	private boolean accept(NonTerminal nt) throws IOException
	{
		if (have(nt)) 
		{
			currentToken = scanner.next();
			return true;
		}
		return false;
	}


	private boolean expect(Token.Kind kind) throws IOException
	{
		if (accept(kind))
			return true;
		String errorMessage = reportSyntaxError(kind);
		throw new QuitParseException(errorMessage);
		//return false;
	}


	private boolean expect(NonTerminal nt) throws IOException
	{
		if (accept(nt))
			return true;
		String errorMessage = reportSyntaxError(nt);
		throw new QuitParseException(errorMessage);
		//return false;
	}

	// Grammar Rules =====================================================

	// literal := INTEGER | FLOAT | TRUE | FALSE .
	public Expression literal() throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Token literal = expectRetrieve(NonTerminal.LITERAL);

		if (literal.kind == Kind.INTEGER)
			return new LiteralInt(lineNumber, charPosition, 
					Integer.parseInt(literal.lexeme()));
		else if (literal.kind == Kind.FLOAT)
			return new LiteralFloat(lineNumber, charPosition, 
					Float.parseFloat(literal.lexeme()));
		else if (literal.kind == Kind.TRUE)
			return new LiteralBool(lineNumber, charPosition,
					Value.TRUE);
		else 
			return new LiteralBool(lineNumber, charPosition,
					Value.FALSE);
	}


	// designator := IDENTIFIER { "[" expression0 "]" } .
	/**
	 * 
	 * @param expression 
	 * @return per the rule for designator, if this method finds no expression,
	 * then return the Expression passed as the parameter, otherwise return
	 * the result expression  
	 * @throws IOException
	 */
	// not sure how to do this, not sure how array indexing
	// works in Crux 
	public Expression designator() throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Token base = expectRetrieve(Token.Kind.IDENTIFIER);
		AddressOf address = 
				new AddressOf(lineNumber, charPosition,
						new Symbol(base.lexeme()));

		// set index to the base address to correctly represent one-dimensional 
		// array
		Expression index = address;
		while (accept(Token.Kind.OPEN_BRACKET)) 
		{
			charPosition = charPosition();
			Expression amount =  expression0();

			// a multi-dimensional array can be represented as a one 
			// dimensional array in which each element is also an array
			index = new Index(lineNumber, charPosition,
					index, amount);

			expect(Token.Kind.CLOSE_BRACKET);
		}

		return index;
	}


	// call-expression ";"
	public Statement callStatement () throws IOException
	{
		Statement callStatement = (Statement) callExpression ();
		expect (Token.Kind.SEMICOLON);

		return callStatement;
	}


	// expression-list := [ expression0 { "," expression0 } ] .
	public ExpressionList expressionList () throws IOException
	{
		ExpressionList expressionList = 
				new ExpressionList(lineNumber(), charPosition());

		if (have (NonTerminal.EXPRESSION0))
		{
			expressionList.add(expression0 ());
			while (accept (Token.Kind.COMMA))
				expressionList.add(expression0 ());
		}

		return expressionList;
	}

	// "::" IDENTIFIER "(" expression-list ")" .
	public Expression callExpression () throws IOException
	{
		ExpressionList arguments = null;

		int lineNumber = lineNumber();
		int charPosition = charPosition();

		expect (Token.Kind.CALL);

		Symbol func = tryResolveSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.OPEN_PAREN);
		arguments = expressionList ();
		expect (Token.Kind.CLOSE_PAREN);

		return new Call(lineNumber, charPosition, func, arguments);
	}


	// "not" expression3 | "(" expression0 ")"
	// | designator | call-expression | literal .
	public Expression expression3 () 	throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();
		Expression expression = null;

		if (accept (Token.Kind.NOT))
			expression = new LogicalNot
			(lineNumber, charPosition, 
					expression3());

		else if (accept (Token.Kind.OPEN_PAREN))
		{
			expression = expression0 ();
			expect (Token.Kind.CLOSE_PAREN);
		}
		// not sure about array indexing 
		// assuming need to use Dereference
		else if (have (NonTerminal.DESIGNATOR))
		{
			tryResolveSymbol(currentToken);
			expression = new Dereference
					(lineNumber, charPosition, designator ());
		}	
		else if (have (NonTerminal.CALL_EXPRESSION))
		{
			expression = callExpression();
		}
		else if (have (NonTerminal.LITERAL))
			expression = literal ();
		else
			throw new QuitParseException (reportSyntaxError(NonTerminal.EXPRESSION3));

		return expression;
	}


	// expression3 { op2 expression3 } .
	public Expression expression2 () throws IOException
	{		
		Expression leftSide = expression3();
		while ( have (NonTerminal.OP2))
		{
			Token operatorToken = op2 ();
			Expression rightSide = expression3 ();

			leftSide = Command.newExpression(leftSide, operatorToken, rightSide);
		}

		return leftSide;
	}


	// expression2 { op1  expression2 } .
	public Expression expression1 () throws IOException
	{
		Expression leftSide = expression2();

		while (have (NonTerminal.OP1))
		{
			Token operator = op1 ();

			Expression rightSide = expression2();
			leftSide = Command.newExpression(leftSide, operator, rightSide);
		}

		return leftSide;
	}


	// expression1 [ op0 expression1 ] .
	public Expression expression0 () throws IOException
	{
		Expression leftSide =  expression1();

		if (have (NonTerminal.OP0))
		{	
			Token operationToken = op0 ();
			Expression rightSide = expression1 ();

			return Command.newExpression(leftSide, operationToken, rightSide);
		}

		return leftSide;
	}


	// "return" expression0 ";" 
	public Statement returnStatement () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Expression arguments = null;
		expect (Token.Kind.RETURN);
		arguments =  expression0 ();
		expect (Token.Kind.SEMICOLON);

		return new Return(lineNumber, charPosition, arguments);
	}


	// "while" expression0 statement-block .
	public Statement whileStatement () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Expression condition = null; 
		StatementList body = null;

		expect (Token.Kind.WHILE);
		condition = expression0 ();
		enterScope();
		body = statementBlock ();
		exitScope();

		return new WhileLoop(lineNumber, charPosition,
				condition, body);
	}


	//  "let" designator "=" expression0 ";"
	public Statement assignmentStatement () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Expression destination = null;
		Expression source = null;

		expect (Token.Kind.LET);

		if (have(Token.Kind.IDENTIFIER))
			tryResolveSymbol (currentToken);

		destination = designator ();
		expect (Token.Kind.ASSIGN);
		source = expression0();
		expect (Token.Kind.SEMICOLON);

		return new Assignment(lineNumber, charPosition,
				destination, source);
	}


	// "if" expression0 statement-block [ "else" statement-block ]
	public Statement ifStatement () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Expression condition = null;
		StatementList thenBlock = null;
		expect (Token.Kind.IF);
		condition = expression0();

		enterScope();
		thenBlock = statementBlock ();
		exitScope();

		// it is necessary to initialize elseBlock even if there is no Else
		// part of the IfElseBranch, since ast.IfElseBranch.elseBlock() always
		// get called.  
		StatementList elseBlock = new StatementList(lineNumber(), charPosition());
		if (have (Token.Kind.ELSE))
		{
			expect (Token.Kind.ELSE);	
			enterScope();
			elseBlock = statementBlock ();
			exitScope();
		}

		return new IfElseBranch (lineNumber, charPosition,
				condition, thenBlock, elseBlock);
	}


	// variable-declaration  | call-statement | assignment-statement
	// | if-statement | while-statement | return-statement .
	public Statement statement () throws IOException
	{
		if (have (NonTerminal.VARIABLE_DECLARATION))
			return variableDeclaration ();
		else if (have (NonTerminal.CALL_STATEMENT))
			return callStatement ();
		else if (have (NonTerminal.ASSIGNMENT_STATEMENT))
			return assignmentStatement ();
		else if (have (NonTerminal.IF_STATEMENT))
			return ifStatement ();
		else if (have (NonTerminal.WHILE_STATEMENT))
			return whileStatement ();
		else if (have (NonTerminal.RETURN_STATEMENT))
			return returnStatement ();
		else 
			throw new QuitParseException
			(reportSyntaxError (NonTerminal.STATEMENT));
	}


	// { statement }
	public StatementList statementList () throws IOException
	{
		StatementList statementList = new StatementList(lineNumber(), charPosition());

		while (have (NonTerminal.STATEMENT))
			statementList.add(statement ());

		return statementList;
	}


	// "{" statement-list "}"
	public StatementList statementBlock () throws IOException
	{
		StatementList statementList = null;

		expect (Token.Kind.OPEN_BRACE);
		statementList = statementList ();
		expect (Token.Kind.CLOSE_BRACE);

		return statementList;
	}


	// IDENTIFIER ":" type
	public Symbol parameter () throws IOException
	{
		Symbol symbol = null;
		if (have(Token.Kind.IDENTIFIER))
			symbol = tryDeclareSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.COLON);
		type ();

		return symbol;
	}


	// parameter-list := [ parameter { "," parameter } ] .
	public List<Symbol> parameterList () throws IOException
	{
		List<Symbol> parameterList = new ArrayList<Symbol>();

		if (have (NonTerminal.PARAMETER))
		{
			parameterList.add(parameter ());
			while (accept (Token.Kind.COMMA))
			{
				parameterList.add(parameter ());
			}
		}

		return parameterList;
	}


	// "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block
	public FunctionDefinition functionDefinition () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		Symbol func = null;
		StatementList body = null;
		List<Symbol> args  = null;

		expect (Token.Kind.FUNC);

		if (have(Token.Kind.IDENTIFIER))
			func = tryDeclareSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.OPEN_PAREN);

		enterScope();

		args = parameterList ();
		expect (Token.Kind.CLOSE_PAREN);
		expect (Token.Kind.COLON);
		type ();
		body = statementBlock ();

		exitScope();

		return new FunctionDefinition(lineNumber, charPosition,
				func, args, body);
	}


	// type := IDENTIFIER .
	public void type () throws IOException
	{
		expect (Token.Kind.IDENTIFIER);
	}

	// "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
	public ArrayDeclaration arrayDeclaration () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();

		ArrayDeclaration arrayDeclaration = null;

		expect (Token.Kind.ARRAY);

		if (have(Token.Kind.IDENTIFIER))
		{
			arrayDeclaration = 
					new ArrayDeclaration
					(lineNumber, charPosition,  tryDeclareSymbol(currentToken));
		}
		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.COLON);
		type();
		expect (Token.Kind.OPEN_BRACKET);
		expect (Token.Kind.INTEGER);
		expect (Token.Kind.CLOSE_BRACKET);

		while (accept (Token.Kind.OPEN_BRACKET))
		{
			expect (Token.Kind.INTEGER);
			expect (Token.Kind.CLOSE_BRACKET);
		}

		expect (Token.Kind.SEMICOLON);

		return arrayDeclaration;
	}


	// ">=" | "<=" | "!=" | "==" | ">" | "<"
	public Token op0 () throws IOException
	{
		return expectRetrieve (NonTerminal.OP0);
	}


	// "+" | "-" | "or" .
	public Token op1 () throws IOException
	{
		return expectRetrieve (NonTerminal.OP1);
	}


	// "*" | "/" | "and" .
	public Token op2 () throws IOException 
	{
		return expectRetrieve(NonTerminal.OP2);
	}


	// "var" IDENTIFIER ":" type ";"
	public VariableDeclaration variableDeclaration () throws IOException
	{
		int lineNumber = lineNumber();
		int charPosition = charPosition();
		VariableDeclaration variableDeclaration = null;

		expect (Token.Kind.VAR);

		if (have (Token.Kind.IDENTIFIER))
		{
			variableDeclaration = new VariableDeclaration(
					lineNumber, charPosition, 
					tryDeclareSymbol(currentToken));
		}

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.COLON);
		type();
		expect (Token.Kind.SEMICOLON);

		return variableDeclaration;
	}


	//  variable-declaration | array-declaration | function-definition .
	public Declaration declaration () throws IOException
	{
		if (have (NonTerminal.VARIABLE_DECLARATION))
			return variableDeclaration ();
		else if (have (NonTerminal.ARRAY_DECLARATION))
			return arrayDeclaration ();
		else if ( have (NonTerminal.FUNCTION_DEFINITION))
			return functionDefinition ();
		else 
			throw new QuitParseException 
			(reportSyntaxError (NonTerminal.DECLARATION));
	}


	// { declaration }
	public DeclarationList declarationList () throws IOException
	{
		DeclarationList declarationList = new DeclarationList(lineNumber(), charPosition());

		while (have (NonTerminal.DECLARATION))
		{
			declarationList.add(declaration ());
		}

		return declarationList;
	}


	// program := declaration-list EOF .
	public DeclarationList program() throws IOException
	{
		ast.DeclarationList list;
		list = declarationList ();
		expect (Token.Kind.EOF);

		return list;
	}
}
