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

	
	
	
	public static Token OPEN_PAREN (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.OPEN_PAREN;
		return tok;
	}

	public static Token ERROR (int linePos, int charPos, 
			String errorChar)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.ERROR;
		tok.lexeme = errorChar;
		return tok;
	}
	public static Token CLOSE_PAREN (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.CLOSE_PAREN;
		return tok;
	}


	public static Token OPEN_BRACE (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.OPEN_BRACE;
		return tok;
	}

	public static Token CLOSE_BRACE (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.CLOSE_BRACE;
		return tok;
	}

	public static Token OPEN_BRACKET (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.OPEN_BRACKET;
		return tok;
	}


	public static Token CLOSE_BRACKET (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.CLOSE_BRACKET;
		return tok;
	}

	public static Token ADD (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.ADD;
		return tok;
	}


	public static Token SUB (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.SUB;
		return tok;
	}

	public static Token MUL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.MUL;
		return tok;
	}


	public static Token DIV (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.DIV;
		return tok;
	}

	public static Token GREATER_EQUAL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.GREATER_EQUAL;
		return tok;
	}


	public static Token LESSER_EQUAL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.LESSER_EQUAL;
		return tok;
	}


	public static Token NOT_EQUAL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.NOT_EQUAL;
		return tok;
	}


	public static Token EQUAL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.EQUAL;
		return tok;
	}


	public static Token GREATER_THAN (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.GREATER_THAN;
		return tok;
	}


	public static Token LESSER_THAN (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.LESS_THAN;
		return tok;
	}


	public static Token ASSIGN (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.ASSIGN;
		return tok;
	}


	public static Token COMMA (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.COMMA;
		return tok;
	}


	public static Token SEMICOLON (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.SEMICOLON;
		return tok;
	}


	public static Token COLON (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.COLON;
		return tok;
	}


	public static Token CALL (int linePos, int charPos)
	{
		Token tok = new Token (linePos, charPos);
		tok.kind = Kind.CALL;
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


	private boolean isFloat (String lexeme)
	{
		try
		{
			Float.parseFloat(lexeme);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}


	private boolean isInteger (String lexeme)
	{
		try
		{
			Integer.parseInt (lexeme);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}


	private boolean isIdentifier (String lexeme)
	{
		// per the doc, IDENTIFIER: 
		// ("_"|letter){"_"|letter|digit}
		char[] chars = lexeme.toCharArray();

		if (chars.length == 0)	// empty string
			return false;
		else if (chars[0] != '_' 
				|| !isLetter (chars[0]))
			return false;
		else 
		{
			for (int i = 1; i < chars.length; i++)
			{
				if (!isLetter(chars[i]))
				{
					// if chars[i] == '.', need to check the character
					// before to see if there is a valid float and not 
					// invalid character 
					if (chars[i] == '.' 
							&& chars[i-1] >=1 
							&& !isInteger ("" + chars[i-1]))
						return false;
					else if (!isInteger ("" + chars[i]))
						return false;
				}
			}
		}

		return true;

	}


	private boolean isLetter (char c)
	{
		return isLowerCaseLetter (c) || isUpperCaseLetter (c);
	}


	private boolean isLowerCaseLetter (char c)
	{
		return c == 'a' || c == 'b' || c == 'c' || c == 'd'
				|| c == 'e' || c == 'f' || c == 'g' || c == 'h'
				|| c == 'i' || c == 'j' || c == 'k' || c == 'l'
				|| c == 'm' || c == 'n' || c == 'o' || c == 'p'
				|| c == 'q' || c == 'r' || c == 's' || c == 't'
				|| c == 'u' || c == 'v' || c == 'w' || c == 'x'
				|| c == 'y' || c == 'z';
	}


	private boolean isUpperCaseLetter (char c)
	{
		return c == 'A' || c == 'B' || c == 'C' || c == 'D'
				|| c == 'E' || c == 'F' || c == 'G' || c == 'H'
				|| c == 'I' || c == 'J' || c == 'K' || c == 'L'
				|| c == 'M' || c == 'N' || c == 'O' || c == 'P'
				|| c == 'Q' || c == 'R' || c == 'S' || c == 'T'
				|| c == 'U' || c == 'V' || c == 'W' || c == 'X'
				|| c == 'Y' || c == 'Z';
	}

	public Token (Kind kind, int lineNum, int charPos)
	{
		this.kind = kind;
		this.lineNum = lineNum;
		this.charPos = charPos;

	}


	public static Token INTEGER (String lexeme, int lineNum, int charPos)
	{
		Token tok = new Token (lineNum, charPos);
		tok.kind = Kind.INTEGER;
		tok.lexeme = lexeme;
		return tok;
	}


	public static Token FLOAT (String lexeme, int lineNum, int charPos)
	{
		Token tok = new Token (lineNum, charPos);
		tok.kind = Kind.FLOAT;
		tok.lexeme = lexeme;
		return tok;
	}
	
	
	
	public static Token IDENTIFIER (String lexeme, int lineNum, int charPos)
	{
		Token tok = new Token (lineNum, charPos);
		tok.kind = Kind.IDENTIFIER;
		tok.lexeme = lexeme;
		return tok;
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

}
