package types;

import java.util.HashMap;
import java.util.Iterator;

import ast.Addition;
import ast.AddressOf;
import ast.ArrayDeclaration;
import ast.Assignment;
import ast.Call;
import ast.Command;
import ast.CommandVisitor;
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
import crux.Symbol;
import crux.Token;

public class TypeChecker implements CommandVisitor 
{
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;
    
    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    
    private String getInvalidSignatureError()
    {
    	return "Function main has invalid signature.";
    }
    
    
    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) 
        {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) 
    {
       Iterator<Expression> i = node.iterator();
       while (i.hasNext())
       {
    	   Expression exp = i.next();
    	   exp.accept(this);
       }
    }

    @Override
    public void visit(DeclarationList node) 
    {
        Iterator<Declaration> i = node.iterator();
        while (i.hasNext())
        {
        	Declaration declaration = i.next();
        	declaration.accept(this);
        }
    }

    @Override
    public void visit(StatementList node)
    {
       Iterator<Statement> i = node.iterator();
       while (i.hasNext())
       {
    	   Statement statement = i.next();
    	   statement.accept(this);
       }
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
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(FunctionDefinition node)
    {
    
    	Symbol funcSymbol = node.function();
    	Type funcType = funcSymbol.type();
    	
    	if (funcSymbol.name().equals("main"))
    	{
    		// check that function "main" has type Void
    		if (!(funcType instanceof VoidType))
    		{
    			reportError(node.lineNumber(), node.charPosition(),
    					getInvalidSignatureError());
    		}
    	}
    	else	// check that function actually returns the correct type
    	{
    		
    	}
    	

    }

    @Override
    public void visit(Comparison node)
    {
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
    public void visit(LogicalAnd node) 
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalOr node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalNot node) 
    {
        throw new RuntimeException("Implement this");
    }
    
    @Override
    public void visit(Dereference node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Index node)
    {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Assignment node) 
    {
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
        put(node, new ErrorType(node.message()));
    }
}
