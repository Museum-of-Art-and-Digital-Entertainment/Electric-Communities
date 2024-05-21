package ec.e.quake;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import ec.e.cap.EEnvironment;
import ec.e.openers.*;


/**
 * The holder for an ERunQ and a set of guest & steward objects, all
 * of which get saved and restored as a unit.  The vat itself is the
 * root object for externalizing/internalizing from a checkpoint file.
 * The objects "in" the vat are those that are e-reachable starting
 * from roots & terminating at leafs.  <p>
 * 
 * Unlike Leafs, roots are not Tethers.  Rather, roots are the objects
 * designated by root holders, which are Tethers.  Fragile roots are
 * objects designated by FragileRootHolders, and must also be
 * Seismologists. <p>
 *
 * various leafs and roots are registered by name in the EEnvironment.
 * There should be a small number of such statically known names, as
 * these are the basis for hooking things back up following a
 * quake. <p>
 *
 * Fragile roots are registered with the Vat so long as their
 * FragileRootHolder continues to exist and no quake occurs.  Should
 * the FragileRootHolder get collected, the fragile root is simply
 * unregistered so it may be collected as well.  Should a quake occur,
 * all the fragile roots are notified and unregistered. <p>
 *
 * The vat is callable by multiple threads, both from within and
 * without the vat.  But only very carefully!  (XXX need to be more
 * explicit here.)  The vat registers itself as a sturdy root in its
 * own EEnvironment under the name "vat". <p>
 *
 * XXX-Note that this vat conflates the namespaces of in-vat
 * environment with inter-vat designation. In the EVM world, a
 * vat client introduces named Leaves and can choose to name
 * Roots at will. These names are invisible to guests. If needed,
 * but not currently supplied in the EVM, one can feel free to
 * create a generic name->object EEnvironment-like object, but this
 * would be a separate thing.
 */
public class Vat {
    private Object myVatLock;
    private EEnvironment myEEnv;
    private Hashtable myFragileRoots;
    private long myQuakeCount;
    private TimeQuake myLastQuake;
    Seismologist myWaiter;
    
    /**
     * Returns a new vat that starts out containing only itself
     * (registered as a sturdy root under the name "vat").
     */
    public Vat() {
        myVatLock = new Object();
        myEEnv = new EEnvironment();
        myFragileRoots = new Hashtable();
        myQuakeCount = 0;
        myLastQuake = new TimeQuake(0, 0);
        myWaiter = null;
        makeSturdyRoot("vat", this);
    }


    public void init(String cpName) throws OnceOnlyException, IOException {
        if (myEEnv.containsKey("stableStore")) {
            throw new OnceOnlyException("Already initialized");
        }

        StableStore stable = null;
        if (cpName != null) {
            stable = new StableStore(cpName);
        }

        Tether store = new SettableTether(this, stable);
        myEEnv.put("stableStore", store);
        
        makeSturdyRoot("timeMachine", new TimeMachine(this, store));
    }
    /** 
     * To restart a Vat, do "java Vat checkpoint-filename", where
     * checkpoint-filename is the same name that was provided in a
     * "checkpoint=name" argument to an earlier EBoot command.
     */
    static public void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new Error("usage: java Vat checkpoint-filename");
        }
        reviveFrom(new StableStore(args[0]));
    }
    
    /**
     * Returns the Vat gotten by internalizing the checkpoint,
     * but only after reporting a Reincarnation quake and telling 
     * myWaiter, if any, to notice a commit.  This method will restart
     * the ERunQ thread. <p>
     *
     * XXX If there could be multiple ERunQs, we'd need to be careful
     * that the deliveries scheduled by this method are in the ERunQ
     * of the revived vat.
     */
    static public Vat reviveFrom(StableStore stable) 
         throws IOException {

        Vat vat = (Vat)stable.restore();

        /*
         * This computation is "in-vat", and so executes with the
         * vatLock held.  Since it causes new enqueue'ings, it could set
         * the ERunQ thread going early, otherwise. <p>
         */
        synchronized(vat.vatLock()) {
            try {
                RtRun.setStatics((RtRun)vat.eEnv().get("eRunner"),
                                 vat.vatLock());
            } catch (OnceOnlyException e) {
                throw new Error("RtRun already initialized: " + e);
            }

            //Set the TimeMachine's StableStore Tether to hold the
            //StableStore we just restored from.
            SettableTether store 
                = (SettableTether)vat.eEnv().get("stableStore");
            store.set(stable);

            vat.report(TimeQuake.REINCARNATION);
            if (vat.myWaiter != null) {
                vat.myWaiter <- noticeCommit();
                vat.myWaiter = null;
            }
        }
        RtRun.theOne().beRunning(); //just in case nothing was newly enqueued 
        return vat;
    }
    
    /** 
     * This is the one and only lock for synchronizing between the
     * vat and the outside world.  This is only yielded by the ERunQ
     * at bracepoints.
     */
    public Object vatLock() {
        return myVatLock;
    }
    
    /**
     * Deprecated.
     * Returns an in-vat Tether for holding 'outOfVat' until the next
     * quake. 
     */
    public Tether makeFragileLeaf(Object outOfVat) {
        return new Tether(this, outOfVat);
    }
    
    
    /* *************************** eEnv section ********************/
    
    /**
     * The vat's EEnvironment
     */
    public EEnvironment eEnv() {
        return myEEnv;
    }
    
    /** 
     * Registers in-vat object 'root' under 'name' in the
     * EEnvironment, and returns an out-of-vat Tether for accessing
     * it.
     */
    public Tether makeSturdyRoot(String name, Object root) {
        synchronized(myVatLock) {
            if (myEEnv.containsKey(name)) {
                throw new IllegalArgumentException
                ("name " + name + " already registered");
            }
            myEEnv.put(name, root);
        }
        return new SimplySturdyTether(this, root);
    }
    
    /**
     * Removed whatever is registered in the EEnvironment under
     * 'name'. 
     */
    public void removeFromEEnv(String name) {
        synchronized(myVatLock) {
            myEEnv.remove(name);
        }
    }
    
    /**
     * Get an out-of-vat object that corresponds to the in-vat object
     * registered in the environment under 'name'.
     */
    public Object outOfVat(String name) throws SmashedException {
        //XXX do we actually not need to synchronize?
        Object inVat = myEEnv.get(name);
        if (inVat == null) {
            throw new IllegalArgumentException(name + " not found");
        }
        if (inVat instanceof Tether) {
            return ((Tether)inVat).held();
        } else {
            return new SimplySturdyTether(this, inVat);
        }
    }
    
    /**
     * Register under 'name' & return an in-vat leaf for accessing the
     * out-of-vat object.
     */
    public Tether makeSturdyLeaf(String name, Object outOfVat) {
        Tether result;
        synchronized(myVatLock) {
            if (myEEnv.containsKey(name)) {
                throw new IllegalArgumentException
                    ("name " + name + " already registered");
            }
            result = new SimplySturdyTether(this, outOfVat);
            myEEnv.put(name, result);
        }
        return result;
    }
    
    
    
    /* ********************** fragile roots section ********************/
    
    /**
     * Registers the in-vat seismologist, and returns the out-of-vat
     * Tether that retains it.  This is typically used for the return
     * path of an asynchronous call from in-vat to out-of-vat.  The
     * out-of-vat activity would reply to the Seismologist when
     * complete (after narrowing it to something else, of course), but
     * in the meantime, the Seismologist would still be considered
     * reachable for egc & persistence.  Should a quake occur before
     * the operation completes, the Seismologist is notified
     * instead. 
     */
    public FragileRootHolder makeFragileRoot(Seismologist waiting) {
        Object key = new Object();
        synchronized(myVatLock) {
            myFragileRoots.put(key, waiting);
        }
        return new FragileRootHolder(this, key, waiting);
    }
    
    /**
     * Called only by FragileRootHolder.finalize().
     */
    void removeFragileRoot(Object key) {
        synchronized(myVatLock) {
            myFragileRoots.remove(key);
        }
    }
    
    
    /* ******************** quaking section ********************/
    
    /**
     * How many quakes has this vat experienced?
     */
    public long quakeCount() {
        return myQuakeCount;
    }
    
    /**
     * A description of the last quake experienced by this vat.
     */
    public TimeQuake lastQuake() {
        return myLastQuake;
    }
    
    /**
     * Tells the vat that a quake happened, and what kind of damage
     * occurred.  The vat updates its lastQuake, and causes all
     * Seismologists waiting for this quake to notice it, including
     * all fragile roots.
     */
    void report(int damage) {
        synchronized(myVatLock) {
            myQuakeCount++; 
            TimeQuake prev = myLastQuake;
            myLastQuake = new TimeQuake(damage, myQuakeCount);
            prev.setNextQuake(myLastQuake);
            for (Enumeration e = myFragileRoots.elements();
                 e.hasMoreElements(); ) {
                
                Seismologist s = (Seismologist)e.nextElement();
                s <- noticeQuake(myLastQuake);
            }
            myFragileRoots = new Hashtable();
        }
    }
}


class SettableTether extends SimplySturdyTether {
    
    SettableTether(Vat vat, Object held) {
        super(vat, held);
    }

    void set(Object newValue) {
        myHeld = newValue;
    }
}

