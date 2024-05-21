/* 
  HelloCoding.e -- Version 0.1 -- Simple comm test

  Gordie Freedman 
  Electric Communities
  19-February-1996

  Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.comm;

// import ec.e.start.*;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;

// import ec.e.cap.*;
// import ec.e.net.*;
import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefImporter;
import ec.e.net.SturdyRefExporter;

// import ec.e.stream.*;
// import java.util.*;
// import ec.e.db.*;

import ec.e.file.EStdio;
import ec.e.lang.EString;
import java.io.IOException;


public eclass HelloCoding implements ELaunchable
{
    emethod go (EEnvironment env) {
        Registrar registrar  = Registrar.summon(env);

        try {
            EStdio.initialize(env.vat());
        } catch (IOException e) {
            throw new Error("fatal EStdio initialization problem " + e);
        }
        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }
        
        String url=env.getProperty("url");
        if (url == null) {
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefExporter refExporter = registrar.getSturdyRefExporter();
            HelloCodingReceiver receiver = new HelloCodingReceiver(registrar, refMaker, refExporter);
        } else {
            SturdyRefImporter importer = registrar.getSturdyRefImporter();
            SturdyRef ref;
            importer <- importRef(new EString(url), &ref);
            HelloCodingSender   s = new HelloCodingSender();
            s <- send(ref);

        }
    }
}

abstract class Thing
{
    abstract public void printSelf ();
}

class EncodingThing extends Thing implements RtEncodeable
{
    Object self;
    int answer;
    Thang thang;

    EncodingThing (int ans, Thang aThang) {
        answer = ans;
        self = this;
        thang = aThang;
    }

    public void printSelf () {
        EStdio.out().println(this + " answer is " + answer + " thang is " + thang + " self is " + self);
    }

    public String classNameToEncode (RtEncoder encoder) {
        return DecodingThing.sacrificalDecodingThing.getClass().getName();
    }

    public void encode (RtEncoder coder) {
        try {
            coder.encodeObject(thang);
            coder.encodeObject(self);
            coder.writeInt(answer);
        } catch (Exception e) {
            EStdio.err().println("Exception occured encoding");
            EStdio.reportException(e);
        }
    }
}

class DecodingThing extends Thing implements RtDecodeable
{
    Object self;
    int answer;
    Thang thang;

    static DecodingThing sacrificalDecodingThing = new DecodingThing();

    private DecodingThing () {
    }

    public void printSelf () {
        EStdio.out().println(this + " answer is " + answer + " thang is " + thang + " self is " + self);
    }

    public Object decode (RtDecoder coder) {
        EStdio.out().println("In decodeObject");
        try {
            thang = (Thang) coder.decodeObject();
            self = coder.decodeObject();
            EStdio.out().println("Decoded self as " + self);
            answer = coder.readInt();
        } catch (Exception e) {
            EStdio.err().println("Error decoding");
            EStdio.reportException(e);
        }
        return this;
    }
}

class Thang 
{
    Object somebody;
    String name;

    Thang (String aName) {
        Object obj = new Object();
        somebody = obj;
        name = aName;
    }

    public void printSelf () {
        EStdio.out().println("Thang, name is " + name);
    }
}

eclass HelloCodingSender 
{
    emethod send (SturdyRef ref) {
        HelloCodingReceiver otherGuy;
        Thang thang = new Thang("Thang");
        EncodingThing opie = new EncodingThing(1234, thang);

        ref <- followRef(&otherGuy);
        otherGuy <- helloCoding(opie, this);
        otherGuy <- helloCoding(opie, this);
    }
}

eclass HelloCodingReceiver 
{
    Registrar myRegistrar;
    boolean sent = false;
    
    HelloCodingReceiver(Registrar registrar, SturdyRefMaker refMaker, SturdyRefExporter refExporter) {
        try {
            myRegistrar = registrar;
            SturdyRef ref;
            refMaker <- makeSturdyRef(this, &ref);
            EString Eurl;
            refExporter <- exportRef(ref, &Eurl);
            ewhen Eurl (String url) {
                EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.tests.comm.HelloCoding url=" + url);
            }
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod helloCoding (Thing opie, HelloCodingSender sender) {
        EStdio.out().println("Here is received Coded object: ");
        opie.printSelf();
        if (sent == false) {
            sent = true;
        }
        else {
            try {
                myRegistrar.offTheAir();
            } catch (RegistrarException e) {
                EStdio.out().println("reg.shutdown: " + e);
            }
        }
    }
}


