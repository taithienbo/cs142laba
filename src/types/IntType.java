package types;

public class IntType extends Type 
{
	
    public IntType() 
    {
    }

    @Override
    public String toString() 
    {
        return "int";
    }

    @Override
    public Type add(Type that) 
    {
        if (!(that instanceof IntType))
            return super.add(that);
        return this;
    }

    @Override
    public Type sub(Type that)
    {
        if (!(that instanceof IntType))
            return super.sub(that);
        return this;
    }

    @Override
    public Type mul(Type that) 
    {
        if (!(that instanceof IntType))
            return super.mul(that);
        return this;
    }

    @Override
    public Type div(Type that) 
    {
        if (!(that instanceof IntType))
            return super.div(that);
        return this;
    }

    @Override
    public Type deref()
    {
    	return this;
    }
    
    @Override
    public Type compare(Type that)
    {
        if (!(that instanceof IntType))
            return super.compare(that);
        return new BoolType();
    }

    @Override
    public boolean equivalent(Type that) 
    {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        return true;
    }
    
    @Override
    public Type assign(Type source)
    {
    	if (source instanceof IntType)
    		return this;
    	return super.assign(source);
    }
}
