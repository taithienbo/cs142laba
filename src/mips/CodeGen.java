package mips;

import java.util.regex.Pattern;

import ast.*;
import types.*;

public class CodeGen implements ast.CommandVisitor {
    
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
}
