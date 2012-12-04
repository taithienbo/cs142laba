package mips;

import types.TypeChecker;
import ast.Addition;
import ast.AddressOf;
import ast.ArrayDeclaration;
import ast.Assignment;
import ast.Call;
import ast.Command;
import ast.Comparison;
import ast.Declaration;
import ast.DeclarationList;
import ast.Dereference;
import ast.Division;
import ast.Expression;
import ast.ExpressionList;
import ast.FunctionDefinition;
import ast.IfElseBranch;
import ast.Index;
import ast.LiteralBool;
import ast.LiteralBool.Value;
import ast.LiteralFloat;
import ast.LiteralInt;
import ast.LogicalAnd;
import ast.LogicalNot;
import ast.LogicalOr;
import ast.Multiplication;
import ast.Return;
import ast.Statement;
import ast.StatementList;
import ast.Subtraction;
import ast.VariableDeclaration;
import ast.WhileLoop;
import types .*;

public class CodeGen implements ast.CommandVisitor {
<<<<<<< HEAD

	private StringBuffer errorBuffer = new StringBuffer();
	private TypeChecker tc;
	private Program program;
	private ActivationRecord currentFunction;

	private int regCounter = 0;

	private String makeTempRegister(int regNum)
	{
		return "$t" + regNum;
	}

	private String makeSavedRegister(int regNum)
	{
		return "$s" + regNum;
	}

	private String makeArgumentResiterCounter(int regNum)
	{
		return "$a" + regNum;
	}
	
	private String makeFloatRegister(int regNum)
	{
		return "$f" + regNum;
	}


	public CodeGen(TypeChecker tc)
	{
		this.tc = tc;
		this.program = new Program();

	}

	public boolean hasError()
	{
		return errorBuffer.length() != 0;
	}

	public String errorReport()
	{
		return errorBuffer.toString();
	}

	private class CodeGenException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public CodeGenException(String errorMessage) {
			super(errorMessage);
		}
	}

	public boolean generate(Command ast)
	{
		try 
		{
			currentFunction = ActivationRecord.newGlobalFrame();
			ast.accept(this);
			return !hasError();
		} 
		catch (CodeGenException e) 
		{
			return false;
		}
	}

	public Program getProgram()
	{
		return program;
	}

	@Override
	public void visit(ExpressionList node) 
	{
		for (Expression e : node)
			e.accept(this);
	}

	@Override
	public void visit(DeclarationList node) 
	{
		for (Declaration d : node)
			d.accept(this);
		// throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(StatementList node) 
	{
		for (Statement statement : node)
			statement.accept(this);   	
	}

	@Override
	public void visit(AddressOf node) 
	{	
		// AddressOf either denotes the address of the variable or in the case 
		// of array, it denotes the value for indexing into the array
		// In either case, we treat AddressOf as an int or float and push
		// onto the stack
		String rd = makeTempRegister(regCounter);
		// push the address onto the stack
		program.pushInt(rd);
	}

	@Override
	public void visit(LiteralBool node) 
	{
		// need to determine the boolean value to push onto the stack
		Value boolVal = node.value();
		String rd = makeTempRegister(regCounter);
		if (boolVal == Value.FALSE)		// put -1 to represent false
			program.appendInstruction("li " + rd + ", " + -1  
					+ " # " + rd + " = -1");
		else
			program.appendInstruction("li " + rd + ", " + 1  
					+ " # " + rd + " = 1");

		// push the result back onto the stack
		program.pushInt(rd);
	}

	@Override
	public void visit(LiteralFloat node) 
	{
		String rd = makeTempRegister(regCounter);
		
		// add the float value into register
		// not sure if this works
		program.appendInstruction("li.s " + rd + ", " + node.value()
				+ " # " + rd + " = " + node.value());

		// push the result back onto the stack
		program.pushFloat(rd);
	}

	@Override
	public void visit(LiteralInt node) 
	{
		String rd = makeTempRegister(regCounter);
		program.appendInstruction("add " + rd + ", " + "$0" + node.value()
					+ " # " + rd + " = " + node.value());
		program.pushInt(rd);
	}

	@Override
	public void visit(VariableDeclaration node) 
	{
		// Each time the visitor encounters an array or variable declaration it
		// notifies the current ActivationRecord object, which records an 
		// offset (from the frame pointer) where the symbol will be stored at 
		// runtime.
		currentFunction.add(program, node);
	}

	@Override
	public void visit(ArrayDeclaration node) 
	{
		currentFunction.add(program, node);	
	}

	@Override
	public void visit(FunctionDefinition node) 
	{
		// Each time the CodeGen visitor encounters a FunctionDefinition, it
		// can create a new ActivationRecord object to model that function's
		// scope. ActivationRecord's are linked via a parent field, that allows
		// symbol lookup to chain upwards. If a symbol isn't found in the 
		// inner-most scope, a parent ActivationRecord supplies the address.
		currentFunction = new ActivationRecord(node, currentFunction);
	}

	@Override
	public void visit(Addition node) 
	{
		// push left and right sides onto stack
		int regLeft = regCounter;
		node.leftSide().accept(this);
		int regRight = ++regCounter;
		node.rightSide().accept(this);

		// retrieve left and right sides from stack
		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		String rd;
		String rs;
	
		if (leftType instanceof IntType
				&& rightType instanceof IntType)
		{
			rd = makeTempRegister(regLeft);
			rs = makeTempRegister(regRight);
			
			// pop right side
			program.popInt(rs);
			// pop left side
			program.popInt(rd);
			// do the addition
			program.appendInstruction("add " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			// push the result back onto stack
			program.pushInt(rd);
		}
		else if (leftType instanceof FloatType
					&& rightType instanceof FloatType)
		{
			rd = makeFloatRegister(regLeft);
			rs = makeFloatRegister(regRight);
			program.popFloat(rs);
			program.popFloat(rd);
			program.appendInstruction("add.s " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			program.pushFloat(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " + " + rightType); 
		
		regCounter = 0;
	}

	@Override
	public void visit(Subtraction node) 
	{
		// push left and right sides onto stack
		int regLeft = regCounter;
		node.leftSide().accept(this);
		int regRight = ++regCounter;
		node.rightSide().accept(this);

		// retrieve left and right sides from stack
		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		String rd;
		String rs;
	
		if (leftType instanceof IntType
				&& rightType instanceof IntType)
		{
			rd = makeTempRegister(regLeft);
			rs = makeTempRegister(regRight);
			
			// pop right side
			program.popInt(rs);
			// pop left side
			program.popInt(rd);
			// do the addition
			program.appendInstruction("sub " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			// push the result back onto stack
			program.pushInt(rd);
		}
		else if (leftType instanceof FloatType
					&& rightType instanceof FloatType)
		{
			rd = makeFloatRegister(regLeft);
			rs = makeFloatRegister(regRight);
			program.popFloat(rs);
			program.popFloat(rd);
			program.appendInstruction("sub.s " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			program.pushFloat(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " - " + rightType); 
		
		regCounter = 0;
	}

	@Override
	public void visit(Multiplication node) 
	{
		// push left and right sides onto stack
		int regLeft = regCounter;
		node.leftSide().accept(this);
		int regRight = ++regCounter;
		node.rightSide().accept(this);

		// retrieve left and right sides from stack
		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		String rd;
		String rs;
	
		if (leftType instanceof IntType
				&& rightType instanceof IntType)
		{
			rd = makeTempRegister(regLeft);
			rs = makeTempRegister(regRight);
			
			// pop right side
			program.popInt(rs);
			// pop left side
			program.popInt(rd);
			// do the addition
			program.appendInstruction("mul " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			// push the result back onto stack
			program.pushInt(rd);
		}
		else if (leftType instanceof FloatType
					&& rightType instanceof FloatType)
		{
			rd = makeFloatRegister(regLeft);
			rs = makeFloatRegister(regRight);
			program.popFloat(rs);
			program.popFloat(rd);
			program.appendInstruction("mul.s " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			program.pushFloat(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " + " + rightType); 
		
		regCounter = 0;
	}


	@Override
	public void visit(Division node) 
	{
		// push left and right sides onto stack
		int regLeft = regCounter;
		node.leftSide().accept(this);
		int regRight = ++regCounter;
		node.rightSide().accept(this);

		// retrieve left and right sides from stack
		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		String rd;
		String rs;
	
		if (leftType instanceof IntType
				&& rightType instanceof IntType)
		{
			rd = makeTempRegister(regLeft);
			rs = makeTempRegister(regRight);
			
			// pop right side
			program.popInt(rs);
			// pop left side
			program.popInt(rd);
			// do the addition
			program.appendInstruction("div " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			// push the result back onto stack
			program.pushInt(rd);
		}
		else if (leftType instanceof FloatType
					&& rightType instanceof FloatType)
		{
			rd = makeFloatRegister(regLeft);
			rs = makeFloatRegister(regRight);
			program.popFloat(rs);
			program.popFloat(rd);
			program.appendInstruction("div.s " + rd + ", " 
					+ rd + ", " + rs
					+ " # " + rd + " = " 
					+  rd + " + " +  rs);
			program.pushFloat(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " / " + rightType); 
		
		regCounter = 0;
	}

	// Bitwise and syntax: and $d, $s, $t
	// $d = $s and $t
	@Override
	public void visit(LogicalAnd node) 
	{
		// push left and right sides onto stack
		String rd = makeTempRegister(regCounter++);
		node.leftSide().accept(this);
		String rs = makeTempRegister(regCounter);
		node.rightSide().accept(this);

		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		// pop left and right sides from stack
		if (rightType instanceof BoolType
				&& leftType instanceof BoolType)
		{
			// pop the right side
			program.popInt(rs);
			// pop the left side
			program.popInt(rd);
			// compute logical and 
			program.appendData("and " + "rd, " + "rd, " + "rs "
			 + "# " + rd + " = " + rd + " and " + rs);
			//push the result back onto stack
			program.pushInt(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " and " 
										+ rightType);

		regCounter = 0;

	}

	@Override
	public void visit(LogicalOr node) 
	{
		// push left and right sides onto stack
		String rd = makeTempRegister(regCounter++);
		node.leftSide().accept(this);
		String rs = makeTempRegister(regCounter);
		node.rightSide().accept(this);
		
		Type leftType = tc.getType((Command) node.leftSide());
		Type rightType = tc.getType((Command) node.rightSide());

		// pop left and right sides from stack
		if (rightType instanceof BoolType
				&& leftType instanceof BoolType)
		{
			// pop the right side
			program.popInt(rs);
			// pop the left side
			program.popInt(rd);
			// compute logical or
			program.appendData("or " + "rd, " + "rd, " + "rs "
			 + "# " + rd + " = " + rd + " or " + rs);
			//push the result back onto stack
			program.pushInt(rd);
		}
		else
			throw new RuntimeException("cannot compute " + leftType + " or " 
										+ rightType);

		regCounter = 0;
	}

	@Override
	public void visit(LogicalNot node) 
	{
		String rd = makeTempRegister(regCounter++);
		// push the boolean expression onto the stack
		node.expression().accept(this);

		// MIPS does not support bitwise negation (this can be performed with 
		// two instructions: setting a register's value to -1 with addi and
		// using xor on the register to be negated and the register with -1 in
		// it, which is storing all 1's).
		String rs = makeTempRegister(regCounter);
		program.appendInstruction("addi " + rs 
				+ ", $0, " + -1 
				+ " # " + rs + " = "
				+ "-1");

		program.pushInt(rs);

		// pop the -1 from stack 
		program.popInt(rs);
		// pop the value of the register to be negated from stack 
		program.popInt(rd);

		// do the xor
		program.appendInstruction("xor " + rd + ", " + rd + ", "
				+ rs + " # " + rd + " = " 
				+ rd + "xor " + rs);

		// push the result back onto the stack
		program.pushInt(rd);
		
		regCounter = 0;
	}

	@Override
	public void visit(Comparison node) 
	{
		String rd = makeTempRegister(regCounter++);
		// push left
		node.leftSide().accept(this);
		String rs = makeTempRegister(regCounter);
		// push right
		node.rightSide().accept(this);
		// pop right
		program.popInt(rs);
		// pop left
		program.popInt(rd);
		
		// do comparison using pseudo-instructions
		// first compute 
		
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Dereference node) 
	{
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Index node) {
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Assignment node) {
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Call node) 
	{
		// ("jal...");
		// return value
		//...("adi, $sp, $sp, #")

		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(IfElseBranch node) {
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(WhileLoop node) {
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(Return node) {
		throw new RuntimeException("Implement this");
	}

	@Override
	public void visit(ast.Error node) 
	{
		String message = "CodeGen cannot compile a " + node;
		errorBuffer.append(message);
		throw new CodeGenException(message);
	}
=======
    
    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private ActivationRecord currentFunction;

    public CodeGen(TypeChecker tc)
    {
        this.tc = tc;
        this.program = new Program();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public CodeGenException(String errorMessage) 
        {
            super(errorMessage);
        }
    }
    
    public boolean generate(Command ast)
    {
        try 
        {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);
            return !hasError();
        } 
        catch (CodeGenException e) 
        {
            return false;
        }
    }
    
    public Program getProgram()
    {
        return program;
    }

    @Override
    public void visit(Command node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Expression node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Declaration node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Statement node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ExpressionList node) 
    {
    	for (Expression expression : node)
    		expression.accept(this);
    }

    @Override
    public void visit(DeclarationList node) 
    {
       for (Declaration declaration : node)
    	   declaration.accept(this);
    }

    @Override
    public void visit(StatementList node) 
    {
       for (Statement statement : node)
    	   statement.accept(this);
    }

    @Override
    public void visit(AddressOf node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralBool node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralFloat node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralInt node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(VariableDeclaration node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ArrayDeclaration node) 
    {
    	// todo: notify current Activation Record to record an offset
    	// (from the frame pointer) where the symbol will be stored at runtime
  
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(FunctionDefinition node) 
    {
    	/**
    	 * Each time the CodeGen visitor encounters a FunctionDefinition, it
    	 * can create a new ActivationRecord object to model that function's 
    	 * scope. ActivationRecord's are linked via a parent field, that 
    	 * allows symbol lookup to chain upwards. If a symbol isn't found in 
    	 * the inner-most scope, a parent ActivationRecord supplies the address
    	 * (Because Crux does not support lexically nested functions, our 
    	 * implementation really only has 2 scope: local and gloabl.) Once the
    	 * CodGen visitor has finished assembling the function body, it can pop
    	 * the current ActivationRecord and restore the previous one.
    	 */
    	
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Addition node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Subtraction node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Multiplication node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Division node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalAnd node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalOr node) {
        throw new RuntimeException("Implement this");
    }
    
    @Override
    public void visit(LogicalNot node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Comparison node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Dereference node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Index node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Assignment node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Call node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(IfElseBranch node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(WhileLoop node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Return node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }
>>>>>>> 3341d2c63f948b644cc8abcab25d53d45e6e81d7
}
