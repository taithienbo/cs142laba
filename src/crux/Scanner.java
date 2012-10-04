package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class Scanner implements Iterable<Token> {
	public static String studentName = "TODO: YOUR NAME";
	public static String studentID = "TODO: Your 8-digit id";
	public static String uciNetID = "TODO: uci-net id";

	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
	private Reader input;

	Scanner(Reader reader) throws IOException
	{
		// TODO: initialize the Scanner
		this.input = reader;

		// initialize nextChar with the first character
		nextChar = readChar();

	}	

	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	//
	private int readChar() throws IOException 
	{
		// advance to the next character to get ready for the next call
		// before returning nextChar
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
		// TODO: implement this
		if (nextChar == -1)	// check for EOF 
			return Token.EOF (lineNum, charPos);

		String token = "";
		int lineNum = this.lineNum;
		int charPos = this.charPos;
		
		while (!isBlank (nextChar))
		{
			switch ((char) nextChar)
			{
			case 

			}
			nextChar = readChar();
		}
	}




	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
