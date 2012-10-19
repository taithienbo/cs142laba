package crux;

import java.io.FileReader;
import java.io.IOException;


public class Compiler {
	public static String studentName = "Tai Bo";
	public static String studentID = "53907660";
	public static String uciNetID = "tbo";

	public static void main(String[] args)
	{
		String sourceFilename = args[0];

		Scanner s = null;
		try 
		{
			s = new Scanner(new FileReader(sourceFilename));
			Parser p;
			p = new Parser(s);
			p.parse();
			if (p.hasError()) 
			{
				System.out.println("Error parsing file.");
				System.out.println(p.errorReport());
				System.exit(-3);
			}
			System.out.println("Crux program successfully parsed.");
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
			System.exit(-2);
		}



	}
}

