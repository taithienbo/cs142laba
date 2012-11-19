package crux;

public class Token 
{

	public static enum Kind 
	{
		AND("and"),
		OR("or"),
		NOT("not"),
		LET ("let"),
		VAR ("var"),
		ARRAY ("array"),
		FUNC ("func"),
		IF ("if"),
		ELSE ("else"),
		WHILE ("while"),
		TRUE ("true"),
		FALSE ("false"),
		RETURN ("return"),

		OPEN_PAREN ("("),
		CLOSE_PAREN (")"),
		OPEN_BRACE ("{"),
		CLOSE_BRACE ("}"),
		OPEN_BRACKET ("["),
		CLOSE_BRACKET ("]"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		GREATER_EQUAL (">="),
		LESSER_EQUAL ("<="),
		NOT_EQUAL ("!="),
		EQUAL ("=="),
		GREATER_THAN (">"),
		LESS_THAN ("<"),
		ASSIGN ("="),
		COMMA (","),
		SEMICOLON (";"),
		COLON (":"),
		CALL ("::"),

		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF();

		// TODO: complete the list of possible tokens

		public String default_lexeme;

		Kind()
		{
			default_lexeme = "";
		}

		Kind(String lexeme)
		{
			default_lexeme = lexeme;
		}

		public boolean hasStaticLexeme()
		{
			return default_lexeme != null;
		}


		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}

	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";

	private static final String UNEXPECTED_CHARACTER = "Unexpected character";

	// OPTIONAL: implement factory functions for some tokens, as you see fit           
	public static Token EOF(int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}

	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;

		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	public Token (Kind kind, int lineNum, int charPos)
	{
		this.kind = kind;
		this.lineNum = lineNum;
		this.charPos = charPos;
	}

	public Token (Kind kind, String lexeme, int lineNum, int charPos)
	{
		this.kind = kind;
		this.lexeme = lexeme;
		this.lineNum = lineNum;
		this.charPos = charPos;
	}

	public int lineNumber()
	{
		return lineNum;
	}

	public int charPosition()
	{
		return charPos;
	}

	// Return the lexeme representing or held by this token
	public String lexeme()
	{
		// TODO: implement
		return lexeme;
	}

	public String toString()
	{
		// TODO: implement this
		StringBuilder result 
		= new StringBuilder (this.kind.name());

		if (kind == Kind.ERROR)
			result.append(toStringError());
		else if (kind == Kind.INTEGER || kind == Kind.FLOAT
				|| kind == Kind.IDENTIFIER)
			result.append (toStringLexeme ());
		
		result.append(toStringCharPosition());

		return result.toString();
	}

	private String toStringLexeme ()
	{
		return "(" + lexeme + ")";
	}
	private String toStringCharPosition ()
	{
		return "(lineNum:" + lineNum + ", charPos:" 
				+ charPos + ")";  
	}

	private String toStringError ()
	{
		return "(" + UNEXPECTED_CHARACTER + ": " + lexeme + ")";
	}

	// OPTIONAL: function to query a token about its kind
	//           boolean is(Token.Kind kind)

	// OPTIONAL: add any additional helper or convenience methods
	//           that you find make for a clean design
	public Kind kind ()
	{
		return kind;
	}
	
	public boolean is (Kind kind)
	{
		return this.kind == kind;
	}
}
