package ec.misc;

import netscape.util.Hashtable;
import netscape.util.Enumeration;

public class NetscapeHashtable extends Hashtable
{
	public NetscapeHashtable () {
		super();
	}
	
	public NetscapeHashtable (int capacity) {
		super(capacity);
	}
	
	public NetscapeEnumeration getKeys () {
		return new NetscapeEnumeration(keys());
	}

	public NetscapeEnumeration getElements () {
		return new NetscapeEnumeration(elements());
	}
}

