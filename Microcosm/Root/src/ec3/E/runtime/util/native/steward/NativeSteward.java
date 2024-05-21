/*
 * A STEWARD class to talk to the CREW version of this class.
 */
package ec.util;

public class NativeSteward {

    /**
     * Wrapper for Native.registerPID()
     * This is called to store uCosm's PID in a well known place.
     * External application (like uCosmHelper) look for it.
     */
    public static int registerPID() {
        return Native.registerPID();
    }

    /**
     * Wrapper for Native.registerPID()
     */
    public static int registerPort(int port) {
        return Native.registerPort(port);
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

//     public static void dumpThreads() {
//         Native.dumpThreads();
//     }

    public static long queryPerformanceCounter() {
        return Native.queryPerformanceCounter();
    }
 
    public static String formatNow() {
        return Native.formatNow();
    }

    public static String format(long t) {
        return Native.format(t);
    }

    public static int flushWorkingSet() {
        return Native.flushWorkingSet();
    }

}
