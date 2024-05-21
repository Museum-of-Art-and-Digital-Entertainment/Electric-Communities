/* 
  HelloUnique.e -- Version 0.1 -- Simple comm test

  Gordie Freedman 
  Electric Communities
  19-February-1996

  Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.comm;

import ec.e.comm.ELaunchable;

import ec.e.comm.*;
import ec.e.stream.*;
import java.util.*;
import ec.e.db.*;

public class HelloUnique
{
    public static void main(String args[]) {
        RtLauncher.launch(new HelloUniqueLauncher(), args);
    }
}

class UniqueEException extends RuntimeException {
    public UniqueEException(String message) {
        super(message);
    }
}

class Opaque implements RtCodeable, RtUniquelyCodeable
{
    Opaque self;
    int answer;
    Thing thing;

    Opaque (int ans, Thing aThing) {
        answer = ans;
        self = this;
        thing = aThing;
    }

    public void printSelf () {
        System.out.println(this + " answer is " + answer + " thing is " + thing + " self is " + self);
    }

    public String classNameToEncode (RtEncoder encoder) {
        return this.getClass().getName();
    }

    public void encode (RtEncoder coder) {
        try {
            coder.encodeObject(thing);
            coder.encodeObject(self);
            coder.writeInt(answer);
        } catch (Exception e) {
            System.out.println("Exception occured encoding Opaque");
            e.printStackTrace();
        }
    }

    public Object decode (RtDecoder coder) {
        System.out.println("In decodeObject for Opaque");
        try {
            thing = (Thing) coder.decodeObject();
            self = (Opaque) coder.decodeObject();
            System.out.println("Decoded self as " + self);
            answer = coder.readInt();
        } catch (Exception e) {
            System.out.println("Error decoding Opaque");
            e.printStackTrace();
        }
        return this;
    }
}

class Thing 
{
    Object somebody;
    String name;

    Thing (String aName) {
        Object obj = new Object();
        somebody = obj;
        name = aName;
    }

    public void printSelf () {
        System.out.println("Thing, name is " + name);
    }
}

eclass HelloUniqueLauncher implements ELaunchable
{
    emethod go (RtEEnvironment env) {
        RtNetworkController con;
        System.out.println("Go called on HelloUniqueLauncher!");
        con = env.startNetworkEnvironment();
        HelloUniqueReceiver r = new HelloUniqueReceiver();
        HelloUniqueSender   s = new HelloUniqueSender();
        r <- receive(env);
        s <- send(env, con);
    }
}

eclass HelloUniqueSender 
{
    RtNetworkController networkController;
    emethod send (RtEEnvironment env, RtNetworkController con) {
        HelloUniqueReceiver otherGuy;
        networkController = con;
        Thing thing = new Thing("Thing");
        Opaque opie = new Opaque(1234, thing);

        etry {
            env.getConnector().lookup("localhost", "Doohickey", &otherGuy);
        } ecatch (RtDirectoryEException e) {
            System.out.println("HelloUniqueGordie caught exception on lookup: " +
                               e.getMessage());
        }

        otherGuy <- helloUnique(opie, this);
        otherGuy <- helloUnique(opie, this);
    }

    emethod goodbye () {
        networkController.stopNetworkEnvironment();
    }
}

eclass HelloUniqueReceiver 
{
    boolean sent = false;
    emethod receive (RtEEnvironment env) {
        env.getRegistrar().register("Doohickey", (EObject)this);
    }

    emethod helloUnique (Opaque opie, HelloUniqueSender sender) {
        System.out.println("Here is received Opaque object: ");
        opie.printSelf();
        if (sent == false) {
            sent = true;
        }
        else {
            sender <- goodbye();
        }
    }
}


