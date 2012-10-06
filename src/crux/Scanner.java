package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import crux.Token.Kind;

public class Scanner implements Iterable<Token> {
	public static String studentName = "TODO: YOUR NAME";
	public static String studentID = "TODO: Your 8-digit id";
	public static String uciNetID = "TODO: uci-net id";

	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
	private Reader input;


	// to determine whether the next char to nextChar
	// has already been read. Since sometimes, it is 
	// necessary to lookahead at the next character
	// to determine if the current character represents
	// a valid token
	boolean nextCharRead = false;	
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

		nextChar = input.read();
		updateCharPosition();
		return nextChar;
	}


	private void skipBlankSpace () throws IOException
	{
		//	System.out.println("skipBlankSpace()," +
		//			" nextChar: " + nextChar);
		while (isBlank (nextChar))
		{
			nextChar = input.read ();
			updateCharPosition ();		
		}
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
			charPos = 0;
		}
		else
			charPos++;
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


		// TODO: implement this

		int lineNum = this.lineNum;
		int charPos = this.charPos;

		Token token;
		switch ( (char)nextChar)
		{
		case '(':
			token = Token.OPEN_PAREN(lineNum, charPos);
			nextChar = readChar();
			break;
		case ')':
			token = Token.CLOSE_PAREN(lineNum, charPos);
			nextChar = readChar();
			break;
		case '{':
			token = Token.OPEN_BRACE(lineNum, charPos);
			nextChar = readChar();
			break;
		case '}':
			token = Token.CLOSE_BRACE(lineNum, charPos);
			nextChar = readChar();
			break;
		case '[':
			token = Token.OPEN_BRACKET(lineNum, charPos);
			nextChar = readChar();
			break;
		case ']':
			token = Token.CLOSE_BRACKET(lineNum, charPos);
			nextChar = readChar();
			break;
		case '+':
			token = Token.ADD(lineNum, charPos);
			break;
		case '-':
			token = Token.SUB(lineNum, charPos);
			nextChar = readChar();
			break;
		case '*':
			token = Token.MUL(lineNum, charPos);
			nextChar = readChar();
			break;
		case '/':
			token = Token.DIV(lineNum, charPos);
			nextChar = readChar();
			break;
		case ',':
			token = Token.COMMA(lineNum, charPos);
			nextChar = readChar();
			break;
		case ';':
			token = Token.SEMICOLON(lineNum, charPos);
			nextChar = readChar();
			break;
		default:
			if (isNumber ((char) nextChar))
				token = parseNumber (lineNum, charPos);
			else
				token = parseMultiChar (lineNum, charPos);
		}

		return token;
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

			return Token.FLOAT (number, lineNum, charPos);
		}
		else
			return Token.INTEGER (number, lineNum, charPos);
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
				return Token.GREATER_EQUAL(lineNum, charPos);
			}
			else
				return Token.GREATER_THAN(lineNum, charPos);

		case '<':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return Token.LESSER_EQUAL(lineNum, charPos);
			}
			else 
				return Token.LESSER_THAN(lineNum, charPos);

		case '!':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return Token.NOT_EQUAL(lineNum, charPos);
			}
			else
				return Token.ERROR(lineNum, charPos, "!");

		case '=':
			nextChar = readChar();
			if (((char) nextChar) == '=')
			{
				nextChar = readChar();
				return Token.EQUAL(lineNum, charPos);
			}
			else
				return Token.ASSIGN(lineNum, charPos);
		case ':':
			nextChar = readChar ();
			if (((char) nextChar) == ':')
			{
				nextChar = readChar ();
				return Token.CALL (lineNum, charPos);
			}
			else
				return Token.COLON (lineNum, charPos);
		default:
			return parseIdentifier (lineNum, charPos);
		}
	}

	private Token parseIdentifier (int lineNum, int charPos) throws IOException
	{
		String lexeme = "" + (char) nextChar;
		switch ((char) nextChar)
		{
		case 'a':
			nextChar = readChar ();
			if (((char) nextChar ) == 'n')		// check for Kind.AND
			{
				lexeme += (char) nextChar;
				nextChar = readChar ();
				if (((char) nextChar) == 'd')
				{
					lexeme += (char) nextChar;
					nextChar = readChar ();
					return new Token (Token.Kind.AND, lineNum, charPos);
				}
			}
			else if (((char) nextChar) == 'a')	// check for Kind.ARRAY
			{
				nextChar = readChar ();
				if (((char) nextChar) == 'r')
				{
					lexeme += nextChar;
					nextChar = readChar ();
					if (((char) nextChar == 'r'))
					{
						lexeme += nextChar;
						nextChar = readChar ();
						if (((char) nextChar ) == 'a')
						{
							lexeme += nextChar;
							nextChar = readChar ();
							if (((char) nextChar) == 'y')
							{
								lexeme +=nextChar;
								nextChar = readChar ();
								return new Token (Kind.ARRAY, lineNum, charPos);
							}
						}
					}
				}
			}
	
		}
		String lexeme = "" + (char) nextChar;

		nextChar = readChar();

		return Token.ERROR (lineNum, charPos, lexeme);

	}


	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
