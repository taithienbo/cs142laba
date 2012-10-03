package crux;

import java.io.FileReader;

import java.io.IOException;


public class Compiler {
    public static String studentName = "Tai Bo";
    public static String studentID = "53907660";
    public static String uciNetID = "tbo";
	
	public static void main(String[] args)
	{
        String sourceFile = args[0];
        Scanner s = null;

        try 
        {
            s = new Scanner(new FileReader(sourceFile));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
            System.exit(-2);
        }

        Token t;
		try 
		{
			t = s.next();
	        while ( t.kind != Token.Kind.EOF/* t is not the EOF token */) 
	        {
	                System.out.println(t);
	                t = s.next();
	        }
	        System.out.println(t);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
