package ec.ifc.util;

import netscape.util.Enumeration;

public class NetscapeEnumeration implements Enumeration
{
	private Enumeration enumeration;
	
	NetscapeEnumeration (Enumeration enumeration) {
		this.enumeration = enumeration;
	}
	
	public Object nextElement () {
		return enumeration.nextElement();
	}
	
	public boolean hasMoreElements () {
		return enumeration.hasMoreElements();
	}
}
