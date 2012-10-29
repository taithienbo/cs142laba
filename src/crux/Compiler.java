package crux;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Compiler {
	public static String studentName = "Tai Bo";
	public static String studentID = "53907660";
	public static String uciNetID = "tbo";

	public static void main(String[] args)
	{
		String sourceFilename = args[0];

		Scanner s = null;
		try {
			s = new Scanner(new FileReader(sourceFilename));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
			System.exit(-2);
		}

		Parser p;
		try 
		{
			p = new Parser(s);
			ast.Command syntaxTree = p.parse();
			if (p.hasError()) {
				System.out.println("Error parsing file " + sourceFilename);
				System.out.println(p.errorReport());
				System.exit(-3);
			}

			ast.PrettyPrinter pp = new ast.PrettyPrinter();
			syntaxTree.accept(pp);
			System.out.println(pp.toString());
		}

		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

