package crux;

import java.io.IOException;


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


	public void parse() throws IOException
	{
		initSymbolTable();
		try 
		{
			program();
		} 
		catch (QuitParseException q) 
		{
			errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
			errorBuffer.append("[Could not complete parsing.]");
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
	public void literal() throws IOException
	{
		expect (NonTerminal.LITERAL);
	}


	// designator := IDENTIFIER { "[" expression0 "]" } .
	public void designator() throws IOException
	{
		expect(Token.Kind.IDENTIFIER);
		while (accept(Token.Kind.OPEN_BRACKET)) 
		{
			expression0();
			expect(Token.Kind.CLOSE_BRACKET);
		}
	}


	// call-expression ";"
	public void callStatement () throws IOException
	{
		callExpression ();
		expect (Token.Kind.SEMICOLON);
	}


	// expression-list := [ expression0 { "," expression0 } ] .
	public void expressionList () throws IOException
	{
		if (have (NonTerminal.EXPRESSION0))
		{
			expression0 ();
			while (accept (Token.Kind.COMMA))
				expression0 ();
		}
	}

	// "::" IDENTIFIER "(" expression-list ")" .
	public void callExpression () throws IOException
	{
		expect (Token.Kind.CALL);

		if (have(Token.Kind.IDENTIFIER))
			tryResolveSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.OPEN_PAREN);
		expressionList ();
		expect (Token.Kind.CLOSE_PAREN);
	}


	// "not" expression3 | "(" expression0 ")"
	// | designator | call-expression | literal .
	public void expression3 () throws IOException
	{
		if (accept (Token.Kind.NOT))
			expression3 ();
		else if (accept (Token.Kind.OPEN_PAREN))
		{
			expression0 ();
			expect (Token.Kind.CLOSE_PAREN);
		}
		else if (have (NonTerminal.DESIGNATOR))
		{
			if (have(NonTerminal.DESIGNATOR))
				tryResolveSymbol(currentToken);
			designator ();
		}
		else if (have (NonTerminal.CALL_EXPRESSION))
			callExpression ();
		else if (have (NonTerminal.LITERAL))
			literal ();
		else
			throw new QuitParseException (reportSyntaxError(NonTerminal.EXPRESSION3));
	}


	// expression3 { op2 expression3 } .
	public void expression2 () throws IOException
	{
		expression3 ();
		while ( have (NonTerminal.OP2))
		{
			op2 ();
			expression3 ();
		}
	}


	// expression2 { op1  expression2 } .
	public void expression1 () throws IOException
	{
		expression2 ();
		while ( have (NonTerminal.OP1))
		{
			op1 ();
			expression2 ();
		}
	}


	// expression1 [ op0 expression1 ] .
	public void expression0 () throws IOException
	{
		expression1 ();

		if (have (NonTerminal.OP0))
		{
			op0 ();
			expression1 ();
		}
	}


	// "return" expression0 ";" 
	public void returnStatement () throws IOException
	{
		expect (Token.Kind.RETURN);
		expression0 ();
		expect (Token.Kind.SEMICOLON);
	}


	// "while" expression0 statement-block .
	public void whileStatement () throws IOException
	{
		expect (Token.Kind.WHILE);
		expression0 ();
		enterScope();
		statementBlock ();
		exitScope();
	}


	//  "let" designator "=" expression0 ";"
	public void assignmentStatement () throws IOException
	{
		expect (Token.Kind.LET);

		if (have(Token.Kind.IDENTIFIER))
			tryResolveSymbol (currentToken);

		designator ();
		expect (Token.Kind.ASSIGN);
		expression0();
		expect (Token.Kind.SEMICOLON);
	}


	// "if" expression0 statement-block [ "else" statement-block ]
	public void ifStatement () throws IOException
	{
		expect (Token.Kind.IF);
		expression0();

		enterScope();
		statementBlock ();
		exitScope();

		if (have (Token.Kind.ELSE))
		{
			expect (Token.Kind.ELSE);	
			enterScope();
			statementBlock ();
			exitScope();
		}
	}


	// variable-declaration  | call-statement | assignment-statement
	// | if-statement | while-statement | return-statement .
	public void statement () throws IOException
	{
		if (have (NonTerminal.VARIABLE_DECLARATION))
			variableDeclaration ();
		else if (have (NonTerminal.CALL_STATEMENT))
			callStatement ();
		else if (have (NonTerminal.ASSIGNMENT_STATEMENT))
			assignmentStatement ();
		else if (have (NonTerminal.IF_STATEMENT))
			ifStatement ();
		else if (have (NonTerminal.WHILE_STATEMENT))
			whileStatement ();
		else if (have (NonTerminal.RETURN_STATEMENT))
			returnStatement ();
		else 
			throw new QuitParseException
			(reportSyntaxError (NonTerminal.STATEMENT));
	}


	// { statement }
	public void statementList () throws IOException
	{
		while (have (NonTerminal.STATEMENT))
			statement ();	
	}


	// "{" statement-list "}"
	public void statementBlock () throws IOException
	{
		expect (Token.Kind.OPEN_BRACE);
		statementList ();
		expect (Token.Kind.CLOSE_BRACE);
	}


	// IDENTIFIER ":" type
	public void parameter () throws IOException
	{
		if (have(Token.Kind.IDENTIFIER))
			tryDeclareSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.COLON);
		type ();
	}


	// parameter-list := [ parameter { "," parameter } ] .
	public void parameterList () throws IOException
	{
		if (have (NonTerminal.PARAMETER))
		{
			parameter ();
			while (accept (Token.Kind.COMMA))
			{
				parameter ();
			}
		}
	}


	// "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block
	public void functionDefinition () throws IOException
	{
		expect (Token.Kind.FUNC);

		if (have(Token.Kind.IDENTIFIER))
			tryDeclareSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.OPEN_PAREN);

		enterScope();

		parameterList ();
		expect (Token.Kind.CLOSE_PAREN);
		expect (Token.Kind.COLON);
		type ();
		statementBlock ();

		exitScope();
	}


	// type := IDENTIFIER .
	public void type () throws IOException
	{
		expect (Token.Kind.IDENTIFIER);
	}

	// "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
	public void arrayDeclaration () throws IOException
	{
		expect (Token.Kind.ARRAY);

		if (have(Token.Kind.IDENTIFIER))
			tryDeclareSymbol(currentToken);

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
	}


	// ">=" | "<=" | "!=" | "==" | ">" | "<"
	public void op0 () throws IOException
	{
		expect (NonTerminal.OP0);
	}


	// "+" | "-" | "or" .
	public void op1 () throws IOException
	{
		expect (NonTerminal.OP1);
	}


	// "*" | "/" | "and" .
	public void op2 () throws IOException 
	{
		expect (NonTerminal.OP2);
	}


	// "var" IDENTIFIER ":" type ";"
	public void variableDeclaration () throws IOException
	{
		expect (Token.Kind.VAR);

		if (have (Token.Kind.IDENTIFIER))
			tryDeclareSymbol(currentToken);

		expect (Token.Kind.IDENTIFIER);
		expect (Token.Kind.COLON);
		type();
		expect (Token.Kind.SEMICOLON);
	}


	//  variable-declaration | array-declaration | function-definition .
	public void declaration () throws IOException
	{
		if (have (NonTerminal.VARIABLE_DECLARATION))
			variableDeclaration ();
		else if (have (NonTerminal.ARRAY_DECLARATION))
			arrayDeclaration ();
		else if ( have (NonTerminal.FUNCTION_DEFINITION))
			functionDefinition ();
		else 
			throw new QuitParseException 
			(reportSyntaxError (NonTerminal.DECLARATION));
	}


	// { declaration }
	public void declarationList () throws IOException
	{
		while (have (NonTerminal.DECLARATION))
		{
			declaration ();
		}
	}


	// program := declaration-list EOF .
	public void program() throws IOException
	{
		declarationList ();
		expect (Token.Kind.EOF);
	}
}
