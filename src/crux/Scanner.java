package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import crux.Token.Kind;

public class Scanner implements Iterable<Token> {
	public static String studentName = "Tai Bo";
	public static String studentID = "53907660";
	public static String uciNetID = "tbo";

	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
	private Reader input;


	
	// nextChar has been examined
	Scanner(Reader reader) throws IOException
	{
		// TODO: initialize the Scanner
		this.input = reader;

		lineNum = 1;
		charPos = 1;
		// initialize nextChar with the first character
		nextChar = input.read();

	}	

	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	//
	private int readChar() throws IOException 
	{
		// advance to the next character to get ready for the next call
		// before returning nextChar
		if (nextChar == -1)	// EOF
			return -1;
		updateCharPosition();
		nextChar = input.read();

		return nextChar;
	}


	private void skipBlankSpace () throws IOException
	{
		//	System.out.println("skipBlankSpace()," +
		//			" nextChar: " + nextChar);
		while (isBlank (nextChar))
			nextChar = readChar ();
		
	}


	private boolean isBlank (int character)
	{ 
		// for more info, look up ASCII Table 
		int[] blankValues = new int[] {0, 10, 12, 9, 32};
		//	System.out.println("isBlank(), nextChar: " + character);
		for (int blankValue : blankValues)
			if (character == blankValue)
				return true;

		return false;
	}

	private void updateCharPosition ()
	{
		if (nextChar == 10)
		{
			lineNum++;
			charPos = 1;
		}
		else
			charPos++;
	}
	
	
	private void skipCommands() throws IOException
	{
		while ( nextChar  != 10 && nextChar != -1)	// per ASCII table, 10 is the linefeed 
		{
			nextChar = readChar ();
		}
	}
	
	

	
	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	public Token next() throws IOException
	{
		skipBlankSpace ();

		if (nextChar == -1)	// check for EOF 
			return Token.EOF (lineNum, charPos);
		
		if ( (char) nextChar == '/') 	// check commands
		{
			do
			{
				int lineNum = this.lineNum;
				int charPos = this.charPos;
				nextChar = readChar ();
				if (nextChar == '/')
				{
					skipCommands ();
					skipBlankSpace ();
				}
				else
					return new Token (Kind.DIV, lineNum, charPos);
			} while ((char) nextChar == '/');
		
			
		}
		// TODO: implement this

		int lineNum = this.lineNum;
		int charPos = this.charPos;

		switch ( (char)nextChar)
		{
		case '(':
			nextChar = readChar();
			return new Token (Kind.OPEN_PAREN,lineNum, charPos);
		case ')':
			nextChar = readChar();
			return new Token (Kind.CLOSE_PAREN, lineNum, charPos);	
		case '{':
			nextChar = readChar();
			return  new Token (Kind.OPEN_BRACE, lineNum, charPos);
		case '}':
			nextChar = readChar();
			return new Token (Kind.CLOSE_BRACE, lineNum, charPos);
		case '[':
			nextChar = readChar();
			return new Token (Kind.OPEN_BRACKET, lineNum, charPos);
		case ']':
			nextChar = readChar();
			return new Token (Kind.CLOSE_BRACKET, lineNum, charPos);
		case '+':
			nextChar = readChar();
			return new Token (Kind.ADD, lineNum, charPos);
		case '-':
			nextChar = readChar();
			return new Token (Kind.SUB, lineNum, charPos);
		case '*':
			nextChar = readChar();
			return new Token (Kind.MUL, lineNum, charPos);
		case '/':
			nextChar = readChar();
			return new Token (Kind.DIV, lineNum, charPos);
		case ',':
			nextChar = readChar();
			return new Token (Kind.COMMA, lineNum, charPos);
		case ';':
			nextChar = readChar();
			return new Token (Kind.SEMICOLON, lineNum, charPos);
		default:
			if (isNumber ((char) nextChar))
				return parseNumber (lineNum, charPos);
			else
				return parseMultiChar (lineNum, charPos);
		}

	}


	private boolean isNumber (char character)
	{
		return character == '0' || character == '1'  || character == '2'
				|| character == '3' || character == '4' || character == '5'
				|| character == '6' || character == '7' || character == '8'
				|| character == '9';
	}


	private Token parseNumber (int lineNum, int charPos) throws IOException
	{

		String number = "" + (char) nextChar;
		nextChar = readChar ();

		while (isNumber ((char)nextChar))
		{
			number += (char) nextChar;
			nextChar = readChar ();
		}

		if ( ( (char) nextChar) == '.')		// Float 
		{
			number += (char) nextChar;
			nextChar = readChar ();

			while (isNumber ((char)nextChar))
			{
				number += (char) nextChar;
				nextChar = readChar ();
			}

			return new Token (Kind.FLOAT, number, lineNum, charPos);
		}
		else
			return new Token (Kind.INTEGER, number, lineNum, charPos);
	}



	private Token parseMultiChar (int lineNum, int charPos) throws IOException
	{
		switch ((char) nextChar)
		{
		case '>':
			nextChar = readChar ();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return new Token (Kind.GREATER_EQUAL, lineNum, charPos);
			}
			else
				return new Token (Kind.GREATER_THAN, lineNum, charPos);

		case '<':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return new Token (Kind.LESSER_EQUAL, lineNum, charPos);
			}
			else 
				return new Token (Kind.LESS_THAN, lineNum, charPos);

		case '!':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return new Token (Kind.NOT_EQUAL, lineNum, charPos);
			}
			else
				return new Token (Kind.ERROR, "!", lineNum, charPos);

		case '=':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return  new Token (Kind.EQUAL, lineNum, charPos);
			}
			else
				return new Token (Kind.ASSIGN, lineNum, charPos);
		case ':':
			nextChar = readChar ();
			if (((char) nextChar) == ':')
			{
				nextChar = readChar ();
				return new Token (Kind.CALL, lineNum, charPos);
			}
			else
				return new Token (Kind.COLON, lineNum, charPos);
		default:
			if (isValidIdentifierCharacter ((char) nextChar))
				return parseIdentifier (lineNum, charPos);
			else
			{
				String lexeme = "" + (char) nextChar;
				nextChar = readChar ();
				return new Token (Kind.ERROR, lexeme, lineNum, charPos);
			}
		}
	}

	private Token parseIdentifier (int lineNum, int charPos) throws IOException
	{
		String lexeme = "" + (char) nextChar;
		switch ((char) nextChar)
		{
		case 'a':
			nextChar = readChar ();
			if ((char) nextChar  == 'n')		// check for Kind.AND
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 'd')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					if (isBlank (nextChar) || !isValidIdentifierCharacter ((char)nextChar))
						return new Token (Token.Kind.AND, lineNum, charPos);
				}
			}
			else if ((char) nextChar == 'r')	// check for Kind.ARRAY
			{
				lexeme += (char) nextChar ;
				nextChar = readChar ();
				if ((char) nextChar == 'r')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					if ((char) nextChar  == 'a')
					{
						lexeme += (char) nextChar;
						nextChar = readChar ();
						if ((char) nextChar == 'y')
						{
							lexeme += (char) nextChar;
							nextChar = readChar ();
							if (isBlank (nextChar) || 
									!isValidIdentifierCharacter ((char)nextChar))
								return new Token (Kind.ARRAY, lineNum, charPos);	
						}
					}
				}
			}
			break;

		case 'o':
			nextChar = readChar ();
			if ((char)nextChar == 'r')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if (isBlank (nextChar) || !isValidIdentifierCharacter ((char)nextChar))
					return new Token (Kind.OR, lineNum, charPos);
			}
			break;
		case 'n':	// kind.NOT
			nextChar = readChar ();
			if ((char) nextChar == 'o')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 't')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					if (isBlank (nextChar) 
							|| !isValidIdentifierCharacter ((char)nextChar))
						return new Token (Kind.NOT, lineNum, charPos);
				}
			}
			break;
		case 'l':	// Kind.LET
			nextChar = readChar ();
			if ((char) nextChar == 'e')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 't')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					if (isBlank (nextChar) 
							|| !isValidIdentifierCharacter ((char) nextChar))
						return new Token (Kind.LET, lineNum, charPos);
				}
			}
			break;
		case 'v': 	// Kind.VAR
			nextChar = readChar ();
			if ((char) nextChar == 'a')
			{
				lexeme += (char)nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 'r')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if (isBlank (nextChar) || !isValidIdentifierCharacter ( (char) nextChar))
						return new Token (Kind.VAR, lineNum, charPos);
				}
			}
			break;
		case 'f':		// Kind.FUNC or Kind.FALSE
			nextChar = readChar ();
			if ((char) nextChar == 'u')
			{
				lexeme += nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 'n')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if ((char) nextChar == 'c')
					{
						lexeme += (char) nextChar;
						nextChar = readChar ();
						if (isBlank (nextChar) ||
								!isValidIdentifierCharacter ( (char) nextChar))
							return new Token (Kind.FUNC, lineNum, charPos);
					}
				}
			}
			else if ((char) nextChar == 'a')		// Kind.False
			{
				nextChar = readChar ();
				if ((char) nextChar == 'l')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					if ((char) nextChar == 's')
					{
						lexeme += (char) nextChar;
						nextChar = readChar();
						if ((char) nextChar == 'e')
						{
							lexeme += (char) nextChar;
							nextChar = readChar ();
							if (isBlank (nextChar) || 
									 !isValidIdentifierCharacter ( (char) nextChar))
								return new Token (Kind.FALSE, lineNum, charPos);
						}
					}
				}
			}
			break;
		case 'i':	// Kind.If
			nextChar = readChar ();
			if ((char) nextChar == 'f')
			{
				lexeme += (char) nextChar;
				nextChar = readChar();
				if (isBlank (nextChar) || 
						 !isValidIdentifierCharacter ( (char) nextChar))
					return new Token (Kind.IF, lineNum, charPos);
			}
			break;
		case 'e':	// Kind.ELSE
			nextChar = readChar();
			if ((char) nextChar == 'l')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 's')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if ((char) nextChar == 'e')
					{
						lexeme += (char) nextChar;
						nextChar = readChar ();
						if (isBlank (nextChar) || 
								 !isValidIdentifierCharacter ( (char) nextChar))
							return new Token (Kind.ELSE, lineNum, charPos);
					}
				}
			}
			break;
		case 'w' :	// Kind.While 
			nextChar = readChar ();
			if ((char) nextChar == 'h')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 'i')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if ((char) nextChar == 'l')
					{
						lexeme += (char ) nextChar;
						nextChar = readChar ();
						if ((char) nextChar == 'e')
						{
							lexeme += (char) nextChar;
							nextChar = readChar ();
							if (isBlank (nextChar) || 
									!isValidIdentifierCharacter((char) nextChar))
								return new Token (Kind.WHILE, lineNum, charPos);
						}
					}
				}
			}
			break;
		case 't'	:	// Kind.TRUE
			nextChar = readChar();
			if ((char) nextChar == 'r')
			{
				lexeme += (char)  nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 'u')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if ((char) nextChar == 'e')
					{
						lexeme += (char) nextChar;
						nextChar = readChar ();
						if (isBlank (nextChar) || 
								!isValidIdentifierCharacter((char)nextChar))
							return new Token (Kind.TRUE, lineNum, charPos);
					}
				}
			}
			break;
		case 'r':		// Kind.RETURN 
			nextChar = readChar();
			if ((char) nextChar == 'e')
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if ((char) nextChar == 't')
				{
					lexeme += (char) nextChar;
					nextChar = readChar();
					if ((char) nextChar == 'u')
					{
						lexeme += (char ) nextChar;
						nextChar = readChar ();
						if ((char) nextChar == 'r')
						{
							lexeme += nextChar;
							nextChar = readChar ();
							if ((char) nextChar == 'n')
							{
								lexeme +=  (char) nextChar;
								nextChar = readChar ();
								if (isBlank (nextChar) ||  
										!isValidIdentifierCharacter ( (char) nextChar))
									return new Token (Kind.RETURN, lineNum, charPos);
							}
						}
					}
				}
			}
			break;
		default:	// regular identifier 
			nextChar = readChar ();
			while (isValidIdentifierCharacter ((char) nextChar))
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
			}
			return new Token (Kind.IDENTIFIER, lexeme, lineNum, charPos);
		}
		
		// if nothing has been return, then this must be an identifier 
		// even though it may contains keywords (EX: 'andfur')
		while (isValidIdentifierCharacter ((char) nextChar))
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
				}
		return new Token (Kind.IDENTIFIER, lexeme, lineNum, charPos);
	}


	private boolean isValidIdentifierCharacter (char character)
	{
		// IDENTIFIER	("_" | letter) { "_" | letter | digit }
		return character == '_' || isLetter (character) || isNumber (character);
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


	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
