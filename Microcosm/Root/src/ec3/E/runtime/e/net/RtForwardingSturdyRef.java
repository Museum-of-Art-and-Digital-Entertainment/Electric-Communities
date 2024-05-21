/**
 * RtForwardingSturdyRef.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Arturo Bejar
 * December 8, 1997
 *
 *
 */

package ec.e.net;

import java.io.Serializable;

import ec.e.run.RtTether;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRef;
import ec.e.net.Registrar;
import ec.e.start.EEnvironment;
import ec.e.serialstate.RtSerializableTether;


/**
* Used as the internal key in RtForwardingSturdyRef.java
*/
public class SerializableObject extends Object 
        implements Serializable, RtStateUpgradeable{
  static final long serialVersionUID = 6668438671621018682L;        

    public SerializableObject() {
    }
}

/**
 * Used when you want to make a SturdyRef for an object that will be
 * the same across sessions. This is the object that you put in your
 * state bundles, upon decode you n <p> There needs to be an instance
 * of the
 * current Registrar in the same
 * StateOutputStream since SturdyRefs are created from Registrar info +
 * Exportable object identity information.
 * 
 */

public class RtForwardingSturdyRef 
        implements Serializable, RtStateUpgradeable {

  static final long serialVersionUID = 6668438671621018682L;        

    private SerializableObject myKey;
    private RtSerializableTether myTether;

    // This instance variable forces the current registrar to
    // put in this output stream, if you then attempt to read in
    // this object in a system that already has a running registrar
    // it should cause an exception, which is what we want
    // because with tne new registrar, the SturdyRef would be different.
    private Registrar myRegistrar;

    /** 
     * This is the SturdyRef created when this object is initially instantiated
     */

    private SturdyRef myInitialSturdyRef;

    /** 
     * This is the SturdyRef recreated after deserialization & setting
     * the target, it has be equal to myInitialSturdyRef.
     */
    transient private SturdyRef mySturdyRef;


    /**
     *@param registrar you need to provide a valid Registrar.
     */


     // Made constructor protected for better security properties, yay!

    protected RtForwardingSturdyRef(Registrar registrar, EObject target) {
        myRegistrar = registrar;

        myKey = new SerializableObject();
        myTether = new RtSerializableTether(target, myKey);

        // Initial publication of the object.
        myInitialSturdyRef = myRegistrar.getSturdyRefMaker().makeSturdyRef(myTether);

        mySturdyRef = myInitialSturdyRef;
    }

    /**
   * Sets the object that you want represented by this SturdyRef, note that
     * this is only done once per session and the SturdyRef is not actually
     * registered until you setTarget (avoiding null lookups).
     * @param target object that will be looked up under the known SturdyRef.
     * @return the SturdyRef that will be the same across sesssions.
     *
     */

    public SturdyRef setTarget(EObject target) 
            throws Exception {

        if (mySturdyRef != null) {

            throw new Exception("This ForwardingSturdyRef has already been assigned a target.");

        } else {// This is a recently deserialized ForwardingSturdyRef
            
            myTether.setTarget(myKey, target);
        }

        mySturdyRef = myRegistrar.getSturdyRefMaker().makeSturdyRef(myTether);

        // Compare with initial sturdyRef to see if everything got preserved ok.
        if (!mySturdyRef.equals(myInitialSturdyRef)) {
            throw new Exception("SturdyRefs don't match, Bad! Bad!");
        }

        return mySturdyRef;
    }

    /**
     * @return - the current target.
     */

    public RtTether getTarget() {
        return myTether;
    }

    /**
     * @return the SturdyRef that will be the same across sesssions, or null if
     * no target has been set yet.
     */

    public SturdyRef getSturdyRef() {
        return mySturdyRef;
    }
}

