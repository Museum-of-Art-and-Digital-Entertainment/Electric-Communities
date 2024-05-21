package ec.tests.comm;

import java.util.*;
import java.net.*;
import ec.e.comm.*;

public class HelloCryptor implements RtCryptor 
{
	private Hashtable connections = new Hashtable();

	public HelloCryptor () {
	}

	public boolean startConnection (Object key, Socket socket, boolean init)
	{
		System.out.println("StartConnection for " + key);
		connections.put(key, "Ignored");
		return true;
	}

	public byte[] encrypt (byte message[], Object key) {
		System.out.println("Encrypt called for " + key);
		int i;
		for (i = 0; i < message.length; i++) {
			message[i] = (byte) ((int)message[i] ^ 0x37);
		}
		return message;
	}

	public byte[] decrypt (byte message[], Object key) {
		System.out.println("Decrypt called for " + key);
		int i;
		for (i = 0; i < message.length; i++) {
			message[i] = (byte) ((int)message[i] ^ 0x37);
		}
		return message;
	}
}
