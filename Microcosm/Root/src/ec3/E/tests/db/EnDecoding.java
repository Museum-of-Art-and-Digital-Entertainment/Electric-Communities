package ec.tests.db;

import java.io.*;
import java.util.*;

import ec.e.run.*;
import ec.e.db.*;

public class EnDecoding {

public static void main (String args[])
    throws IOException, FileNotFoundException, DBAccessException {
    String  filename    = "Coding.out";
    /*
    RtStandardEncoder.tr.traceMode(true);
    RtStandardDecoder.tr.traceMode(true);
    TypeTable.tr.traceMode(true);
    */
    
    /*
    if (args.length == 0) {
    System.out.println
        ("Usage: java ec.tests.misc.EnDecoding <iterations>");
        return;
    }
    */
    
    FileOutputStream    fileOutputStream = new FileOutputStream (filename);
    int         iter        = ((new Integer(args[0])).intValue());
    MyTestObject        controlObject1  = new MyTestObject();
    MyTestObject        controlObject2  = new MyTestObject();
    MyTestObject        decodedObject1 = controlObject1;
    MyTestObject        decodedObject2 = controlObject2;
    TypeTable       theTT   = new TypeTable (true);
    RtEncoder       encoder = new RtStandardEncoder (null, theTT);

    // Encode my object
    for (int i = 0; i < iter; i++)
    {
    encoder.encodeObject (controlObject1);
    encoder.encodeObject (controlObject2);
    System.out.println("Encoded objects #" + i + "\n" + 
        controlObject1 + "\n" + controlObject2);
    }
    byte bytes[] = encoder.getBytes();
    fileOutputStream.write (bytes);
    fileOutputStream.close ();

    FileInputStream fileInputStream = new FileInputStream (filename);
    ////bytes[] = new byte[fileInputStream.available()];
    ////fileInputStream.read (bytes);

    RtDecoder       decoder     = new RtStandardDecoder (null, theTT, bytes);

    // Decode my object
    for (int i = 0; i < iter; i++)
    {
    decodedObject1 = (MyTestObject)decoder.decodeObject ();
    decodedObject2 = (MyTestObject)decoder.decodeObject ();
    System.out.println("Decoding objects #" + i + "\n" + 
        decodedObject1 + "\n" + decodedObject2);
    }
    compare(controlObject1, decodedObject1, "one");
    compare(controlObject2, decodedObject2, "two");
    
}

    private static void compare (Object a, Object b, String s) {
    if (a.equals(b)) {
        System.out.println("Object " + s + " decoded properly");
    }
    else {
        System.out.println("*** Object " + s + " mismatch");
    }

    }
}

class MyTestObject {
    int x = 5;
    int xs[] = new int[5];
    String s = "Hello";
    String ss[] = new String[2];
    boolean foo = true;
    
    MyTestObject () {
        for (int i = 0; i < 5; i++) {
            xs[i] = i;
        }
        ss[0] = "Zero";
        ss[1] = "One";
    }

    public boolean equals(Object other) {
        if (other == null) return false;
        MyTestObject ot = null;
        try {
            ot = (MyTestObject)other;
        }
        catch (ClassCastException e) {
            return false;
        }
        return ((x == ot.x) && s.equals(ot.s) && (foo == ot.foo));
    }
    
    public String toString() {
        return ("MyTestObject: " + x + ", " + xs + ", " + s + ", " + ss + ", " + foo);
    }
}
