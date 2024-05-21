
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


class LoopBenchmark extends Benchmark {
    int[]   useint = new int[3];

    LoopBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    static int calcIterations (boolean ideal) {
        if (timerLoopNanos <= 0)
            calibrate();
        long idealIterations = ((resolution/1000) / timerLoopNanos);
        if (ideal)
            return (int) idealIterations * 1000;
        gc();
        long mem = Runtime.getRuntime().freeMemory() - 10000;
        return (int) Math.max(25L, Math.min(mem / 8000, idealIterations)) * 1000;
    }

    public int getTestTime () {
        return (int) (1.35 * 15 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (1.1 * getTestTime());
    }

    public long runTest () {
        int     quicki, dummy1 = 0, dummy2 = 0;  // occupy implicit index slots
        int     ii = 0, jj, cnt;
        long    exceptNanos;
        int     iterations = calcIterations(false);
        int     repeat = Math.max(1, calcIterations(true) / iterations);
        int     repeatIter = repeat * iterations;
        int[]   array = new int[iterations], array2 = new int[iterations];

        startTest();

        println("--- Empty loop " + repeat + " * " + iterations + " times");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = 0;  ii < array.length;  ii++)
                    ;
            stopTimer(cnt, repeatIter);
        }
        report("for (i = 0; i < array.length; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = 0;  ii < iterations;  ii++)
                    ;
            stopTimer(cnt, repeatIter);
        }
        report("for (i = 0; i < local; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (quicki = 0;  quicki < iterations;  quicki++)
                    ;
            stopTimer(cnt, repeatIter);
        }
        report("implict i: for (i = 0; i < local; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = iterations;  --ii >= 0; )
                    ;
            stopTimer(cnt, repeatIter);
        }
        report("for (i = local; --i >= 0; )", 0);


        println("--- Loop int array[i] = i;");

/*
        if (repeat == 1)
            exceptNanos = 0;
        else {
            for (cnt = getSampleCount();  --cnt >= 0; ) {
                startTimer(false);
                for (jj = (repeat - 1) * 1000;  --jj >= 0; )
                    try { array[-1] = jj; } catch (IndexOutOfBoundsException e) {}              
                stopTimer(cnt, 1000);
            }
            exceptNanos = report(null, 0);
        }
*/
exceptNanos = 0;

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = 0;  ii < array.length;  ii++)
                    array[ii] = ii;
            stopTimer(cnt, repeatIter);
        }
        report("for (i = 0; i < array.length; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = array.length;  --ii >= 0; )
                    array[ii] = ii;
            stopTimer(cnt, repeatIter);
        }
        report("for (i = array.length; --i >= 0; )", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                try {
                    for (ii = 0;  ;  ii++)
                        array[ii] = ii;
                }
                catch (IndexOutOfBoundsException e) {}
            stopTimer(cnt, repeatIter);
        }
        report("try { for (i = 0; ; i++) }", exceptNanos);


        println("--- Loop int array[i] = array2[i];");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = 0;  ii < array.length;  ii++)
                    array[ii] = array2[ii];
            stopTimer(cnt, repeatIter);
        }
        report("for (i = 0; i < array.length; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = iterations;  --ii >= 0; )
                    array[ii] = array2[ii];
            stopTimer(cnt, repeatIter);
        }
        report("for (i = array.length; --i >= 0; )", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                try {
                    for (ii = 0;  ;  ii++)
                        array[ii] = array2[ii];
                }
                catch (IndexOutOfBoundsException e) {}
            stopTimer(cnt, ii);
        }
        report("try { for (i = 0; ; i++) }", exceptNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (ii = 50 * repeat;  --ii >= 0; )
                System.arraycopy(array, 0, array2, 0, array.length);
            stopTimer(cnt, repeatIter * 50);
        }
        report("int System.arraycopy()", 0);


        println("--- Loop byte array[i] = array2[i];");

        array = array2 = null;
        gc();
        byte[] barray = new byte[iterations], barray2 = new byte[iterations];

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = 0;  ii < barray.length;  ii++)
                    barray[ii] = barray2[ii];
            stopTimer(cnt, repeatIter);
        }
        report("for (i = 0; i < array.length; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                for (ii = iterations;  --ii >= 0; )
                    barray[ii] = barray2[ii];
            stopTimer(cnt, repeatIter);
        }
        report("for (i = array.length; --i >= 0; )", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (jj = repeat;  --jj >= 0; )
                try {
                    for (ii = 0;  ;  ii++)
                        barray[ii] = barray2[ii];
                }
                catch (IndexOutOfBoundsException e) {}
            stopTimer(cnt, repeatIter);
        }
        report("try { for (i = 0; ; i++) }", exceptNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (ii = 50 * repeat;  --ii >= 0; )
                System.arraycopy(barray, 0, barray2, 0, barray.length);
            stopTimer(cnt, repeatIter * 50);
        }
        report("byte System.arraycopy()", 0);

        useint[0] = dummy1;  useint[1] = dummy2;
        return finishTest();
    }
}

// EOF
