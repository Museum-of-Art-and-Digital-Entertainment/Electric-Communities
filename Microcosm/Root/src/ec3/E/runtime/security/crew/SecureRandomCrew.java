// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.security.crew;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import ec.e.start.FragileRootHolder;
import ec.e.start.SmashedException;
import ec.security.EntropyHolder;
import ec.util.Native;


/**
 * Cryptographically Strong Random Number Generator
 *
 * <p>This class provides a cryptographically strong random number
 * generator based on user provided sources of entropy and the MD5
 * hash algorithm.  It maintains an estimate of the amount of entropy 
 * supplied.  If there is a request for secure random data, a call on
 * nextBytes(), when there is less than 80 bits of randomness available,
 * it will use super.getSeed() to bring the level up to 80 bits.
 *
 * <p>The calls inherited from Random and SecureRandom are implemented 
 * in terms of the strengthened functionality.
 *
 * @see java.util.Random
 * @see java.security.SecureRandom
 *
 * @author Bill Frantz 
 */
public class SecureRandomCrew extends SecureRandom {
    //Constants

    private static final Trace tr = new Trace("ec.security.SecureRandomCrew");

    private static SecureRandomCrew theSecureRandomCrew = null;

    // Static state for gathering mouse and keyboard entropy
    private static int[] theLastKeys = new int[5];  // Save last for keys
    private static int theLastX;        // The last mouse x value
    private static int theLastY;        // The last mouse y value
    private static int theLastDX;       // The last change in mouse x value
    private static int theLastDY;       // The last change in mouse y value

    
    //Output size of the hash function used
    private static final int HASH_SIZE = 16;

    //Size of the entropy pool in bits.  This constant limits the amount of actual
    //entropy that goes into building the secure random output.
    // N.B. must be a multiple of HASH_SIZE.
    private static final int MAX_ENTROPY = 16*HASH_SIZE*8;
    private static final int MIN_ENTROPY = 160;     //Bits

    //State
    private byte[] myEntropyPool = new byte[MAX_ENTROPY/8]; //Storage for entropy
    private int myPoolCursor = 0;                   //Next place in the pool
    private int myAvailableEntropy = 0; // Bits of entropy introduced into pool

    private MessageDigest myMD = null;  //Distilling function
    private long myDigestNumber = 0;     //Increment for each use of myMD
    private FragileRootHolder myInnerRandom;

    static public void provideEntropy(byte entropy[], int bitEstimate) {
        if (theSecureRandomCrew == null) {
            theSecureRandomCrew = new SecureRandomCrew(entropy, bitEstimate);
        }
        else {
            theSecureRandomCrew.setSeed(entropy, bitEstimate);
        }
    }


    /**
      * This method accepts a mouse event to input entropy.
      * 
      * @param x the mouse event x value.
      * @param y the mouse event y value.
      * @param type the mouse event type code.
      */

    static public void setMouseSeed(int x, int y, int type) {
        int dx = theLastX - x;
        int dy = theLastY - y;
        if (0 == dx && 0 == dy) return; // No entropy
        theLastX = x;
        theLastY = y;

        int ddx = theLastDX - dx;
        int ddy = theLastDY - dy;
        if (0 == ddx && 0 == ddy) return; // No entropy
        theLastDX = ddx;
        theLastDY = ddy;

        byte[] seed = { (byte) ((x>>24) & 0xff),
                        (byte) ((x>>16) & 0xff),
                        (byte) ((x>> 8) & 0xff),
                        (byte) ((x    ) & 0xff),
                        (byte) ((y>>24) & 0xff),
                        (byte) ((y>>16) & 0xff),
                        (byte) ((y>> 8) & 0xff),
                        (byte) ((y    ) & 0xff),
                        (byte) ((type>> 8) & 0xff),
                        (byte) ((type    ) & 0xff) };

        provideEntropy(seed,1);
        if (tr.verbose && Trace.ON) tr.verbosem("setMouseSeed: x="+x+" y="+y+" type="+type);
    }

    /**
      * This method accepts a keyboard event to input entropy.
      * 
      * @param key the keyboard code.
      * @param modifiers the modifier keys pressed.
      * @param type the mouse event type code.
      */

    static public void setKeySeed(int key, int modifiers, int type) {
        boolean entropy = true;

        for (int i=1; i<theLastKeys.length; i++) {
            if (theLastKeys[i] == key) entropy=false;   // No entropy
            theLastKeys[i-1] = theLastKeys[i];  // Push down our memory
        }
        theLastKeys[theLastKeys.length-1] = key;
        if (!entropy) return;

        byte[] seed = { (byte) ((key>> 8) & 0xff),
                        (byte) ((key    ) & 0xff),
                        (byte) ((modifiers>> 8) & 0xff),
                        (byte) ((modifiers    ) & 0xff),
                        (byte) ((type>> 8) & 0xff),
                        (byte) ((type    ) & 0xff) };

        provideEntropy(seed,1);
        if (tr.verbose && Trace.ON) {
            tr.verbosem("setKeySeed: key="+key+" modifiers="+modifiers
                    +" type="+type);
        }
    }

    /**
       Return the singular instance of a SecureRandomCrew.  Creates it
       (using the thread yeald seeding) if necessary.

       @param innerRandom nullOK an EntropyHolder to keep entropy in the vat.
    */
    static public SecureRandomCrew getTheSecureRandomCrew(
                    FragileRootHolder innerRandom,
                    byte[] /*nilok*/ entropy,
                    int bitEstimate) 
    {
        if (theSecureRandomCrew == null) {
            if (null == entropy) {
                byte[] dummySeed = new byte[1]; // One bit from Native.queryTimer()
                theSecureRandomCrew = new SecureRandomCrew(dummySeed, 1);
            } else {
                theSecureRandomCrew = new SecureRandomCrew(entropy, bitEstimate);
            }
        }
        if (innerRandom != null) {
            theSecureRandomCrew.myInnerRandom = innerRandom;
        }
        return theSecureRandomCrew;
    }


    //Constructors

    private SecureRandomCrew() {}
    
    /**
      * This constructor takes a user-provided seed and entropy estimate. 
      * it is the preferred constructor call, if there is any initial entropy
      * available.
      * 
      * @param seed the seed.
      * @param entropy an estimate of the amount of entropy in seed in bits.
      *
      * @exception IllegalArgumentException entropy estimate is less than 1 bit.
      * @exception IllegalArgumentException entropy estimate is greater than 8
      *            bits for every byte of the seed.
      */

    private SecureRandomCrew(byte seed[], int entropy) {
        //The super(seed) call here calls our super class constructor and avoids
        //its no parameter constructor's slow self-seeding algorithm.  Note that 
        //the constructor will call setSeed, which comes back to the setSeed 
        //method below.  setSeed will be called before the initializer code for
        //this instance has been run, so variables such as myMD will
        //have their default (null) value.
        //
        //All this nonsense just goes to show what a mess this hierarchy is.  
        //java.security.SecureRandom should really be an interface, and not a class.
        super(seed);    //Our setSeed will ignore the seed
        try {
            myMD = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not available.");
        }
        setSeed(seed, entropy); //Now that the world is build, process the seed
        if (tr.debug && Trace.ON) tr.$("SecureRandomCrew: constructor");
    }


    //Manipulators

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
        //must be prepared to handle receiving entropy before the places to
        //hold it are set up through automatic initialization.  We will detect
        //the situation by noting that myMD is null and quickly
        //bail.  We will then set things up in our own constructor when the
        //state of "this" is a bit more sane.
        if (null == myMD) return;
        if (entropy < 1) {
            throw new IllegalArgumentException("Less than one bit of entropy");
        }
        if (entropy  >  8 * seed.length) {
            throw new IllegalArgumentException("More entropy than data");
        }
        myAvailableEntropy += entropy;
        if (myAvailableEntropy > 4*myEntropyPool.length) {
            myAvailableEntropy = 4*myEntropyPool.length; //Never more than 1/2 full
        }
        while (entropy > 0) {
            byte[] hashBlock = new byte[HASH_SIZE];
            System.arraycopy(myEntropyPool, myPoolCursor, hashBlock, 0, HASH_SIZE);
            myMD.update(hashBlock);
            myMD.update(long2bytes(Native.queryTimer())); // Stir in the time
            hashBlock = myMD.digest(seed);
            System.arraycopy(hashBlock, 0, myEntropyPool, myPoolCursor, HASH_SIZE);
            myPoolCursor += HASH_SIZE;
            if (myEntropyPool.length == myPoolCursor) myPoolCursor = 0;
            entropy -= HASH_SIZE*4;
        }
    }
    


    /**
      * This method provides the secure random output.
      * 
      * @param bytes the byte array which will receive the secure random output.
      */

    public void nextBytes(byte bytes[]) {
        if (tr.debug && Trace.ON) tr.$("SecureRandomCrew: nextBytes " + bytes.length);
        int cursor = 0;
    
        if (myAvailableEntropy < MIN_ENTROPY) { // Need to gather entropy
            long genSeedTime = 0;
            long waitSeedTime = 0;
            int seedLength = 0;
            while (myAvailableEntropy < MIN_ENTROPY) { // Wait for seeding
                long startTime = tr.event ? System.currentTimeMillis() : 0;
                if (TimerJitterEntropy.isStarted()) {   // Is there entropy coming?
                    try {
                        Thread.sleep(500);
                    } catch(InterruptedException e) {
                        // Ignore it
                    }
                    waitSeedTime += (System.currentTimeMillis() - startTime);
                } else {
                    // Do setSeed(getSeed(1), 8); in separate thread with timeout
                    Thread generator = new SecureRandomCrewSeedIt(this);
                    generator.start();
                    try {
                        generator.join(10000);
                    } catch(InterruptedException e) {
                        tr.errorm("Seed generator interrupted" + e);
                        // Ignore it
                    }
                    if (generator.isAlive()) {
                        generator.stop();
                        tr.eventm("Seed generator timeout");
                    } else {
                        seedLength++;
                    }
                    genSeedTime += (System.currentTimeMillis() - startTime);
                }
            }
            if (tr.event && waitSeedTime > 450) {
                tr.eventm("Seeding delay " + (waitSeedTime) + " milliseconds");
            }
            if (tr.event && genSeedTime > 0)  {
                tr.eventm("getSeed(" 
                            + seedLength 
                            + "), time " 
                            + genSeedTime 
                            + " milliseconds");
            }
        }

        synchronized(this) {
            while (cursor < bytes.length) {
                myMD.update(long2bytes(myDigestNumber));
                myDigestNumber++;
                myMD.update(long2bytes(Native.queryTimer()));
                byte[] rand = myMD.digest(myEntropyPool);
                int len = java.lang.Math.min(rand.length, bytes.length - cursor);
                System.arraycopy(rand, 0, bytes, cursor, len);
                cursor += len;
            }
        }
    }


    private byte[] long2bytes(long number) {
        byte[] ans = new byte[8];
        for (int i=7; i>=0; i--) {
            ans[i] = (byte)number;
            number >>= 8;
        }
        return ans;
    }


    //Accessors

    /**
      * Get the amount of entropy the generator now holds.
      *
      * @return the number of bits of entropy in the generator.
      */
    public int availableEntropy() {
        return myAvailableEntropy;
    }

}


class SecureRandomCrewSeedIt extends Thread {

    private SecureRandomCrew mySecureRandom;

    public SecureRandomCrewSeedIt(SecureRandomCrew sr) {
        super("SecureRandomCrewSeedIt");
        mySecureRandom = sr;
    }

    public final void run() {
        mySecureRandom.setSeed(mySecureRandom.getSeed(1), 8);
    }
}
