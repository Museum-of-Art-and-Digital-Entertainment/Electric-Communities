
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


class MixedBenchmark extends Benchmark {
    private static int  staticInt;
    private int         instanceInt;

    static int          staticMethod ()         { return 0; }
    int                 instanceMethod ()       { return 0; }
    synchronized int    instanceSyncMethod ()   { return 0; }

    MixedBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) (19 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (1.15 * getTestTime());
    }

    public long runTest () {
        int     quicki = 0, dummy1 = 0, dummy2 = 0;  // occupy implicit index slots
        int     cnt, ii;

        startTest();


        int loopIter = LoopBenchmark.calcIterations(true);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (ii = 0;  ii < loopIter;  ii++)
                ;
            stopTimer(cnt, loopIter);
        }
        report("for ( ; i < local; i++)", 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(false);
            for (ii = loopIter;  --ii >= 0; )
                ;
            stopTimer(cnt, loopIter);
        }
        report("for ( ; --i >= 0; )", 0);


        int localInt = 0;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                localInt = localInt;
            stopTimer(cnt, ii);
        }
        report("local int x = x");

        int[] intArray = new int[1];
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                intArray[0] = intArray[0];
            stopTimer(cnt, ii);
        }
        report("local int array[0] = array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                staticInt = staticInt;
            stopTimer(cnt, ii);
        }
        report("static int x = x");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                instanceInt = instanceInt;
            stopTimer(cnt, ii);
        }
        report("instance int x = x");


        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                staticMethod();
            stopTimer(cnt, ii);
        }
        report("static method() {return 0;}");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                instanceMethod();
            stopTimer(cnt, ii);
        }
        report("instance int method() {return 0;}");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                instanceSyncMethod();
            stopTimer(cnt, ii);
        }
        report("instance synchronized method() {return 0;}");


        byte b1 = 1, b2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                b1 += b2;
            stopTimer(cnt, ii);
        }
        report("local byte += byte");

        short s1 = 1, s2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                s1 += s2;
            stopTimer(cnt, ii);
        }
        report("local short += short");

        int i1 = 1, i2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                i1 += i2;
            stopTimer(cnt, ii);
        }
        report("local int += int");

        long l1 = 1, l2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                l1 += l2;
            stopTimer(cnt, ii);
        }
        report("local long += long");

        float f1 = 1, f2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                f1 += f2;
            stopTimer(cnt, ii);
        }
        report("local float += float");

        double d1 = 1, d2 = 2;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                d1 += d2;
            stopTimer(cnt, ii);
        }
        report("local double += double");


        Object          dobj = new DerivedObject();
        DerivedObject   derived;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                derived = (DerivedObject) dobj;
            stopTimer(cnt, ii);
        }
        report("Subclass s = (Subclass) superclass");

        Object  c1obj = new CastObject1();
        Inter1  inter1;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                inter1 = (Inter1) c1obj;
            stopTimer(cnt, ii);
        }
        report("Interface i = (Interface) object");


        for (cnt = getSampleCount();  --cnt >= 0; ) {
            gc();
            startTimer(true);
            for (ii = 0;  go;  ii++)
                new Object();
            stopTimer(cnt, ii);
        }
        report("new Object()");


        for (cnt = getSampleCount();  --cnt >= 0; ) {
            gc();
            startTimer(true);
            for (ii = 0;  go;  ii++)
                try { throw new Exception(); } catch (Exception e) {}
            stopTimer(cnt, ii);
        }
        report("Throw and catch new Exception()");


        useint[0] = quicki;  useint[1] = dummy1;  useint[2] = dummy2;
        return finishTest();
    }
}  // class MixedBenchmark

// EOF
