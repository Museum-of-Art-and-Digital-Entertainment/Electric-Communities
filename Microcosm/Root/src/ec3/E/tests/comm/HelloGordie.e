/*
  HelloGordie.e -- Version 0.1 -- Simple comm test

  Arturo Bejar & Chip Morningstar & Gordie
  Electric Communities
  19-February-1996

  Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.comm;

import ec.e.start.*;
import ec.e.cap.*;
import ec.e.net.*;
import ec.e.lang.*;
import ec.e.db.*;
import java.util.Vector;

class GordieEException extends RuntimeException {
    public GordieEException(String message) {
        super(message);
    }
}

class EverythingIsTransient
{
    transient Object _object = new Object();
    transient int _int = 1234;
    transient long _long = 12345678;
    transient short _short = 12;
    transient byte _byte = 1;
    transient boolean _boolean = true;
    transient char _char = 'c';
    transient Object _array[];
    transient float _float = (float)9.87;
    transient double _double = 3.14159;

    EverythingIsTransient () {
    _array = new Object[2];
    _array[0] = "Array Index 0";
    _array[1] = "Array Index 1";
    }

    void printSelf () {
    System.out.println("Everything is transient, values are:");
    System.out.println("Transient Object is " + _object);
    System.out.println("Transient int is " + _int);
    System.out.println("Transient long is " + _long);
    System.out.println("Transient short is " + _short);
    System.out.println("Transient byte is " + _byte);
    System.out.println("Transient boolean is " + _boolean);
    System.out.println("Transient char is " + _char);
    System.out.println("Transient Array is " + _array);
    System.out.println("Transient float is " + _float);
    System.out.println("Transient double is " + _double);
    System.out.println("");
    }
}

class Opaque implements RtCodeable, RtAwakeAfterDecoding
{
    Opaque self;
    int answer;
    boolean skip;
    Thing thing;
    String alwaysNull = null;
    EverythingIsTransient eit = new EverythingIsTransient();

    Opaque (boolean sk, int ans, Thing aThing) {
        System.out.println("Opaque in constructor");
    skip = sk;
    answer = ans;
    self = this;
    thing = aThing;
    }

    public String classNameToEncode (RtEncoder encoder) { 
        return this.getClass().getName(); 
    }

    public void encode (RtEncoder coder) {
    try {
        System.out.println("Opaque object encode called");
        coder.writeUTF(alwaysNull);
        coder.encodeObject(thing);
        coder.encodeObject(this);
        coder.writeBoolean(skip);
        coder.writeInt(answer);
        coder.encodeObject(eit);
        System.out.println("Encoded eit, it is:");
        eit.printSelf();
    } catch (Exception e) {
        System.out.println("Exception occured encoding Opaque");
        e.printStackTrace();
    }
    }

    public Object decode (RtDecoder coder) {
    System.out.println("In decode for Opaque");
    try {
        alwaysNull = coder.readUTF();
        System.out.println("Decoded alwaysNull as " + alwaysNull);
        thing = (Thing) coder.decodeObject();
        self = (Opaque) coder.decodeObject();
        System.out.println("Decoded self as " + self);
        skip = coder.readBoolean();
        if (skip == false) {
        answer = coder.readInt();
        eit = (EverythingIsTransient)coder.decodeObject();
        }
        else {
        answer = 0;
        eit = null;
        }
    } catch (Exception e) {
        System.out.println("Error decoding Opaque");
        e.printStackTrace();
    }
    
    if (skip == true) return this;
    
    try {
        int bogus = coder.readInt();
    } catch (Exception e) {
        System.out.println("Expected exception decoding bogus");
    }
    
    return this;
    }

    public void awakeAfterDecoding () {
    System.out.println("Opaque object woke up, printing thing");
    if (thing != null) thing.printSelf();
    if (eit != null) eit.printSelf();
    }
}

class Thing implements RtAwakeAfterDecoding
{
    Vector vec;
    Double doubles[];
    long longs[];
    String name;
    Object nobody = null;
    Thing otherThing;
    Object somebody;
    transient int ti;
    transient Object tobj;
    Opaque opie;
    String alwaysNull = null;
    
    Thing(String aName) {
    Object obj = new Object();
    longs = new long[2];
    longs[0] = 5555;
    longs[1] = 6666;
    doubles = new Double[3];
    doubles[0] = new Double(123.456);
    doubles[1] = new Double(1.1111);
    doubles[2] = new Double(0.0001);
    vec = new Vector(2);
    vec.addElement(obj);
    vec.addElement(obj);
    name = aName;
    otherThing = null;
    somebody = obj;
    ti = 192837;
    tobj = obj;
    opie = new Opaque(true, 0, this);
    }

    Thing(String aName, boolean doMore) {
    longs = new long[2];
    longs[0] = 1234;
    longs[1] = 9876;
    doubles = new Double[3];
    doubles[0] = new Double(3.14159);
    doubles[1] = new Double(9.9999);
    doubles[2] = new Double(0.0);
    vec = new Vector(1);
    vec.addElement(this);
    name = aName;
    otherThing = this;
    somebody = new Object();
    opie = new Opaque(false, 42, this);
    }

    public void awakeAfterDecoding () {
    System.out.println("I am thing, hear me roar!");
    }

    public void printSelf () {
    System.out.println("Thing, name is " + name);
    System.out.println("Thing, value is " + this);
    System.out.println("My long[0] = " + longs[0]);
    System.out.println("My long[1] = " + longs[1]);
    System.out.println("My Double[0] = " + doubles[0]);
    System.out.println("My Double[1] = " + doubles[1]);
    System.out.println("My Double[2] = " + doubles[2]);
    System.out.println("My vector contains " + vec.size() + " elements");
    System.out.println("My vector element 0 is " + vec.firstElement());
    if (vec.size() > 1)
        System.out.println("My vector element 1 is " + vec.elementAt(1));
    System.out.println("My other thing is " + otherThing);
    System.out.println("Somebody is " + somebody);
    System.out.println("Opaque object is " + opie);
    System.out.println("AlwaysNull is " + alwaysNull);
    System.out.println("");
    }
}

public class HelloGordie implements ELaunchable
{
    public void go (EEnvironment env) {
        System.out.println("Go called on HelloGordieLauncher!");
        HelloGordieReceiver r = new HelloGordieReceiver();
        HelloGordieSender   s = new HelloGordieSender();
        ERegistrar reg = (ERegistrar)env.get("registrar.root");
        try {
            reg.startup(0);
        } catch (ERestrictedException e) {
            System.out.println("net startup: " + e);
            System.exit(0);
        }
        String who = env.getProperty("who");
        if (who == null) {
            r <- receive(env, reg);
        }
        else {
            s <- send(env, who, reg);
        }
    }
}

eclass HelloGordieSender
{
    ERegistrar reg;
    emethod send (EEnvironment env, String who, ERegistrar reg) {
        this.reg = reg;
        HelloGordieReceiver otherGuy;
        Thing thing1 = new Thing("Thing-1", true);
        Thing thing2 = new Thing("Thing-2");
        etry {
            String errorString = ENetUtility.lookupWithName(&otherGuy, env, who);
        } ecatch (Throwable e) {
            System.out.println("E Exception on lookup: " + e);
            System.exit(0);
        }
        otherGuy <- helloGordie(thing1, this);
        etry {
            otherGuy <- helloGordie(thing2, this);
        } ecatch (GordieEException ee) {
            System.out.println("HelloGordie (" + this + ") caught expected exception: " + ee.getMessage());
            try {
                reg.shutdown();
            } catch (Exception e) {
                System.out.println("Exception shutting down comm system: " + e);
                System.exit(0);
            }
        }
    }

    emethod yeah () {
        System.out.println("The receiver yeah'd me back, cool");
    }
}

eclass HelloGordieReceiver
{
    ERegistrar reg;
    boolean sent = false;
    emethod receive (EEnvironment env, ERegistrar reg) {
        this.reg = reg;
        try {
            ENetUtility.registerWithPropertyName(this, env, "EARLFile", "HelloGordie: who=");           
        } catch (Exception e) {
            System.out.println("Exception registering");
            e.printStackTrace();
            System.exit(0);
        }
    }

    emethod helloGordie(Thing thing, HelloGordieSender sender) {
        System.out.println("Hola Mundo from " + thing.name);
        ////thing.printSelf();
        if (sent == false) {
            sent = true;
            sender <- yeah();
        }
        else {
            ethrow new GordieEException("Just testing");
            try {
                reg.shutdown();
            } catch (Exception e) {
                System.out.println("Exception shutting down comm system: " + e);
                System.exit(0);
            }
        }
    }
}


