/*
 * A STEWARD class to talk to the CREW version of this class.
 */
package ec.util;

import ec.misc.Native;

public class NativeSteward {

    /**
     * Register our PID with the OS so uCosmHelper can find it.
     */
    public static int registerPID() {
        return Native.registerPID();
    }

    /**
     * Get the current value of the high-res timer in microseconds.
     * @return the current value of the high-res timer in microseconds.
     */
    public static long queryTimer() {
        return Native.queryTimer();
    }

    /**
     * Given two high-res timer values, return the number of
     * microseconds in the delta between them.
     *
     * @param start the start time
     * @param end the end time
     */
    public static long deltaTimerUSec(long start, long end) {
        return Native.deltaTimerUSec(start, end);
    }

    /**
     * Given two high-res timer values, return the number of
     * milliseconds in the delta between them.
     *
     * @param start the start time
     * @param end the end time
     */
    public static long deltaTimerMSec(long start, long end) {
        return Native.deltaTimerMSec(start, end);
    }

    /**
     * Given two high-res timer values, return the number of
     * seconds in the delta between them.
     *
     * @param start the start time
     * @param end the end time
     */
    public static long deltaTimerSec(long start, long end) {
        return Native.deltaTimerSec(start, end);
    }

    public static long deltaTimerSec(long start) {
        return Native.deltaTimerSec(start);
    }

    public static long deltaTimerMSec(long start) {
        return Native.deltaTimerMSec(start);
    }

    public static long deltaTimerUSec(long start) {
        return Native.deltaTimerUSec(start);
    }
}
