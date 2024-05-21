package ec.util;



public final class InspectorType
{
	static final public InspectorType BOOLEAN = new InspectorType ("boolean");
	static final public InspectorType BYTE    = new InspectorType ("byte");
	static final public InspectorType CHAR    = new InspectorType ("char");
	static final public InspectorType DOUBLE  = new InspectorType ("double");
	static final public InspectorType FLOAT   = new InspectorType ("float");
	static final public InspectorType INT     = new InspectorType ("int");
	static final public InspectorType LONG    = new InspectorType ("long");
	static final public InspectorType OBJECT  = new InspectorType ("Object");
	static final public InspectorType SHORT   = new InspectorType ("short");

	private String label;

	private InspectorType (String theLabel)
	{
		label = theLabel;
	}

	public String toString ()
	{
		return (label);
	}
}



public interface InspectorElement
{
	InspectorType getType ();
	String getName ();
	Object getValue ();
	void setValue (Object newValue);

	boolean getBooleanValue ();
	byte getByteValue ();
	char getCharValue ();
	double getDoubleValue ();
	float getFloatValue ();
	int getIntValue ();
	long getLongValue ();
	short getShortValue ();

	void setBooleanValue (boolean newValue);
	void setByteValue (byte newValue);
	void setCharValue (char newValue);
	void setDoubleValue (double newValue);
	void setFloatValue (float newValue);
	void setIntValue (int newValue);
	void setLongValue (long newValue);
	void setShortValue (short newValue);
}



public class StandardInspectorElement implements InspectorElement
{
	private InspectorType type;
	private String name;
	private (->Object) getter;
	private (Object->void) setter;

	public StandardInspectorElement (
		InspectorType theType, String theName, (->Object) theGetter,
		(Object->void) theSetter)
	{
		type = theType;
		name = theName;
		getter = theGetter;
		setter = theSetter;
	}

	public String toString ()
	{
		return (type.toString () + " " + name + " = " + getter().toString ());
	}

	public InspectorType getType ()
	{
		return (type);
	}

	public String getName ()
	{
		return (name);
	}

	public Object getValue ()
	{
		return (getter ());
	}

	public void setValue (Object newValue)
	{
		setter (newValue);
	}

	public boolean getBooleanValue ()
	{
		if (type == InspectorType.BOOLEAN)
		{
			return (((Boolean) getValue ()).booleanValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public byte getByteValue ()
	{
		if (type == InspectorType.BYTE)
		{
			return ((byte) ((Integer) getValue ()).intValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public char getCharValue ()
	{
		if (type == InspectorType.CHAR)
		{
			return (((Character) getValue ()).charValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public double getDoubleValue ()
	{
		if (type == InspectorType.DOUBLE)
		{
			return (((Double) getValue ()).doubleValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public float getFloatValue ()
	{
		if (type == InspectorType.FLOAT)
		{
			return (((Float) getValue ()).floatValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public int getIntValue ()
	{
		if (type == InspectorType.INT)
		{
			return (((Integer) getValue ()).intValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public long getLongValue ()
	{
		if (type == InspectorType.LONG)
		{
			return (((Long) getValue ()).longValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public short getShortValue ()
	{
		if (type == InspectorType.SHORT)
		{
			return ((short) ((Integer) getValue ()).intValue ());
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setBooleanValue (boolean newValue)
	{
		if (type == InspectorType.BOOLEAN)
		{
			setValue (new Boolean (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setByteValue (byte newValue)
	{
		if (type == InspectorType.BYTE)
		{
			setValue (new Integer (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setCharValue (char newValue)
	{
		if (type == InspectorType.CHAR)
		{
			setValue (new Character (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setDoubleValue (double newValue)
	{
		if (type == InspectorType.DOUBLE)
		{
			setValue (new Double (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setFloatValue (float newValue)
	{
		if (type == InspectorType.FLOAT)
		{
			setValue (new Float (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setIntValue (int newValue)
	{
		if (type == InspectorType.INT)
		{
			setValue (new Integer (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setLongValue (long newValue)
	{
		if (type == InspectorType.LONG)
		{
			setValue (new Long (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}

	public void setShortValue (short newValue)
	{
		if (type == InspectorType.SHORT)
		{
			setValue (new Integer (newValue));
		}
		else
		{
			throw (new RuntimeException ("Bad value type"));
		}
	}
}



public class Inspector
{
	private InspectorElement[] elements;

	public Inspector (InspectorElement[] theElements)
	{
		// BUG--we should copy this to insulate from changes
		elements = theElements;
	}

	public int getSize ()
	{
		return (elements.length);
	}

	public InspectorElement get (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n]);
	}

	public String getName (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getName ());
	}

	public InspectorType getType (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getType ());
	}

	public Object getValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getValue ());
	}

	public void setValue (int n, Object newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setValue (newValue);
	}

	public boolean getBooleanValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getBooleanValue ());
	}

	public byte getByteValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getByteValue ());
	}

	public char getCharValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getCharValue ());
	}

	public double getDoubleValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getDoubleValue ());
	}

	public float getFloatValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getFloatValue ());
	}

	public int getIntValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getIntValue ());
	}

	public long getLongValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getLongValue ());
	}

	public short getShortValue (int n)
		throws ArrayIndexOutOfBoundsException
	{
		return (elements[n].getShortValue ());
	}

	public void setBooleanValue (int n, boolean newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setBooleanValue (newValue);
	}

	public void setByteValue (int n, byte newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setByteValue (newValue);
	}

	public void setCharValue (int n, char newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setCharValue (newValue);
	}

	public void setDoubleValue (int n, double newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setDoubleValue (newValue);
	}

	public void setFloatValue (int n, float newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setFloatValue (newValue);
	}

	public void setIntValue (int n, int newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setIntValue (newValue);
	}

	public void setLongValue (int n, long newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setLongValue (newValue);
	}

	public void setShortValue (int n, short newValue)
		throws ArrayIndexOutOfBoundsException
	{
		elements[n].setShortValue (newValue);
	}

	public String toString ()
	{
		String result = "{ ";
		for (int i = 0; i < elements.length; i++)
		{
			result += elements[i].toString () + "; ";
		}
		result += "}";

		return (result);
	}
}



public interface Inspectable
{
	Inspector createInspector ();
}
