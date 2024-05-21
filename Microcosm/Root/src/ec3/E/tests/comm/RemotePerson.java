
package ec.tests.comm;

import java.lang.String;
import ec.e.db.*;


interface XXXRemoteSuperInterface {
}

interface XXXRemoteInterface extends XXXRemoteSuperInterface
{
	String remotely ();
}

class XXXRemoteSuperSuper {
}

class XXXRemoteSuper extends XXXRemoteSuperSuper {
	public String namely () { return "Yo"; }
}

public class XXXRemotePerson extends XXXRemoteSuper implements XXXRemoteInterface, RtCodeable, RtAwakeAfterDecoding
{
	String name;
	
	public String remotely () { return "Remote Yo"; }

	public XXXRemotePerson (String n) {
		name = n;	
	}

	public String classNameToEncode (RtEncoder coder) {
		return getClass().getName();
	}

	public void encode (RtEncoder coder) {
		try {
			coder.encodeObject(name);
		} catch (Exception e) {
			System.out.println("Exception occured encoding Opaque");
			e.printStackTrace();
		}
	}

	public Object decode (RtDecoder coder) {
		try {
			name = (String) coder.decodeObject();
		} catch (Exception e) {
			System.out.println("Error decoding Opaque");
			e.printStackTrace();
		}
		return this;
	}

	public void awakeAfterDecoding () {
		System.out.println(this + " woke up");
	}
	
	public String toString () {
		return ("RemotePerson:" + name);
	}
}
