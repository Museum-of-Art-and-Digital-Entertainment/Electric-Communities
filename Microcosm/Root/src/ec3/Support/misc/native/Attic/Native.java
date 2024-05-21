/*
 * A class to hold all the miscellaneous system-dependent utilities
 * Microcosm might need.
 */
package ec.misc;


public class Native {

    static  {
      try {
          System.loadLibrary("ecutils");
      }
      catch(UnsatisfiedLinkError e) {
          System.out.println("ecutils linkage error");
          e.printStackTrace();
      }
      initializeTimer();
    }

    /** 
     * Write the process ID of the current process to a well-known
     * place.
     */
    public static native 
    int registerPID();

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

}
