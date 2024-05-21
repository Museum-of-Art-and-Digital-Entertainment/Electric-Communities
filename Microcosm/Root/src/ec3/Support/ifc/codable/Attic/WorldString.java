package ec.ifc.codable;

import netscape.util.ClassInfo;
import netscape.util.Codable;
import netscape.util.CodingException;
import netscape.util.Decoder;
import netscape.util.Encoder;

public class WorldString implements Codable
{
	static private final int VERSION = 1;
	static private String className = null;
	
	static private final String STRING = "String";
	
	private String string;
	
	public WorldString () {
	}

	public void setString(String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
	
	public void describeClassInfo (ClassInfo info) {
		if (className == null) className = getClass().getName();
		info.addClass(className, VERSION);
		info.addField(STRING, Codable.STRING_TYPE);
	}
	
	public void encode (Encoder encoder) throws CodingException {
		encoder.encodeString(STRING, string);
	}
	
	public void decode (Decoder decoder) throws CodingException {
		string = decoder.decodeString(STRING);
	}	

	public void finishDecoding () throws CodingException {
	}
}
