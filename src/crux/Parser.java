package crux;

import java.io.IOException;


public class Parser {
	public static String studentName = "Tai Bo";
	public static String studentID = "53907660";
	public static String uciNetID = "tbo";

	// Grammar Rule Reporting ==========================================
	private int parseTreeRecursionDepth = 0;
	private StringBuffer parseTreeBuffer = new StringBuffer();

	public void enterRule(NonTerminal nonTerminal) {
		String lineData = new String();
		for(int i = 0; i < parseTreeRecursionDepth; i++)
		{
			lineData += "  ";
		}
		lineData += nonTerminal.name();
		//System.out.println("descending " + lineData);
		parseTreeBuffer.append(lineData + "\n");
		parseTreeRecursionDepth++;
	}

	private void exitRule(NonTerminal nonTerminal)
	{
		parseTreeRecursionDepth--;
	}

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

	public void parse()
	{
		try 
		{
			program();
		} 
		catch (QuitParseException q) 
		{
			errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
			errorBuffer.append("[Could not complete parsing.]");
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("An error occurs while parsing. " +
					"Error message: " + e.getMessage());
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
		if (have(kind)) {
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
		enterRule (NonTerminal.LITERAL);

		expect (NonTerminal.LITERAL);

		exitRule (NonTerminal.LITERAL);
	}

	// designator := IDENTIFIER { "[" expression0 "]" } .
	public void designator() throws IOException
	{
		enterRule(NonTerminal.DESIGNATOR);

		expect(Token.Kind.IDENTIFIER);
		while (accept(Token.Kind.OPEN_BRACKET)) {
			expression0();
			expect(Token.Kind.CLOSE_BRACKET);
		}

		exitRule(NonTerminal.DESIGNATOR);
	}


	// call-expression ";"
	public void callStatement () throws IOException
	{
		enterRule (NonTerminal.CALL_STATEMENT);

		callExpression ();
		expect (Token.Kind.SEMICOLON);

		exitRule (NonTerminal.CALL_STATEMENT);

	}


	// expression-list := [ expression0 { "," expression0 } ] .
	public void expressionList () throws IOException
	{
		enterRule (NonTerminal.EXPRESSION_LIST);

		if (have (NonTerminal.EXPRESSION0))
		{
			expression0 ();

			while (accept (Token.Kind.COMMA))
				expression0 ();
		}
	
	exitRule (NonTerminal.EXPRESSION_LIST);
}


// "::" IDENTIFIER "(" expression-list ")" .
public void callExpression () throws IOException
{
	enterRule (NonTerminal.CALL_EXPRESSION);

	expect (Token.Kind.CALL);
	expect (Token.Kind.IDENTIFIER);
	expect (Token.Kind.OPEN_PAREN);
	expressionList ();
	expect (Token.Kind.CLOSE_PAREN);

	exitRule (NonTerminal.CALL_EXPRESSION);
}


// "not" expression3 | "(" expression0 ")"
// | designator | call-expression | literal .
public void expression3 () throws IOException
{

	enterRule (NonTerminal.EXPRESSION3);

	if (accept (Token.Kind.NOT))
		expression3 ();
	else if (accept (Token.Kind.OPEN_PAREN))
	{
		expression0 ();
		expect (Token.Kind.CLOSE_PAREN);
	}
	else if (have (NonTerminal.DESIGNATOR))
		designator ();
	else if (have (NonTerminal.CALL_EXPRESSION))
		callExpression ();
	else if (have (NonTerminal.LITERAL))
		literal ();
	else
		throw new QuitParseException (reportSynTaxError(NonTerminal.EXPRESSION3));

	exitRule (NonTerminal.EXPRESSION3);

}


private String reportSynTaxError(NonTerminal expression3) {
	// TODO Auto-generated method stub
	return null;
}

// expression3 { op2 expression3 } .
public void expression2 () throws IOException
{
	enterRule (NonTerminal.EXPRESSION2);

	expression3 ();

	while ( have (NonTerminal.OP2))
	{
		op2 ();
		expression3 ();
	}
	exitRule (NonTerminal.EXPRESSION2);
}

// expression2 { op1  expression2 } .
public void expression1 () throws IOException
{
	enterRule (NonTerminal.EXPRESSION1);

	expression2 ();

	while ( have (NonTerminal.OP1))
	{
		op1 ();
		expression2 ();
	}
	exitRule (NonTerminal.EXPRESSION1);
}


// expression1 [ op0 expression1 ] .
public void expression0 () throws IOException
{
	enterRule (NonTerminal.EXPRESSION0);

	expression1 ();

	if (have (NonTerminal.OP0))
	{
		op0 ();
		expression1 ();
	}

	exitRule (NonTerminal.EXPRESSION0);
}


// "return" expression0 ";" 
public void returnStatement () throws IOException
{
	enterRule (NonTerminal.RETURN_STATEMENT);

	expect (Token.Kind.RETURN);
	expression0 ();
	expect (Token.Kind.SEMICOLON);

	exitRule (NonTerminal.RETURN_STATEMENT);
}


// "while" expression0 statement-block .
public void whileStatement () throws IOException
{
	enterRule (NonTerminal.WHILE_STATEMENT);

	expect (Token.Kind.WHILE);
	expression0 ();
	statementBlock ();

	exitRule (NonTerminal.WHILE_STATEMENT);
}


//  "let" designator "=" expression0 ";"
public void assignmentStatement () throws IOException
{
	enterRule (NonTerminal.ASSIGNMENT_STATEMENT);

	expect (Token.Kind.LET);
	designator ();
	expect (Token.Kind.ASSIGN);
	expression0();
	expect (Token.Kind.SEMICOLON);

	exitRule (NonTerminal.ASSIGNMENT_STATEMENT);
}


// "if" expression0 statement-block [ "else" statement-block ]
public void ifStatement () throws IOException
{
	enterRule (NonTerminal.IF_STATEMENT);

	expect (Token.Kind.IF);
	expression0();
	statementBlock ();

	expect (Token.Kind.ELSE);
	do 
	{
		statementBlock ();
	} while (accept (Token.Kind.ELSE));

	exitRule (NonTerminal.IF_STATEMENT);

}

// variable-declaration  | call-statement | assignment-statement
// | if-statement | while-statement | return-statement .
public void statement () throws IOException
{
	enterRule (NonTerminal.STATEMENT);

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

	exitRule (NonTerminal.STATEMENT);
}


// { statement }
public void statementList () throws IOException
{
	enterRule (NonTerminal.STATEMENT_LIST);

	while (have (NonTerminal.STATEMENT))
		statement ();

	exitRule (NonTerminal.STATEMENT_LIST);
}



// "{" statement-list "}"
public void statementBlock () throws IOException
{
	enterRule (NonTerminal.STATEMENT_BLOCK);

	expect (Token.Kind.OPEN_BRACE);
	statementList ();
	expect (Token.Kind.CLOSE_BRACE);

	exitRule (NonTerminal.STATEMENT_BLOCK);
}


// IDENTIFIER ":" type
public void parameter () throws IOException
{
	enterRule (NonTerminal.PARAMETER);

	expect (Token.Kind.IDENTIFIER);
	expect (Token.Kind.COLON);
	type ();

	exitRule (NonTerminal.PARAMETER);
}


// parameter-list := [ parameter { "," parameter } ] .
public void parameterList () throws IOException
{
	enterRule (NonTerminal.PARAMETER_LIST);

	if (have (NonTerminal.PARAMETER))
	{
		parameter ();

		while (accept (Token.Kind.COMMA))
		{
			parameter ();
		}

	}
	exitRule (NonTerminal.PARAMETER_LIST);


}


// "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block
public void functionDefinition () throws IOException
{
	enterRule (NonTerminal.FUNCTION_DEFINITION);

	expect (Token.Kind.FUNC);
	expect (Token.Kind.IDENTIFIER);
	expect (Token.Kind.OPEN_PAREN);
	parameterList ();
	expect (Token.Kind.CLOSE_PAREN);
	expect (Token.Kind.COLON);
	type ();
	statementBlock ();

	exitRule (NonTerminal.FUNCTION_DEFINITION);
}


// type := IDENTIFIER .
public void type () throws IOException
{
	enterRule (NonTerminal.TYPE);

	expect (Token.Kind.IDENTIFIER);

	exitRule (NonTerminal.TYPE);
}

// "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
public void arrayDeclaration () throws IOException
{
	enterRule (NonTerminal.ARRAY_DECLARATION);

	expect (Token.Kind.ARRAY);
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

	exitRule (NonTerminal.ARRAY_DECLARATION);	
}


// ">=" | "<=" | "!=" | "==" | ">" | "<"
public void op0 () throws IOException
{
	enterRule (NonTerminal.OP0);
	expect (NonTerminal.OP0);
	exitRule (NonTerminal.OP0);
}


// "+" | "-" | "or" .
public void op1 () throws IOException
{
	enterRule (NonTerminal.OP1);
	expect (NonTerminal.OP1);
	exitRule (NonTerminal.OP1);
}


// "*" | "/" | "and" .
public void op2 () throws IOException 
{
	enterRule (NonTerminal.OP2);
	expect (NonTerminal.OP2);
	exitRule (NonTerminal.OP2);
}

// "var" IDENTIFIER ":" type ";"
public void variableDeclaration () throws IOException
{
	enterRule (NonTerminal.VARIABLE_DECLARATION);

	expect (Token.Kind.VAR);
	expect (Token.Kind.IDENTIFIER);
	expect (Token.Kind.COLON);
	type();
	expect (Token.Kind.SEMICOLON);

	exitRule (NonTerminal.VARIABLE_DECLARATION);
}


//  variable-declaration | array-declaration | function-definition .
public void declaration () throws IOException
{
	enterRule (NonTerminal.DECLARATION);

	if (have (NonTerminal.VARIABLE_DECLARATION))
		variableDeclaration ();
	else if (have (NonTerminal.ARRAY_DECLARATION))
		arrayDeclaration ();
	else if ( have (NonTerminal.FUNCTION_DEFINITION))
		functionDefinition ();
	else 
		throw new QuitParseException 
		(reportSyntaxError (NonTerminal.DECLARATION));

	exitRule (NonTerminal.DECLARATION);
}

// { declaration }
public void declarationList () throws IOException
{
	enterRule (NonTerminal.DECLARATION_LIST);
	while (have (NonTerminal.DECLARATION))
	{
		declaration ();
	}

	exitRule (NonTerminal.DECLARATION_LIST);
}



// program := declaration-list EOF .
public void program() throws IOException
{
	enterRule (NonTerminal.PROGRAM);

	declarationList ();
	expect (Token.Kind.EOF);

	exitRule (NonTerminal.PROGRAM);
}




}
