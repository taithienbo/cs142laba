package types;

public class AddressType extends Type {
    
    private Type base;
    
    public AddressType(Type base)
    {
        this.base = base;
    }
    
    public Type base()
    {
        return base;
    }

    @Override
    public String toString()
    {
        return "Address(" + base + ")";
    }

    @Override
    public boolean equivalent(Type that) 
    {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;
        
        AddressType aType = (AddressType)that;
        return this.base.equivalent(aType.base);
    }
    
    @Override                                                                                                        
    public Type index(Type that)                                                                                     
    {                                                                                                                
      if (!(base instanceof ArrayType))                                                                        
    	  return super.index(that);                                                                        
      return new AddressType(base.index(that));                                                                
    }

    @Override
    public Type deref()
    {
    	// return whatever type the base is
    	return base;
    }
    
    @Override
    public Type compare(Type that)
    {
        if (!(that instanceof AddressType))
            return super.compare(that);
        return base.compare(((AddressType) that).base());
    }
}
