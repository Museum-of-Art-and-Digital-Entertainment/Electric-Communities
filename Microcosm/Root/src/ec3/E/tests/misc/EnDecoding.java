package ec.tests.misc;

import java.io.*;
import java.util.*;

import ec.e.db.*;
import ec.e.comm.RtLauncher;

public class EnDecoding {

public static void main (String args[])
    throws IOException, FileNotFoundException, DBAccessException {
    String	filename	= "Coding.out";

	Hashtable dict = RtLauncher.makeEnvironmentDictionary(args);
	args = (String[]) dict.get("Args");

    if ((args == null) || (args.length == 0)) {
	System.out.println
	    ("Usage: java ec.tests.misc.EnDecoding <iterations>");
	return;
    }

	boolean separateTables;
	if (args.length > 1) {
		System.out.println("Using separate type tables");
		separateTables = true;
	}
	else {
		System.out.println("Using one type table");
		separateTables = false;
	}

    FileOutputStream	fileOutputStream = new FileOutputStream (filename);
    int			iter		= ((new Integer(args[0])).intValue());
    MyTestObject		controlObject1	= new MyTestObject();
    MyTestObject		controlObject2	= new MyTestObject();
    MyTestObject		decodedObject1;
    MyTestObject		decodedObject2;
	TypeTable		theTT	= new TypeTable (false);
    RtEncoder		encoder		= new RtEncoder (null, theTT, separateTables);

    // Encode my object
    for (int i = 0; i < iter; i++)
    {
	encoder.encodeObject (controlObject1);
	encoder.encodeObject (controlObject2);
	System.out.println("Encoded objects #" + i); 
    }
    fileOutputStream.write (encoder.getBytes());
    fileOutputStream.close ();

    FileInputStream	fileInputStream = new FileInputStream (filename);
    byte		bytes[] = new byte[fileInputStream.available()];
    TypeTable		decodeTT;

	if (separateTables == false) {
		System.out.println("Calling registerClasses");
		decodeTT	= theTT; 
		theTT.registerClassesInTable();
	}
	else {
		decodeTT	= new TypeTable ();
	}

    fileInputStream.read (bytes);

    RtDecoder		decoder		= new RtDecoder (null, decodeTT, bytes);

    // Decode my object
    for (int i = 0; i < iter; i++)
    {
	decodedObject1 = (MyTestObject)decoder.decodeRootObject ();
	decodedObject2 = (MyTestObject)decoder.decodeRootObject ();
	System.out.println("Decoding objects #" + i + " " + 
		decodedObject1 + " " + decodedObject2);
    }
}

}

class MyTestObject {
	int x = 5;

	public boolean equals(Object other) {
		return (((MyTestObject)other).x == x);
	}
}
