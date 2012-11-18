package types;

public class BoolType extends Type {
    
    public BoolType()
    {
    }
    
    @Override
    public String toString()
    {
        return "bool";
    }

    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof BoolType))
            return false;
        
        return true;
    }
    
    
    @Override
    public Type deref()
    {
    	return this;
    }
    
    
  @Override
  public Type and(Type that)
  {
	  if (! (that instanceof BoolType))
		  return super.and(that);
	  return this;
  }
  
  
  @Override
  public Type or(Type that)
  {
	  if (! (that instanceof BoolType))
		  return super.or(that);
	  return this;
  }
  
  
  @Override
  public Type not()
  {
	  return this;
  }
  
  
  @Override
  public Type assign(Type source)
  {
	  if (! (source instanceof BoolType))
		  return super.assign(source);
	  return this;
  }
}    
