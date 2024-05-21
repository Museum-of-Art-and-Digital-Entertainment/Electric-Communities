package ec.e.run;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.io.IOException;
import ec.e.start.crew.CrewCapabilities;

import ec.e.util.crew.PropUtil;


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
 * Fragile roots are registered with the Vat so long as their
 * FragileRootHolder continues to exist and no quake occurs.  Should
 * the FragileRootHolder get collected, the fragile root is simply
 * unregistered so it may be collected as well.  Should a quake occur,
 * all the fragile roots are notified and unregistered. <p>
 *
 * The vat is callable by multiple threads, both from within and
 * without the vat.  But only very carefully!  (XXX need to be more
 * explicit here.)
 */
public class Vat {
    static private boolean ThereCanBeOnlyOne = false;

    private Object myVatLock;
    private Hashtable myFragileRoots;
    private long myQuakeCount;
    private TimeQuake myLastQuake;
    private Seismologist myWaiter;
    private RtRun myRunner;
    //private EEnvironment myEEnvironment; // killerhack XXX!!!
    public EEnvironment myEEnvironment; // killerhack XXX!!!
    private Vector myNotifies;

    /**
     * Returns a new vat
     */
    public Vat(Properties props) throws OnceOnlyException, IOException {
        if (ThereCanBeOnlyOne) {
            throw new OnceOnlyException("Already initialized");
        }
        ThereCanBeOnlyOne = true;
        myVatLock = new Object();
        myFragileRoots = new Hashtable();
        myQuakeCount = 0;
        myLastQuake = new TimeQuake(0, 0);
        myWaiter = null;
        myEEnvironment = null;
        myNotifies = new Vector();

        myRunner = new RtRun(this, myVatLock);
        String causalityId = (props.getProperty("CausalityId"));
        if (causalityId != null) {
            RtCausality.setCausalityId(causalityId);
        }
        RtCausality.setCausalityTracing
            ("true".equals(props.getProperty("CausalityTracing", "false")));
    }

    /**
     * For sending a diagnostic string to something out-of-vat which
     * might be listening.  In fact, it just println's to stderr.
     */
    static public void println(String str) {
        System.err.println(str);
    }

    /**
     * Exits the process incarnation.  Same as TimeMachine <-
     * crash(exitCode), but useable even when no TimeMachine has been
     * loaded. 
     */
    public void exit(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * Used once in EBoot so they point at each other.
     */
    public void setEEnvironment(EEnvironment eEnv) {
        if (myEEnvironment == null) {
            myEEnvironment = eEnv;
        }
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
     * Used by the TimeMachine to ensure that a commit is confirmed in
     * the revived world. 
     *
     * @param waiter suspect, nullOk;
     */
    public void setWaiter(Seismologist waiter) {
        myWaiter = waiter;
    }

    /**
     * Post-quake initialization.  Report a Reincarnation quake and
     * tell myWaiter, if any, to notice a commit.  This method will
     * restart the ERunQ thread. <p>
     *
     * (If there could be multiple ERunQs, we'd need to be careful
     * that the deliveries scheduled by this method are in the ERunQ
     * of the revived vat.) <p>
     *
     * @param args revival command line args (after properties are
     * stripped off)
     * @param props properties specified by the revival command line
     * @param checkpoint the new value for the special magic power
     * named "CheckpointTether", and used by the TimeMachine.  XXX
     * This is typed as 'Object' to avoid having this module depend on     
     * ec.e.quake. 
     */
    public void revive(String[] args, 
                       Properties props,
                       Object checkpoint)
         throws OnceOnlyException {

        CrewCapabilities.establishCrewCapabilities(myEEnvironment);

        /*
         * This computation is "in-vat", and so executes with the
         * vatLock held.  Since it causes new enqueue'ings, it could set
         * the ERunQ thread going early, otherwise. <p>
         */

        synchronized(myVatLock) {
            RtRun.setStatics(myRunner, myVatLock);
            myEEnvironment.revive(args, props, checkpoint);

            report(TimeQuake.REINCARNATION);
            if (myWaiter != null) {
                myWaiter <- noticeCommit();
                myWaiter = null;
            }
        }

        // RobJ added this as it didn't appear this would ever happen on revival
        // otherwise!
        String causalityId = (props.getProperty("CausalityId"));
        if (causalityId != null) {
            RtCausality.setCausalityId(causalityId);
        }
        RtCausality.setCausalityTracing
            ("true".equals(props.getProperty("CausalityTracing", "false")));

    }
    

    /* ********************** fragile roots section ********************/
    
    /**
     * Registers the in-vat seismologist, and returns the out-of-vat
     * Tether that retains 'held'.  This is typically used for the
     * return path of an asynchronous call from in-vat to out-of-vat.
     * The out-of-vat activity would reply to the 'held' when
     * complete (after narrowing it to something else, of course), but
     * in the meantime, the Seismologist would still be considered
     * reachable for persistence.  Should a quake occur before
     * the operation completes, the Seismologist (if non-null) is
     * notified instead.  Were we also doing reachability for egc
     * (which the prevat does not do, but the EVat will), then both
     * 'held' and 'waiting' would be reachable for egc purposes.
     *
     * @param held suspect nullOk
     * @param waiting suspect nullOk
     */
    public FragileRootHolder makeFragileRoot(Object held, 
                                             Seismologist waiting) {
        /*
         * In this prevat implementation, 'held' is not registered as
         * a root, since the prevat only uses reachability for
         * persistence, not egc.  'waiting', if it isn't null, is
         * registered since it needs to be informed following a quake.
         */
        if (waiting != null) {
            Object key = new Object();
            synchronized(myVatLock) {
                myFragileRoots.put(key, waiting);
            }
            return new FragileRootHolder(this, held, key);
        }
        return new FragileRootHolder(this, held, null);
    }

    /**
     * Equivalent to 'makeFragileRoot(waiting, waiting)', for when the
     * object held by the Tether is the same as the Seismologist
     * wanting to know that a quake has happened.
     *
     * @param waiting suspect nullOk
     */
    public FragileRootHolder makeFragileRoot(Seismologist waiting) {
        return makeFragileRoot(waiting, waiting);
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
     * Calls waitForNext on the lastQuake object 
     */
    // XXX (GJF) ecomp with safejava bizarre bug
    // forces this to be typed to Object as it gets
    // confused by typing waiter to Seismologist.
    public void waitForNext(Object watcher) {
        myLastQuake.waitForNext((Seismologist)watcher);
    }
   
    /**
     * Registers an interest in syncronously finding out about
     * a quake. Register n times, get told n times.
     */
    public void setSyncQuakeNoticer (Syncologist sync) {
        myNotifies.addElement(sync);
    }

    /**
     * Tells the vat that a quake happened, and what kind of damage
     * occurred.  The vat updates its lastQuake, and causes all
     * Seismologists waiting for this quake to notice it, including
     * all fragile roots.
     */
    public void report(int damage) {
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

            Vector notifies = myNotifies;
            myNotifies = new Vector();
            for (Enumeration e = notifies.elements();
                 e.hasMoreElements(); ) {
                Syncologist s = (Syncologist)e.nextElement();
                s.noticeQuakeSync(myLastQuake);
            }
        }
    }
}


