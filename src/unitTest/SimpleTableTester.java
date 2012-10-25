package unitTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;


import crux.SymbolTable;

public class SimpleTableTester 
{
	
	private SymbolTable table;



	private SymbolTable createTableZeroDepth()
	{
		SymbolTable table = new SymbolTable();
		table.insert("tai");
		table.insert("ngan");
		
		return table;
	}
	
	@Before
	public void testSymbolTable()
	{
		table = new SymbolTable();
		table.insert("readInt");
		table.insert("readFloat");
		table.insert("printBool");
		table.insert("printInt");
		table.insert("printFloat");
		table.insert("println");
		
		assertEquals(table.getDepth(), 0);
		
		table.increaseDepth();
		
		assertEquals(table.getDepth(), 1);
		
		table.insert("foo");
	}
	
	
	@Test 
	public void testMultipleDepth()
	{
		SymbolTable table = new SymbolTable();
		
		
		// first level
		table.insert("mino");
		// second level 
		table.insert("mina");
		try
		{
			table.lookup("tai");
			fail ("should have caught SimpleNotFoundError");
		}
		catch (Error e)
		{
			
		}
	}

	@Test()
	public void testIncreaseDepth() 
	{
		SymbolTable table = createTableZeroDepth();
		assertEquals("tai",table.lookup("tai").name());
		assertEquals(0, table.getDepth());
		table.increaseDepth();
		assertEquals(1, table.getDepth());
		
		try
		{
			table.lookup("foo");
			fail("Error should have been thrown");
		}
		catch (Error e)
		{
			
		}
		

	}

	@Test
	public void testDecreseDepth() 
	{
		SymbolTable table = createTableZeroDepth();
		assertEquals(0, table.getDepth());
		table.increaseDepth();
		assertEquals(1, table.getDepth());
		
		
		table.decreseDepth();
		assertTrue("tai".equals(table.lookup("tai").name()));
	}

	@Test
	public void testGetDepth() 
	{
		assertEquals(1, table.getDepth());
		table.decreseDepth();
		assertEquals(0, table.getDepth());
		
	}

	@Test
	public void testLookup() 
	{
		SymbolTable table = createTableZeroDepth();
		assertEquals("tai", table.lookup("tai").name());
		assertEquals("ngan", table.lookup("ngan").name());
	
	}

	@Test
	public void testInsert() 
	{
		table.insert("tai");
		assertEquals(table.lookup("tai").name(), "tai");
	}

	
	@Test 
	public void testToString()
	{
		table = new SymbolTable();
		table.insert("tai");
		table.increaseDepth();
		table.insert("ngan");
		table.decreseDepth();
		table.insert("kayla");
		
		assertEquals(table.getDepth(), 0);
	
		
		System.out.println("increasing depth...");
		table.increaseDepth();
		
		table.insert("dad");
		table.insert("mom");
		System.out.print(table.toString());
		
	}


}
