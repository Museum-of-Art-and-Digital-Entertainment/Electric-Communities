
/*
 * Copyright (c) 1996, 1997 by Doug Bell <dbell@shvn.com>.  All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */


class DerivedObject {}
class DerivedObject2 extends DerivedObject {}
class DerivedObject3 extends DerivedObject2 {}
class DerivedObject4 extends DerivedObject3 {}
class DerivedObject5 extends DerivedObject4 {}

interface Inter1 {}
interface Inter2 extends Inter1 {}
interface Inter3 extends Inter2 {}
interface Inter4 extends Inter3 {}
interface Inter5 extends Inter4 {}
interface Inter6 extends Inter5 {}


/*
 * This is the base benchmark class.  To create a new benchmark, extend this class and
 * implement getTestTime(), getRunningTime() and runTest() in the extended class.
 */

public class Benchmark implements Runnable {
    private static boolean      calibrated;
    private static long         gcMemTarget;
    private static String[]     resNames = {" ms", " µs", " ns", " ps"};
    private static long[]       resValues = {1000L, 1000000L, 1000000000L, 1000000000000L};

    protected static int        resIndex = 2;       // ns = nanosecond
    protected static String     resolutionName = resNames[resIndex];
    protected static long       resolution = resValues[resIndex];
    protected static long       timerLoopNanos;
    protected static int[]      useint = new int[3]; // used to prevent dead variable elimination

    private BenchmarkRunner     runner;
    private Thread              timer;
    private long                sampleMillis = 1000L;   // 1 second for each test
    private int                 sampleCount = 3;
    private int                 timerIterations;
    private boolean             timerPaused;
    private long                timerStart, reportMillis;
    private int[]               iterations = new int[sampleCount];
    private long[]              milliseconds = new long[sampleCount];

    protected long              timerMillis, totalMillis;
    protected volatile boolean  go;


    public static void gc () {
        System.runFinalization();
        System.gc();
        if (Runtime.getRuntime().freeMemory() < gcMemTarget) {
            try {
                int[] mem = new int[(int) gcMemTarget/4];
                mem = null;
            }
            catch (OutOfMemoryError e) {
                gcMemTarget -= 10000;
                recalibrate();
            }
            System.gc();
        }
        try { Thread.sleep(100); } catch (InterruptedException e) {}
    }


    public static void recalibrate () {
        calibrated = false;
    }


    public static void calibrate () {
        calibrated = false;
        new Benchmark().runTest();
        if (timerLoopNanos < 98) {
            if (resIndex < resValues.length - 1) {
                resIndex++;
                resolutionName = resNames[resIndex];
                resolution = resValues[resIndex];
                calibrate();
            }
        }
        else if (timerLoopNanos > 102000) {
            if (resIndex > 0) {
                resIndex--;
                resolutionName = resNames[resIndex];
                resolution = resValues[resIndex];
                calibrate();
            }
        }
        if (!calibrated) {
            gcMemTarget = 0;
            gc();
            long sysmem = Math.min(1000000, Runtime.getRuntime().totalMemory() - 10000);
            gcMemTarget = Math.min(1000000, Runtime.getRuntime().freeMemory() - 5000);
            if (true|| gcMemTarget < 200000  &&  sysmem > gcMemTarget) {
                boolean ok;
                gcMemTarget = sysmem;
                do {
                    ok = true;
                    try {
                        int[] mem = new int[(int) gcMemTarget/4];
                        mem = null;
                    }
                    catch (OutOfMemoryError e) {
                        gcMemTarget -= 10000;
                        ok = false;
                    }
                } while (!ok);
            }
            gcMemTarget = Math.min(sysmem, gcMemTarget);
            calibrated = true;
        }
    }


    static long getNanos (long[] millis, int[] iterations, long overheadNanos) {
        long nanos = resolution * 100;
        // System.out.println("millis " + millis.length + " " + iterations.length);
        for (int ii = Math.min(millis.length, iterations.length);  --ii >= 0; ) {
          // System.out.println("millis " + ii + " is " + millis[ii] + " " + iterations[ii]);
          if (iterations[ii] > 0)
            nanos = Math.min(nanos,
                     (((resolution/1000) * millis[ii]) / iterations[ii]) -
                     overheadNanos);
          iterations[ii] = 0;
          millis[ii] = 0;
        }
        return nanos;
    }


    Benchmark () {
        this(null);
    }

    Benchmark (BenchmarkRunner runner) {
        this.runner = runner;
    }


    /** Returns the name of the test.
     *  Default implementation uses the unqualified class name, stripping 'Benchmark'
     *  from the name if it exists.
     */
    public String getName () {
        String name = getClass().getName();
        int index = name.lastIndexOf('.');
        name = name.substring(index < 0 ? 0 : index);
        if (name.equals("Benchmark"))
            name = "Calibrate";
        else if (name.endsWith("Benchmark"))
            name = name.substring(0, name.length() - "Benchmark".length());
        return name;
    }


    /** Returns approximate running time of justs the timing tests, in seconds.
     *  Subclass should override this method.
     */
    public int getTestTime () {
        return (int) (getSampleCount() * getSampleMillis()) / 1000;
    }


    /** Returns approximate total running time of the benchmark, in seconds.
     *  Subclass should override this method.
     */
    public int getRunningTime () {
        return getTestTime();
    }


    /** Set the number of samples to measure for a test.
     */
    final void setSampleCount (int samples) {
        if (samples != sampleCount  &&  samples > 0) {
            sampleCount = samples;
            iterations = new int[samples];
            milliseconds = new long[samples];
        }
    }

    /** Get the number of samples to measure for a test.
     */
    final int getSampleCount () {
        return sampleCount;
    }


    /** Set the number of milliseconds to run the timer.
     */
    final void setSampleMillis (long millis) {
        if (millis > 0)
            sampleMillis = millis;
    }

    /** Get the number of milliseconds to run the timer.
     */
    final long getSampleMillis () {
        return sampleMillis;
    }


    /** Should be called at the beginning of runTest().
     */
    protected void startTest () {
        println("");
        println("=== " + getName() + " Benchmark ===");
        gc();
        if (!calibrated  ||  timerLoopNanos <= 0)
            calibrate();
        reportMillis = totalMillis = 0;
        // System.out.println("Done starting test (including calibration.");
    }

    /** Should be called at the end of runTest() to return running time for test.
     */
    protected long finishTest () {
        runnerReport("=== " + getName() + " TOTAL:", reportMillis);
        return totalMillis;
    }


    /** Runs the benchmark.  Returns total running time for all subtests.
     *  Subclasses should override this method.
     */
    public long runTest () {
        int dummy1 = 0, dummy2 = 0, dummy3 = 0;  // occupy implicit index slots
        int sample, ii, total = 0;

        // System.out.println("Benchmark:runTest");

        totalMillis = 0;

        for (sample = getSampleCount();  --sample >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                ;
            stopTimer(sample, ii);
            total += ii;
        }
        timerLoopNanos =
                report("timed loop overhead [iterations=" + total/getSampleCount() + "]", 0);

        // System.out.println("TimerLoopNanos = " + timerLoopNanos);
        return totalMillis;
    }


    /** Timer thread. */
    public void run () {
      // System.out.println("Timer thread running.");
        go = true;
        try {
            try { Thread.sleep(sampleMillis); } catch (InterruptedException e) {}
        }
        finally {
          // System.out.println("Timer thread stopping.");
            go = false;
            timer = null;
        }
    }

    private void killTimer () {
        // protect stop() in case timer completed after test
        if (timer != null  &&  timer.isAlive())
            try { timer.stop(); } catch (NullPointerException e) {}
    }


    protected final void startTimer (boolean useThread) {
        if (timerPaused)
            timerPaused = false;
        else {
            timerMillis = 0;
            timerIterations = 0;
        }
        if (useThread) {
            timer = new Thread(this);
            timer.setPriority(Math.min(Thread.currentThread().getPriority() + 2,
                                       Thread.MAX_PRIORITY - 1));
            // System.out.println("Timer.start.");
            timer.start();
            while (!go)
                Thread.yield();
            // System.out.println("Timer.start.");
        }
        timerStart = System.currentTimeMillis();
        // System.out.println("Timer starts at " + timerStart);
    }

    protected final long pauseTimer (int iterations) {
        long timerStop = System.currentTimeMillis();
        killTimer();
        if (!timerPaused) {
            timerPaused = true;
            timerMillis += timerStop - timerStart;
        }
        timerIterations += iterations;
        return timerMillis;
    }

    protected final long stopTimer (int sample, int iterations) {
        long timerStop = System.currentTimeMillis();
        // System.out.println("Timer stops at " + timerStop);
        killTimer();
        if (!timerPaused)
            timerMillis += timerStop - timerStart;
        timerPaused = false;
        // System.out.println("sample is " + sample + " timerMillis is " + timerMillis + " iterations " + iterations);
        if (sample >= 0) {
            milliseconds[sample] = timerMillis;
            this.iterations[sample] = timerIterations + iterations;
        }
        totalMillis += timerMillis;
        return timerMillis;
    }


    long report (String msg) {
        return report(msg, milliseconds, iterations, timerLoopNanos);
    }

    long report (String msg, long overheadNanos) {
        return report(msg, milliseconds, iterations, overheadNanos);
    }

    long report (String msg, long[] millis, int[] iterations, long overheadNanos) {
        long nanos = getNanos(millis, iterations, overheadNanos);
        runnerReport(msg, nanos);
        reportMillis += nanos;
        return nanos;
    }

        long reportDetail(String msg) {
      println(msg);
      println("Loop iteration overhead is " + timerLoopNanos);
      int samples = Math.min(milliseconds.length, iterations.length);
      println(samples + " samples were taken.");
      for (int ii = samples; --ii >= 0; ) {
        println("Sample " + ii + " had " + iterations[ii] + " iterations in "
            + milliseconds[ii] + " milliseconds.");
      }
      return report(msg);
    }


    void runnerReport (String msg, long nanos) {
        if (runner != null)
            runner.report(msg, nanos);
    }

    void println (String s) {
        if (runner != null)
            runner.println(s);
    }
}

// EOF
