 package mips;

import java.util.HashMap;

import crux.Symbol;
import types.*;

public class ActivationRecord
{
    private static int fixedFrameSize = 2*4;
    private ast.FunctionDefinition func;
    private ActivationRecord parent;
    private int stackSize;
    private HashMap<Symbol, Integer> locals;
    private HashMap<Symbol, Integer> arguments;
    
    
  //  A function's activation record stores bookkeeping information such as the
  // return address and caller frame pointer and contains additional space for 
  // local variables
    
    public static ActivationRecord newGlobalFrame()
    {
        return new GlobalFrame();
    }
    
    protected static int numBytes(Type type)
    {
        if (type instanceof IntType)
            return 4;
        if (type instanceof FloatType)
            return 4;
        if (type instanceof ArrayType) 
        {
            ArrayType aType = (ArrayType)type;
            return aType.extent() * numBytes(aType.base());
        }
        if (type instanceof BoolType)
        	return 4;
        if (type instanceof TypeList)
        {
        	int total = 0;
        	for (types.Type t : (TypeList) type)
        		total += numBytes(t);
        	return total;
        }
        
        throw new RuntimeException("No size known for " + type);
    }
    
    protected ActivationRecord()
    {
        this.func = null;
        this.parent = null;
        this.stackSize = 0;
        this.locals = null;
        this.arguments = null;
    }
    
    public ActivationRecord(ast.FunctionDefinition fd, ActivationRecord parent)
    {
        this.func = fd;
        this.parent = parent;
        this.stackSize = 0;
        this.locals = new HashMap<Symbol, Integer>();
        
        // map this function's parameters
        this.arguments = new HashMap<Symbol, Integer>();
        int offset = 0;
        for (int i=fd.arguments().size()-1; i>=0; --i) {
            Symbol arg = fd.arguments().get(i);
            arguments.put(arg, offset);
            offset += numBytes(arg.type());
        }
    }
    
    public String name()
    {
        return func.symbol().name();
    }
    
    public ActivationRecord parent()
    {
        return parent;
    }
    
    public int stackSize()
    {
        return stackSize;
    }
    
    public void add(Program prog, ast.VariableDeclaration var)
    {
    	//prog.appendInstruction(var.symbol().type());
    	int space = numBytes(var.symbol().type());
    	stackSize += space;
    	locals.put(var.symbol(),space);
    	System.out.println("adding " + var + " to locals");
    }
    
    public void add(Program prog, ast.ArrayDeclaration array)
    {
    	int offset = numBytes(array.symbol().type());
    	stackSize += offset;
    	locals.put(array.symbol(), offset);
    }
    
    public void getAddress(Program prog, String reg, Symbol sym)
    {
    	// need to check in locals first, then arguments, then the parent's 
    	// activation record
    	if (locals.containsKey(sym))
    	{
    		// if this is in local (it is a variable), the address 
    		// is the position of the frame pointer -8 - the offset of the 
    		// variable
    		prog.appendInstruction("subi " + reg + ", $fp, " 
    						+ (-fixedFrameSize -locals.get(sym)));
    	}
    	else if (arguments.containsKey(sym))
    	{
    		// if this is an argument, the address is the position of the frame
    		// pointer, which point to the first argument + the offset of the 
    		// argument
    		prog.appendInstruction("addi " + reg + ", $fp, " + arguments.get(sym));
    		System.out.println("in getAddress() found argument: " + sym + " at offset " + arguments.get(sym));
    	}
    	else
    		parent.getAddress(prog, reg, sym);
    }
}

class GlobalFrame extends ActivationRecord
{
    public GlobalFrame()
    {
    }
    
    private String mangleDataname(String name)
    {
        return "cruxdata." + name;
    }
    
    @Override
    public void add(Program prog, ast.VariableDeclaration var)
    {
    	prog.appendData(mangleDataname(var.symbol().name())
    			+ ": " + "	.space 	" +	numBytes(var.symbol().type()));
    	
    	System.out.println("adding " + var + " to global .data directive");
    }    
    
    @Override
    public void add(Program prog, ast.ArrayDeclaration array)
    {
    	prog.appendData(mangleDataname(array.symbol().name()
    			+ ": " + "	.space	" +	numBytes(array.symbol().type())));
    }
        
    @Override
    public void getAddress(Program prog, String reg, Symbol sym)
    {
    	System.out.println("retriving address " + sym + " in global and store into " + reg);
    	prog.appendInstruction("la " + reg + ", " + mangleDataname(sym.name()));
    }
}
