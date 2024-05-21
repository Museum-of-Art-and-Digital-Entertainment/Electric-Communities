
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


/* {

These versions don't work because of a bug in Netscape's Thread.join().

class PingPongThread extends Thread {
    static long     start, stop;
    static int      counter, iterations;
    static Object   ball = new Object();

    PingPongThread () {
        iterations = counter = (int) ((Benchmark.resolution/1000) / Benchmark.timerLoopNanos);
        new PingPongThread(true).start();
        start = System.currentTimeMillis();
        start();
    }

    PingPongThread (boolean dummy) {}

    public void run () {
        synchronized (ball) {
            ball.notify();
            try {
                while (--counter >= 0) {
                    ball.wait();
                    ball.notify();
                }
            }
            catch (InterruptedException e) {}
        }
        stop = System.currentTimeMillis();
    }
}  // class PingPongThread


class PingPongAllThread extends Thread {
    static long     start, stop;
    static int      counter, iterations;
    static Object   ball = new Object();

    PingPongAllThread () {
        iterations = counter = (int) ((Benchmark.resolution/1000) / Benchmark.timerLoopNanos);
        new PingPongAllThread(true).start();
        start = System.currentTimeMillis();
        start();
    }

    PingPongAllThread (boolean dummy) {}

    public void run () {
        synchronized (ball) {
            ball.notifyAll();
            try {
                while (--counter >= 0) {
                    ball.wait();
                    ball.notifyAll();
                }
            }
            catch (InterruptedException e) {}
        }
        stop = System.currentTimeMillis();
    }
}  // class PingPongAllThread


class PingPongThread extends Thread {
    static long     start, stop;
    static int      counter, iterations;
    static Object   ball = new Object();
    static int      count;      // workaround for Netscape 'join()' bug

    PingPongThread () {
        count = 2;
        iterations = counter = (int) ((Benchmark.resolution/1000) / Benchmark.timerLoopNanos);
        new PingPongThread(true).start();
        start = System.currentTimeMillis();
        start();
    }

    PingPongThread (boolean dummy) {}

    public void run () {
        synchronized (ball) {
            ball.notify();
            try {
                while (--counter >= 0) {
                    ball.wait();
                    ball.notify();
                }
            }
            catch (InterruptedException e) {}
        }
        stop = System.currentTimeMillis();
        count--;
    }
}  // class PingPongThread


class PingPongAllThread extends Thread {
    static long     start, stop;
    static int      counter, iterations;
    static Object   ball = new Object();
    static int      count;      // workaround for Netscape 'join()' bug

    PingPongAllThread () {
        count = 2;
        iterations = counter = (int) ((Benchmark.resolution/1000) / Benchmark.timerLoopNanos);
        new PingPongAllThread(true).start();
        start = System.currentTimeMillis();
        start();
    }

    PingPongAllThread (boolean dummy) {}

    public void run () {
        synchronized (ball) {
            ball.notifyAll();
            try {
                while (--counter >= 0) {
                    ball.wait();
                    ball.notifyAll();
                }
            }
            catch (InterruptedException e) {}
        }
        stop = System.currentTimeMillis();
        count--;
    }
}  // class PingPongAllThread

} */


class MemThread extends Thread {
    long startMem;
    public void run () {
        startMem = Runtime.getRuntime().freeMemory();
    }
}


class PingPongThread extends Thread {
    static int          count;
    static boolean      go;
    static int          iterations;
    Object              ball;
    Benchmark           runner;
    int                 sample;
    PingPongThread      partner;

    PingPongThread (Benchmark runner, int sample) {
        this.runner = runner;
        this.sample = sample;
        ball = new Object();
        iterations = 0;
        go = true;
        count = 2;
        partner = new PingPongThread();
        partner.ball = ball = new Object();
        partner.start();
        start();
    }

    PingPongThread () {}

    public void run () {
        if (runner != null)
            runner.startTimer(false);
        try {
            synchronized (ball) {
                Thread.yield();  // without this, Netscape will deadlock
                ball.notify();
                try {
                    while (go) {
                        iterations++;
                        ball.wait();
                        ball.notify();
                    }
                }
                catch (InterruptedException e) {}
            }
        }
        finally {
            if (runner != null)
                runner.stopTimer(sample, iterations);
            count--;
        }
    }
}  // class PingPongThread


class PingPongAllThread extends Thread {
    static int          iterations;
    static int          count;
    static boolean      go;
    Benchmark           runner;
    Object              ball;
    int                 sample;
    PingPongAllThread   partner;

    PingPongAllThread (Benchmark runner, int sample) {
        this.runner = runner;
        this.sample = sample;
        iterations = 0;
        go = true;
        count = 2;
        partner = new PingPongAllThread();
        partner.ball = ball = new Object();
        partner.start();
        start();
    }

    PingPongAllThread () {}

    public void run () {
        if (runner != null)
            runner.startTimer(false);
        try {
            synchronized (ball) {
                Thread.yield();  // without this, Netscape will deadlock
                ball.notifyAll();
                try {
                    while (go) {
                        iterations++;
                        ball.wait();
                        ball.notifyAll();
                    }
                }
                catch (InterruptedException e) {}
            }
        }
        finally {
            if (runner != null)
                runner.stopTimer(sample, iterations);
            count--;
        }
    }
}  // class PingPongAllThread


class ThreadBenchmark extends Benchmark {
    ThreadBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) (8 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (2.2 * getTestTime());
    }

    public long runTest () {
        int         dummy1 = 0, dummy2 = 0, dummy3 = 0;  // occupy implicit index slots
        long        createNanos, runNanos;
        int         cnt, ii, loop, iters;
        Thread      thread = new Thread();
        Thread[]    threads = new Thread[25];
        int         tlen = threads.length;
        int         sampleCount = getSampleCount();
        int[]       createIters = new int[sampleCount], runIters = new int[sampleCount];
        long[]      createMillis = new long[sampleCount], runMillis = new long[sampleCount];

        startTest();

        println("--- new Thread().start()");

        // get rough idea of how many Threads to create for timing loop
        iters = 0;
        for (long millis = System.currentTimeMillis();  System.currentTimeMillis() - millis < 100;
                iters++)
            new Thread();
        iters *= 10;

        // for kicks, lets see how much memory is involved with starting a Thread
        long mem, startUpSize0 = 0, startUpSize1;
        do {
            startUpSize1 = startUpSize0;
            MemThread t = new MemThread();
            mem = Runtime.getRuntime().freeMemory();
            t.start();
            while (t.startMem == 0)
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            startUpSize0 = t.startMem - mem;
        } while (startUpSize0 != startUpSize1);

        for (cnt = sampleCount;  --cnt >= 0; ) {
            for (loop = iters / tlen;  --loop >= 0; ) {
                startTimer(false);
                for (ii = tlen;  --ii >= 0; )
                    threads[ii] = thread;
                createMillis[cnt] += stopTimer(cnt, tlen);
                createIters[cnt] += tlen;

                startTimer(false);
                for (ii = tlen;  --ii >= 0; )
                    threads[ii].run();
                runMillis[cnt] += stopTimer(cnt, tlen);
                runIters[cnt] += tlen;
            }
        }
        createNanos = report(null, createMillis, createIters, 0);
        runNanos = report(null, runMillis, runIters, 0);

        for (cnt = sampleCount;  --cnt >= 0; ) {
            createMillis[cnt] = runMillis[cnt] = 0;
            createIters[cnt] = runIters[cnt] = 0;
            for (loop = iters / tlen;  --loop >= 0; ) {
                gc();
                startTimer(false);
                for (ii = tlen;  --ii >= 0; )
                    threads[ii] = new Thread();
                createMillis[cnt] += stopTimer(cnt, tlen);
                createIters[cnt] += tlen;

                startTimer(false);
                for (ii = tlen;  --ii >= 0; )
                    threads[ii].start();
                runMillis[cnt] += stopTimer(cnt, tlen);
                runIters[cnt] += tlen;
            }
        }
        report("Instantiating Thread [" +
                    InstantiationBenchmark.objectSize(new Thread()) + " bytes]",
                createMillis, createIters, createNanos);
        report("Starting and stopping a Thread [" + startUpSize0 + " bytes]",
                runMillis, runIters, runNanos);

        println("--- wait()/notify()");


        /* NOTE:
         *   There are at least five other ways to time wait()/notify().  Why do I know?
         *   Because I tried them, and each time there was some problem with one or two
         *   different JVMs.  The following was what finally seemed to work on each one...
         *   at least as far as I've tested.
         */

        setSampleCount(sampleCount << 1);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            thread = new PingPongThread(this, cnt);
            try { Thread.sleep(getSampleMillis()); } catch (InterruptedException e) {}
            PingPongThread.go = false;
            while (PingPongThread.count > 0)
                try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
        report("wait()/notify() task switch");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            thread = new PingPongAllThread(this, cnt);
            try { Thread.sleep(getSampleMillis()); } catch (InterruptedException e) {}
            PingPongAllThread.go = false;
            while (PingPongAllThread.count > 0)
                try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
        report("wait()/notifyAll() task switch");

        setSampleCount(sampleCount);


        useint[0] = dummy1;  useint[1] = dummy2;  useint[2] = dummy3;
        return finishTest();
    }
}  // class ThreadBenchmark

// EOF
