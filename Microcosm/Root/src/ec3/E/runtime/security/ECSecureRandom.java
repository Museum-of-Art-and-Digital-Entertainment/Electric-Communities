package ec.security;

import ec.util.EThreadGroup;
import ec.util.NestedException;

import java.util.Vector;
import java.security.SecureRandom;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;
import ec.e.start.Seismologist;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import ec.e.timer.ETickHandling;
import ec.e.timer.Clock;
import ec.e.timer.ClockController;
import ec.security.crew.SecureRandomCrew;

/*
  The way ECSecureRandom.getCrew() is written, it looks like we don't
  need a Seismologist, but in fact we do.  We want the in-vat
  ECSecureRandom linked up to the SecureRandomCrew soon after
  restarting so collected entropy can be stored in the vat.  If noone
  needs a random number for a while, we can loose some of that
  precious entropy if we don't hook things up right away.  Hence the
  Seismologist.
*/
public eclass ECSecureRandomHolder implements Seismologist, ETickHandling
{
    static private final Trace tr = new Trace("ec.security.ECSecureRandomHolder");
    private ECSecureRandom myGenerator;
    private Clock myClock;

    ECSecureRandomHolder(ECSecureRandom generator) {
        myGenerator = generator;
        // Set up to save entropy in the checkpoint every 5 minutes
        myClock = ClockController.TheQuakeProofClockController().newClock(
                    60*1000, this, null);
        myClock.start();
    }
    
    emethod noticeCommit() {}

    emethod noticeQuake(TimeQuake quake) {
        myGenerator.createCrew();
        myClock.start();
    }

    emethod handleTick(int tick, Clock clock, Object arg) {
        byte[] entropy = new byte[160];
        myGenerator.nextBytes(entropy);
        myGenerator.holdEntropy(entropy, 160);
    }
}
        

/**
 * SecureRandom is the base class for the Vat Secure Random class.
 * SecureRandom handles rebuilding the crew SecureRandom class (held on to
 * by a Tether) upon revival from a quake. <p>
 *
 * Note that the actual SecureRandom Objects always survive a quake (so
 * references to them are valid after revival). <p>
 *
 * @see ec.e.util.crew.SecureRandomCrew
 */
public class ECSecureRandom extends SecureRandom implements EntropyHolder
{
    private static final Trace tr = new Trace("ec.security.ECSecureRandom");
    // Dummy value to keep superclass constructor from taking forever
    private static byte[] dummySeed = new byte[1];

    private ECSecureRandomHolder myHolder;
    private EEnvironment myEnv; // to rebuild tethers
    private Tether myCrew;
    private byte[] myHeldEntropyBits = null;
    private int myHeldEntropyEstimate = 0;
    
    /**
     * Asks the EEnviroment to summon an ECSecureRandomMaker, and returns
     * the ECSecureRandom it conjures up.
     */
    static public ECSecureRandom summon(EEnvironment env)
    {
        try {
            return (ECSecureRandom)env.magicPower("ec.security.ECSecureRandomMaker");
        }
        catch (Exception e) {
            throw new NestedException("problem summoning an ECSecureRandom", e);
        }
    }

    private ECSecureRandom() {}
    
    /**
     * Constuctor
     */ 
    /* package */ ECSecureRandom (EEnvironment env) {
        super(dummySeed);
        myEnv = env;
        myHolder = new ECSecureRandomHolder(this);
        createCrew();
    }
    
    void createCrew() {
        //Note that SecureRandomCrew should be able to get one bit of
        //entropy from the microsecond timer is includes in its seeding
        //operations.
        FragileRootHolder me = myEnv.vat().makeFragileRoot((Object)this, (Seismologist)myHolder);
        myCrew = new Tether (myEnv.vat(), 
                             SecureRandomCrew.getTheSecureRandomCrew(
                                            me, 
                                            myHeldEntropyBits,
                                            myHeldEntropyEstimate));
        myHeldEntropyBits = null;   // Don't reuse this entropy
    }
    
    /**
     * Gets SecureRandomCrew from Tether, throws an exception if it
     * is smashed, as the synchronous quake sync method should
     * reestablish it before this routine is ever called.
     */ 
    private SecureRandomCrew getCrew() {
        SmashedException problem = null;
        
        int i = 0;
        while (i < 2) {
            try {
                return (SecureRandomCrew)myCrew.held();
            } catch (SmashedException e) {
                problem = e;
                createCrew();
            }
            i++;
        }
        throw new NestedException("couldn't create SecureRandomCrew", problem);
    }      

    /**
     * This method is included for compatibility with the super class.
     * As a general statement, the setSeed(seed, entropy) method 
     * should be used in its place.  This method assumes 1 bit of entropy
     * for each byte of the seed.  It is almost certain that this estimate will
     * be wrong.
     * 
     * @param seed the seed.
     */

    public synchronized void setSeed(byte seed[]) {
        setSeed(seed, seed.length);     //Assume 1 bit of entropy/byte
    }

    /**
     * This method is the preferred way to provide entropy for later use.  In
     * practical systems, any source of entropy available can be mixed in using
     * this method.  Examples include: User interface events, disk I/O timings,
     * and random data from quantum-mechanically based hardware.
     * 
     * @param seed the seed.
     * @param entropy an estimate of the amount of entropy in seed in bits.
     *
     * @exception IllegalArgumentException entropy estimate is less than 1 bit.
     * @exception IllegalArgumentException entropy estimate is greater than 8
     *            bits for every byte of the seed.
     */

    public synchronized void setSeed(byte seed[], int entropy) {
        //Super. stinks.  Since the superclass constructor calls setSeed, we
        //must be prepared to handle receiving entropy before the crew
        //SecureRandomCrew has been created.  We will detect this situation
        //by noting that the SecureRandomCrew is null and quickly bail.
        if (myCrew != null) {
            getCrew().setSeed(seed, entropy);
        }
    }

        
    /**
     * This method provides the distilled entropy.  If callers of setSeed have
     * given estimates of entropy which are either low or good, the contents of
     * this object's private state variables haven't been stolen, the macro-
     * cosmic universe has been constructed to allow the existence of 
     * unpredictability, etc. then each bit of output should be totally 
     * uncorrelated with any other bit of output and no external observer can
     * guess the value of any of these outputs.
     * 
     * @param bytes the byte array which will receive the distilled entropy.
     */
    public synchronized void nextBytes(byte bytes[]) {
        getCrew().nextBytes(bytes);
    }

    // EntropyHolder interface
    
    public void holdEntropy(byte[] entropy, int bitEstimate) {
        // store the entropy in the vat for delivery to crew on quake
        myHeldEntropyBits = entropy;
        myHeldEntropyEstimate = bitEstimate;
        tr.debugm("Hold "+bitEstimate+" entropy");
    }
}
