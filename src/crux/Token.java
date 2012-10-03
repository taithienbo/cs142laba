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
		LESSER_THAN ("<"),
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

		private String default_lexeme;

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
		
		public void setLexeme (String lexeme)
		{
			default_lexeme = lexeme;
		}
			
		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}

	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";

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
	
	public Token(String lexeme, int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		this.lexeme = lexeme;
		
		// TODO: based on the given lexeme determine and set the actual kind
		if (isFloat (lexeme))
			kind = Token.Kind.FLOAT;
		else if (isInteger (lexeme))
			kind = Token.Kind.INTEGER;
		else if (lexeme.equals(Token.Kind.AND.default_lexeme))
			kind = Token.Kind.AND;
		else if (lexeme.equals (Token.Kind.OR.default_lexeme))
			kind = Token.Kind.OR;
		else if (lexeme.equals (Token.Kind.NOT.default_lexeme))
			kind = Token.Kind.NOT;
		else if (lexeme.equals (Token.Kind.LET.default_lexeme))
			kind = Token.Kind.LET;
		else if (lexeme.equals (Token.Kind.VAR.default_lexeme))
			kind = Token.Kind.VAR;
		else if (lexeme.equals (Token.Kind.ARRAY.default_lexeme))
			kind = Token.Kind.ARRAY;
		else if (lexeme.equals (Token.Kind.FUNC.default_lexeme))
			kind = Token.Kind.FUNC;
		else if (lexeme.equals (Token.Kind.IF.default_lexeme))
			kind = Token.Kind.IF;
		else if (lexeme.equals (Token.Kind.ELSE.default_lexeme))
			kind = Token.Kind.ELSE;
		else if (lexeme.equals (Token.Kind.WHILE.default_lexeme))
			kind = Token.Kind.WHILE;
		else if (lexeme.equals (Token.Kind.TRUE.default_lexeme))
			kind = Token.Kind.TRUE;
		else if (lexeme.equals (Token.Kind.FALSE.default_lexeme))
			kind = Token.Kind.FALSE;
		else if (lexeme.equals (Token.Kind.RETURN.default_lexeme))
			kind = Token.Kind.RETURN;
		else if (lexeme.equals (Token.Kind.OPEN_PAREN.default_lexeme))
			kind = Token.Kind.OPEN_PAREN;
		else if (lexeme.equals (Token.Kind.CLOSE_PAREN.default_lexeme))
			kind = Token.Kind.CLOSE_PAREN;
		else if (lexeme.equals (Token.Kind.OPEN_BRACE.default_lexeme))
			kind = Token.Kind.OPEN_BRACE;
		else if (lexeme.equals (Token.Kind.CLOSE_BRACE.default_lexeme))
			kind = Token.Kind.CLOSE_BRACE;
		else if (lexeme.equals (Token.Kind.OPEN_BRACKET.default_lexeme))
			kind = Token.Kind.OPEN_BRACKET;
		else if (lexeme.equals (Token.Kind.CLOSE_BRACKET.default_lexeme))
			kind = Token.Kind.CLOSE_BRACKET;
		else if (lexeme.equals (Token.Kind.ADD.default_lexeme))
			kind = Token.Kind.ADD;
		else if (lexeme.equals (Token.Kind.SUB.default_lexeme))
			kind = Token.Kind.SUB;
		else if (lexeme.equals (Token.Kind.MUL.default_lexeme))
			kind = Token.Kind.MUL;
		else if (lexeme.equals (Token.Kind.DIV.default_lexeme))
			kind = Token.Kind.DIV;
		else if (lexeme.equals (Token.Kind.GREATER_EQUAL.default_lexeme))
			kind = Token.Kind.GREATER_EQUAL;
		else if (lexeme.equals (Token.Kind.LESSER_EQUAL.default_lexeme))
			kind = Token.Kind.LESSER_EQUAL;
		else if (lexeme.equals (Token.Kind.NOT_EQUAL.default_lexeme))
			kind = Token.Kind.NOT_EQUAL;
		else if (lexeme.equals (Token.Kind.EQUAL.default_lexeme))
			kind = Token.Kind.EQUAL;
		else if (lexeme.equals (Token.Kind.GREATER_THAN.default_lexeme))
			kind = Token.Kind.GREATER_THAN;
		else if (lexeme.equals (Token.Kind.LESSER_THAN.default_lexeme))
			kind = Token.Kind.LESSER_THAN;
		else if (lexeme.equals (Token.Kind.ASSIGN.default_lexeme))
			kind = Token.Kind.ASSIGN;
		else if (lexeme.equals (Token.Kind.COMMA.default_lexeme))
			kind = Token.Kind.COMMA;
		else if (lexeme.equals (Token.Kind.SEMICOLON.default_lexeme))
			kind = Token.Kind.SEMICOLON;
		else if (lexeme.equals (Token.Kind.COLON.default_lexeme))
			kind = Token.Kind.COLON;
		else if (lexeme.equals (Token.Kind.CALL.default_lexeme))
			kind = Token.Kind.CALL;
		else if (lexeme.equals (Token.Kind.IDENTIFIER.default_lexeme))
			kind = Token.Kind.IDENTIFIER;
		else if (lexeme.equals (Token.Kind.INTEGER.default_lexeme))
			kind = Token.Kind.INTEGER;
		else if (lexeme.equals (Token.Kind.FLOAT.default_lexeme))
			kind = Token.Kind.FLOAT;
		else if (lexeme.equals (Token.Kind.ERROR.default_lexeme))
			kind = Token.Kind.ERROR;
		else if (lexeme.equals (Token.Kind.EOF.default_lexeme))
			kind = Token.Kind.EOF;
		else
		{
			// if we don't match anything, signal error
			this.kind = Kind.ERROR;
			this.lexeme = "Unrecognized lexeme: " + lexeme;
		}

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
		if (kind == Kind.FLOAT || kind == Kind.INTEGER)
			result.append("(" + lexeme + ")");
		
		result.append("(").append("lineNum:")
				.append(lineNum).append(",")
				.append(" charPos:")
				.append(charPos)
				.append(")");
		
		return result.toString();
	}

	// OPTIONAL: function to query a token about its kind
	//           boolean is(Token.Kind kind)

	// OPTIONAL: add any additional helper or convenience methods
	//           that you find make for a clean design

}
