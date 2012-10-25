package crux;


import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.Map;

public class SymbolTable
{
	private int depth = 0;
	private ArrayList<Map<String, Symbol>> table;


	public SymbolTable()
	{
		table = new ArrayList<Map<String, Symbol>>();
		table.add(new LinkedHashMap<String, Symbol>());
	} 


	public void increaseDepth()
	{
		depth++;
		table.add(new LinkedHashMap<String, Symbol>());
	}


	public void decreseDepth()
	{	
		table.remove(depth--);
	}


	// currently use for JUnit testing
	public int getDepth()
	{
		return depth;
	}
	
	
	private Map<String, Symbol> getSymbolsAtDepth(int depth)
	{
		return table.get(depth);
	}
	
	
	

	public Symbol lookup(String name) throws SymbolNotFoundError
	{
		for (int i = depth; i >= 0; i--)
		{
			if (getSymbolsAtDepth(i).containsKey(name))
				return getSymbolsAtDepth(i).get(name);
		}
		throw new SymbolNotFoundError(name);
	}


	public Symbol insert(String name) throws RedeclarationError
	{
		Symbol symbol = getSymbolsAtDepth(depth).get(name);

		if (symbol != null)
			throw new RedeclarationError(symbol);

		symbol = new Symbol(name);

		getSymbolsAtDepth(depth).put(name, symbol);

		return symbol;
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		for (Symbol s : getSymbolsAtDepth(0).values())	
			sb.append(s.toString() + "\n");

		String indent = new String();

		for (int i = 1; i < table.size(); i++)
		{
			indent += "  ";
			for (Symbol s : 
				getSymbolsAtDepth(i).values())
				sb.append(indent + s.toString() + "\n");
		}

		return sb.toString();
	}
}



 class SymbolNotFoundError extends Error
{
	private static final long serialVersionUID = 1L;
	private String name;

	SymbolNotFoundError(String name)
	{
		this.name = name;
	}

	public String name()
	{
		return name;
	}
}


class RedeclarationError extends Error
{
	private static final long serialVersionUID = 1L;

	public RedeclarationError(Symbol sym)
	{
		super("Symbol " + sym + " being redeclared.");
	}
}
