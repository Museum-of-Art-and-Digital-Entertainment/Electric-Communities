/**
 * StateStreams.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Arturo Bejar
 * December 7 1997
 *
 */

package ec.e.serialstate;

import ec.trace.Trace;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import ec.e.run.RtDeflector;
import ec.e.start.EEnvironment;
import ec.e.run.RtDelegatingSerializable;

import ec.e.file.EStdio;


/**
 * StateOutputStream extends ObjectOutputStream and implements a
 * serialization policy for some of the primitives in the E
 * runtime. A StateOutputStream will encode Proxies as null and
 * will get the object to serialize for objects that implement 
 * RtDelegateToSerialize. <p>
 * For now this policy is enforced by the policyWriteObject
 * method. Once we move to a version of Java greater than 1.1.2
 * the policy will be implemented by the replaceObject mechanism.
 * 
 * @see ec.e.run.RtDelegateToSerialize
 * @see ec.e.serialstate.StateInputStream
 * @see java.io.ObjectOutputStream
 */

public final class StateOutputStream
        extends java.io.ObjectOutputStream {

    static Class PROXY_CLASS = null;
    static  {
        try {
            PROXY_CLASS = Class.forName("ec.e.net.Proxy");
        } catch (Exception ex) {
        }
    }

    /** Variables for decoding/validating priorities */

    /** Used for SoulState unum instation */
    static public final int UNUM_VALIDATE = 10;
    /** Used for facet instantiation that happens after unums */
    static public final int FACET_VALIDATE = 5;
    // Variable used for notifying our StateSerializer when we write
    // an object
    StateObjectWriteInterest myInterest;


    public StateOutputStream(OutputStream out) throws IOException {
        super(out);
        this.enableReplaceObject(false);
        myInterest = null;
    }

    /**
     * Enforces the following encoding policy for EObjects: <p>
     * - Proxies get encoded as null. <p>
     * - RtDelegatingSerializable encode their targets. <p>
     * - Tries to encode otherwise, if the class is not encodable it throws.
     * @param object to write.
     * @throws IOException
     * 
     */
    
    protected Object replaceObject(Object obj) throws IOException {
//      Trace.serialstate.debugm();
//      Trace.serialstate.debugm("[StateOutputStream]Calling replaceObject: " + obj);

        if (obj instanceof RtDelegatingSerializable) {
            Trace.serialstate.debugm(" RtDelegatingSerializable: ");
            return ((RtDelegatingSerializable)obj).delegateToSerialize();
        } else if (PROXY_CLASS.isInstance(obj)) {
            Trace.serialstate.debugm(" EProxy: null");
            // Proxies are encoded as null.
            return null;
        } else {
            Trace.serialstate.debugm("  Just trying to encode...");
            // Otherwise just continue encoding.
            return (Serializable)obj;       
        }
    }

    public void policyWriteObject(Object obj) throws IOException {
        // Notify our serializer, if it's been set
        if (myInterest != null) myInterest.objectToBeWritten(this, obj);                 
        if (obj instanceof RtDelegatingSerializable) {
            // Get its delegate and write it out
            policyWriteObject(((RtDelegatingSerializable)obj).delegateToSerialize());
        } else if (PROXY_CLASS.isInstance(obj)) {
            // Proxies are encoded as null.
            super.writeObject(null);
        } else {
            // Otherwise just continue encoding.
            super.writeObject(obj);;        
        }
    }

    /**
     * Set the object where we report to when an object is read to the
     * stream
     *
     * @param interest the object that is interested
     */
    public void setMyInterest(StateObjectWriteInterest interest) {
      myInterest = interest;
    }
}

/**
 * Used for reading in a stream generated by a StateOutput stream, this
 * class is also reponsible for delivering the enviroment to certain
 * classes of objects as they are decoded.
 * 
 */

public final class StateInputStream
    extends java.io.ObjectInputStream {

    static Class REGISTRAR_CLASS = null;
    static Class STURDYREF_CLASS = null;

    static  {
        try {
            REGISTRAR_CLASS = Class.forName("ec.e.net.Registrar");

            STURDYREF_CLASS = Class.forName("ec.e.net.SturdyRef");
        } catch (Exception ex) {
        }
    }

    private EEnvironment myEEnvironment;
    private StateObjectReadInterest myInterest;

    public StateInputStream(EEnvironment env, InputStream in) throws IOException {
        super(in);      
        myEEnvironment = env;
        // We use resolve object to handle existing, valid registrars.
        this.enableResolveObject(true);
        myInterest = null;
    }
    protected Object resolveObject(Object obj) throws IOException {
       // Notify our serializer, if it's been set
       if (myInterest != null) myInterest.objectToBeRead(this, obj);
       if ((REGISTRAR_CLASS != null)&&(REGISTRAR_CLASS.isInstance(obj))) {
           Trace.serialstate.debugm("[StateInputStream]:resolveObject:Registrar");
           Object registrar = null;
           try {
               registrar = myEEnvironment.magicPower("ec.e.net.RegistrarMaker");               
           } catch (Exception ex) {
               throw new IOException("Problem establishing registrar:"+ex);
           }
           Trace.serialstate.debugm("                   Registrar summond equals decoded:"+(registrar == obj));
           return registrar;
           
       }
       return obj;
            
    }
    /**
     * Returns environment if you are one of the designated classes that 
     * can get it, note that when you add a class to this list make sure that
     * it is final so that someone can't just extend it, make one, and get the
     * environment.
     * @returns EEnvironment if a valid class asks, null otherwise.
     */

    public EEnvironment getEEnvironment(Object obj) throws IOException {
        
        if (null == obj) return null;

        //XXXABS 980126 Needs to be reviewed.

        // Changed hardwired references to net to strings to
        // avoid difficult circular dependency.     
        if ((REGISTRAR_CLASS != null)&&(REGISTRAR_CLASS.isInstance(obj))) {
//      if (obj instanceof ec.e.net.Registrar) {
            return myEEnvironment;          
//      } else if (obj instanceof ec.e.net.SturdyRef) {
        } else if ((STURDYREF_CLASS != null)&&(STURDYREF_CLASS.isInstance(obj))) {
            return myEEnvironment;
        }
        throw new IOException("Invalid class asking for environment: " + obj);;
    }       
    /**
     * Set the object where we report to when an object is read from the
     * stream
     * 
     * @param interest the object that is interested
     */
    public void setMyInterest(StateObjectReadInterest interest) {
      myInterest = interest;
    }        
}

