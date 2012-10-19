package crux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable
{
	private int depth;
	private ArrayList<Map<String, Symbol>> table;
	private SymbolTable parent;

	public SymbolTable(SymbolTable parent)
	{
		this.parent = parent;
		table = new ArrayList<Map<String, Symbol>>();
	}

	private boolean haveParent()
	{
		return parent != null;
	}


	public void increaseDepth()
	{
		depth++;
		table.add(new HashMap<String, Symbol>());
	}


	public void decreseDepth()
	{
		depth--;
		table.remove(depth);
	}


	public int getDepth()
	{
		return depth;
	}


	private Map<String, Symbol> getSymbolsAtDepth (int depth)
	{
		return table.get(depth);
	}

	public Symbol lookup(String name) throws SymbolNotFoundError
	{
		if (!getSymbolsAtDepth(depth).containsKey(name))
			throw new SymbolNotFoundError(name);

		return getSymbolsAtDepth(depth).get(name);
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


		if (haveParent())
		{
			sb.append(parent.toString());
		}
		String indent = new String();
		for (int i = 0; i < depth; i++) {
			indent += "  ";
		}

		if (!table.isEmpty())
		{
			for (Map<String, Symbol> symbols : table)
			{
				for (Symbol symbol : symbols.values())
				{
					sb.append(indent + symbol.toString() + "\n");
				}
			}
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
