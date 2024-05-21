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
 * Use clock phase differences between 2 PC clocks to generate entropy.
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
public class TimerJitterEntropy {
    static private final Trace tr = new Trace("ec.security.TimerJitterEntropy");

    static private boolean theIsStarted = false;

    static public void start() {
        if ("x86".equals(System.getProperty("os.arch"))) {
            Thread generator = new TimerJitterEntropyGenerator(
                        "TimerJitterEntropyGenerator");
            generator.setDaemon(true);
            generator.setPriority(Thread.MAX_PRIORITY);
            generator.start();
            theIsStarted = true;
        }
    }

    static public boolean isStarted() {
        return theIsStarted;
    }

}


class TimerJitterEntropyGenerator extends Thread {

    static private final Trace tr = new Trace("ec.security.TimerJitterEntropyGenerator");

    public TimerJitterEntropyGenerator(String name) {
        super(name);
    }

    public final void run() {

        byte[] ran = new byte[1];
        int bitPtr = 0;
        long entropy = 0;

        int delay = 50;

        while (true) {
            try {
                Thread.sleep(delay);    // Cut the CPU overhead by waiting for 
                                        // most of the PC tick interval.
            } catch(InterruptedException e) {
                return;
            }
            long last = System.currentTimeMillis();
            long now = System.currentTimeMillis();
            // Hang out until the TOD clock ticks
            while (now == last) {
                now = System.currentTimeMillis();
            }
            long nowHR = Native.queryTimer();
            ran[0] |= ( (nowHR & 1) << bitPtr);
            bitPtr++;
            if (8 == bitPtr) {
                // Assume 1/2 bit entropy/sample
                SecureRandomCrew.provideEntropy(ran, 4);
                entropy += 4;       // Incr entropy gotten
                if (entropy == 160) {   // Slow down after 160 bits
                    tr.eventm("Slow down point reached");
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    delay = 500;
                }
                ran[0] = 0;
                bitPtr = 0;
            }
        }
    }
}
