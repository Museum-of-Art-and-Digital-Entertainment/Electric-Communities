package ec.pl.examples.lamp;

import netscape.util.Archiver;
import netscape.util.ClassInfo;
import netscape.util.Codable;
import netscape.util.CodingException;
import netscape.util.Decoder;
import netscape.util.Encoder;
import netscape.util.Unarchiver;

import ec.e.cap.EEnvironment;
import ec.pl.runtime.Unum;
import ec.ifc.codable.WorldObjectState;

import java.util.Hashtable;

public class LampState extends WorldObjectState
{
	static private final int VERSION = 1;
	static private String className = null;
	
	static private final String NAME = "Name";
	static private final String STATE = "State";
	
	public String name = null;
	public boolean switchState = false;
	
	public LampState () {
	}
		
	// Returns the proper Unum for this WorldStateObject
	// Dictionary has everything a boy could want to parameterize
	// data needed to initialize the prime presences.
	public Object getWorldObject (Hashtable dictionary) {
		EEnvironment env = (EEnvironment)dictionary.get("environment");
		Unum lamp = ui$_Lamp_ui_.createUnum(env, this);
		// If there were any sub Una contained within, we would
		// have their StateObject in the instance var (which
		// unfortunately implies it is typed "Object" not Unum)
		// and we would call it's getWorldObject after putting 
		// ourselves in the dictionary under a well known name
		return lamp;
	}
			
	public void describeClassInfo (ClassInfo info) {
		super.describeClassInfo(info);
		if (className == null) className = getClass().getName();
		info.addClass(className, VERSION);
		info.addField(NAME, Codable.STRING_TYPE);
		info.addField(STATE, Codable.BOOLEAN_TYPE);
	}
	
	public void encode (Encoder encoder) throws CodingException {
		super.encode(encoder);
		encoder.encodeString(NAME, name);
		encoder.encodeBoolean(STATE, switchState);
	}
	
	public void decode (Decoder decoder) throws CodingException {
		super.decode(decoder);
		name = decoder.decodeString(NAME);
		switchState = decoder.decodeBoolean(STATE);
	}	
}

 
