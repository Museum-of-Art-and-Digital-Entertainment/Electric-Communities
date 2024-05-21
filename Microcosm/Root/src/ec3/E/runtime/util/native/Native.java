/*
 * A class to hold all the miscellaneous system-dependent utilities
 * Microcosm might need.
 */
package ec.util;

import java.text.DecimalFormat;


public class Native {

    static  {
        try {
            System.loadLibrary("ecutil");
        }
        catch(UnsatisfiedLinkError e) {
            System.out.println("ecutil linkage error" + e);
        }
        initializeTimer();

        // We no longer register the PID at initialization. 
        // uCosm should do it explicitly.
    }

    private static 
    DecimalFormat myDecimalFormat = new DecimalFormat("0000000000000000");

    /**
     * Set the working set of this process to 0. This causes the OS to 
     * reload pages as they're touched. Only for NT/95.
     */
    public static native 
    int flushWorkingSet();
 
    /** 
     * Dump the threads. Equivalent to hitting Ctrl-Pause (or Break).
     */
  //    public static synchronized native 
  //    void dumpThreads(); 

    /** 
     * Write the process ID of the current process to a well-known
     * place.
     */
    public static native 
    int registerPID();

    /**
     * Register a port number with the system registry so uCosmHelper
     * can find it later.
     */
    public static native 
    int registerPort(int port);


    // high-res timer code

    /** 
     * Initialize the high-res timer.
     */
    public static native
    void initializeTimer();

    /**
     * Get the current value of the high-res timer in microseconds.
     * @return the current value of the high-res timer in microseconds.
     */
    public static native
    long queryTimer();

    // Utils
    public static
    long deltaTimerUSec(long start, long end) {
      return end - start;
    }

    public static
    long deltaTimerMSec(long start, long end) {
      return (end - start)/1000;
    }

    public static
    long deltaTimerSec(long start, long end) {
      return (end - start)/1000000;
    }

    // Immediate Utils (use the current clock as stoptimed)
    public static
    long deltaTimerUSec(long start) {
      return queryTimer() - start;
    }

    public static
    long deltaTimerMSec(long start) {
      return (queryTimer() - start)/1000;
    }

    public static
    long deltaTimerSec(long start) {
      return (queryTimer() - start)/1000000;
    }

    
    public static native int getPhysicalMemorySizeNative();

    public static int getPhysicalMemorySize() {
        return getPhysicalMemorySizeNative();
    }

    /**
     * Get the current value of the Pentium Performance counter.
     * @return the current value of the Pentium Performance counter.
     */
    public static native
    long queryPerformanceCounter();

    /**
     * Return the time in a preformatted way, suitable for sorting.
     */
    public static String 
    formatNow() {
            return myDecimalFormat.format(queryTimer());
    }


    /**
     * Return a long in a preformatted way, suitable for sorting.
     */
    public static String
    format(long time) {
            return myDecimalFormat.format(time);
    }

   /**
    * Given a valid URL, does whatever the system does when you
    * launch a URL.
    */
    public static native int openURL(String url);

}




