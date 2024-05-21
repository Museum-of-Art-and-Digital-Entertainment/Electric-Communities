package ec.e.quake;

import java.io.IOException;

/** 
 * A Tether is an object which represents in one objectspace an
 * object in another objectspace. For example, a RootHolder is a
 * Tether that represents an in-Vat root to an outer Java
 * environment. <p>
 *
 * Tethers are the Stewards that represent the defining boundary of a
 * Vat.  Stewards can see both Guests and Crew, and can access
 * dangerous Java services.  Therefore, they must preserve delicate
 * correctness properties, so there must be very few of them.  Tethers
 * know the Vat that they collectively define.  They come in two
 * flavors, root-holders and leafs. <p>
 *
 * A Leaf holds onto 'held' without causing it to be e-reachable from
 * the Leaf, since typically 'held' will be an out-of-vat
 * object. Leafs are the Tethers that may directly refer to Crew, as
 * well as to dangerous Java services. <p>
 *
 * These pre-EVM Tethers are loosely based on evm1/ec/util/Tether.java
 * (Thanks Dan!) <p>
 *
 * @see ec.e.quake.Vat#makeSturdyLeaf 
 */
public class Tether {
    private Vat myVat;
    private long myBirthQuake;
    protected transient Object myHeld;

    static public final Class TYPE = new Tether().getClass();

    /**
     * Bogus constructor in order to initialize TYPE
     */
    private Tether() {}
    
    /**
     * Construct the Tether to be on the boundary of 'vat'
     */
    public Tether(Vat vat, Object held) {
        myVat = vat;
        myBirthQuake = myVat.quakeCount();
        myHeld = held;
    }
    
    /**
     * What's the vat of this Tether?
     */
    public Vat vat() {
        return myVat;
    }
    
    /** 
     * This is the one and only lock for synchronizing between the
     * vat and the outside world.  This is only yielded by the ERunQ
     * at bracepoints.
     */
    public Object vatLock() {
        return myVat.vatLock();
    }
    
    /**
     * What was the quakeCount() (the number of quakes that have
     * already occurred) at the time this Tether was born?
     */
    protected long birthQuake() {
        return myBirthQuake;
    }
    
    /**
     * If held() is called on a smashed Tether, it calls
     * reconstructed().  If reconstructed() returns an object, this
     * becomes the newly held object of the newly non-smashed Tether,
     * and the smashed state is not visible to the Tether's user.
     * Such a Tether is "sturdy".  If reconstructed() always returns
     * 'myHeld', then the Tether is unaffected by quakes and so is
     * "simply sturdy".  <p>
     *
     * The default behavior of reconstructed() is to throw a
     * SmashedException, which propogates to held()'s caller.  A
     * Tether inheriting this behavior is "fragile".
     */
    protected Object reconstructed() throws SmashedException {
        myHeld = null; //to free memory
        throw new SmashedException("born after " + myBirthQuake +
                                   ", now is " +
                                   myVat.quakeCount());
    }

    /** 
     * Returns the value held by this Tether.  Yes, this is public
     * and should ONLY BE USED VERY CAREFULLY.  In particular, the
     * value should not be held on the other side of the vat boundary
     * anytime an E bracepoint is possible. <p>
     *
     * If the Tether is a Leaf, this means that the value returned by
     * held() should not be stored, and should evaporate at the end of
     * the method.  If the Tether is a root-holder, then the pattern
     * should be: <p>
     * <pre>
     *         Tether t = //...
     *         synchronized(t.vatLock()) {
     *             ... t.held() ...
     *         }
     * </pre> 
     * where the value from 't.held()' evaporates at the end of the
     * synchronized block. <p>
     *
     * A Tether gets smashed in a quake, but whether this is visible
     * depends on the Tether's reconstructed() method.
     *
     * @see ec.e.quake.Tether#reconstructed
     */
    public Object held() throws SmashedException {
        long quakeCount = myVat.quakeCount();
        if (quakeCount != myBirthQuake) {
            myHeld = reconstructed();
            //never reached if reconstructed() throws
            myBirthQuake = quakeCount;
        }
        return myHeld;
    }

    /**
     * Smashes the Tether and frees 'myHeld' (to enable the
     * PerishableDataHolder's CacheManager to reclaim memory).
     * Tethers intended to be simply sturdy should override this to do
     * nothing, in order to not lose 'myHeld'.
     */
    protected void smash() {
        myHeld = null; //to free memory
        myBirthQuake = -1;
    }
}


/**
 * Some operation could not happen because quake damage was
 * encountered 
 */
public class SmashedException extends IOException {
    public SmashedException() {}
    public SmashedException(String s) { super(s); }
}


/**
 * A Tether that is unaffected by a quake-drill.  Used both for sturdy
 * root holders and for sturdy leafs.
 *
 * @see ec.e.quake.Vat#makeSturdyRoot
 */
public class SimplySturdyTether extends Tether {
    
    /** 
     * Returns a Tether of 'vat' from which 'held' will be accessible,
     * even following a quake-drill.
     */
    public SimplySturdyTether(Vat vat, Object held) {
        super(vat, held);
    }

    /**
     * Overridden to provide the pre-quake held object as the
     * post-quake held object.
     */
    protected Object reconstructed() {
        return myHeld;
    }

    /**
     * Overridden to do nothing
     */
    protected void smash() {}
}


/** 
 * An out-of-vat object that holds an in-vat-Seismologist in a fragile
 * fashion.
 *
 * @see ec.e.quake.Vat#makeFragileRoot
 */
public final class FragileRootHolder extends Tether {
    private Object myKey;
    
    /** 
     * Only to be instantiated by the Vat.
     */
    FragileRootHolder(Vat vat, Object key, Seismologist waiting) {
        super(vat, waiting);
        myKey = key;
    }
    
    /**
     * When the RootHolder is finalized, it unregisters its
     * key-waiting pair
     */
    protected void finalize() {
        synchronized(vatLock()) {
            vat().removeFragileRoot(myKey);
        }
    }
}

