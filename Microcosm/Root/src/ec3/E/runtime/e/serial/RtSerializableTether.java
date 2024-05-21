/**
 * RtSerializableTether.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Arturo Bejar
 * December 8, 1997
 *
 *
 */


package ec.e.serial;

import java.io.Serializable;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import ec.e.run.RtTether;
import ec.e.run.Exportable;
import ec.e.run.NetIdentityMaker;
import ec.e.run.RtDelegatingEncodeable;


/**
 * Used as part of the internals for RtForwardingSturdyRef when we want
 * to store  a SturdyRef for upgrade purposes. <p>
 * SturdyRefs are generated from Registrar information and the identity
 * numbe from an Exportable object. This is a serializable class
 * that will preserve its SturdyRef when serialized to a
 * StateOutputStream with the local Registrar. <p>
 * When encoded this will encode its target, sending a proxy of the
 * righ type to the client machine.
 * 
 */

final public class RtSerializableTether 
        implements RtTether, Exportable, RtDelegatingEncodeable,
        Serializable, RtStateUpgradeable  {

    static final long serialVersionUID = -5063125664720831407L;

    private long identity = 0L;
    private Object myKey;
    transient private RtTether myTarget;

    public RtSerializableTether() {}

        /**
         * @param key used to set a new target for the tether.
         */
    public RtSerializableTether(Object key) {
       myKey = key;
    }

        /**
         * @param key used to set a new target for the tether.
         * @param target object to deliver to & encode.
         * 
         */

    public RtSerializableTether(EObject target, Object key) {
        myKey = key;

                setTarget(key, target);
    }

        /**
         * @param key the key established in the constructor. 
         * @param target object to deliver to & encode.
         * 
         */

    public void setTarget(Object key, EObject target) {
        if (key != myKey) {
            throw new SecurityException("tried to setTarget with wrong key");
        }
        myTarget = (RtTether)target;
    }

        // Exportable methods

    public final long getIdentity() {
        if (identity == 0L) {
            identity = NetIdentityMaker.nextIdentity();
        }
        return identity;
    }

        //RtTether methods

    /** tether invoke--queues up an E message. */
    public void invoke(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
            myTarget.invoke(sealer, args, ee);
    }

    /** tether invokeNow--calls the synchronous invoke method on the sealer,
     * which should call back in to the impl. */
    public void invokeNow(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
            myTarget.invokeNow(sealer, args, ee);
    }

    /** 
     * @returns target or null if there is no target.
     * 
         */

    public Object delegateToEncode ()  {
        if (null != myTarget) {
            return myTarget;
        }   
        else {
            return null;
        }
    }

    public boolean encodeMeForDeflector()  {
        return true;
    }   
}

