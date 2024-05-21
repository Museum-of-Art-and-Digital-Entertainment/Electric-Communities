
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


class CastObject extends DerivedObject5 implements Inter6 {}
class CastObject1 implements Inter1 {}

class MyPoint { int x, y; }


class CastingBenchmark extends Benchmark {
    int x = 0, y = 0;

    CastingBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) ((35 * getSampleCount() * getSampleMillis()) / 1000);
    }

    public int getRunningTime () {
        return 4 + getTestTime();
    }

    public long runTest () {
        int             dummy1 = 0, dummy2 = 0, dummy3 = 0;  // occupy implicit index slots
        long            baseNanos, baseLNanos, baseILNanos;
        int             cnt, ii;
        boolean         test;
        Object          obj;
        Object          dobj = new DerivedObject();
        Object          cobj = new CastObject();
        Object          c1obj = new CastObject1();
        DerivedObject   derived = (DerivedObject) dobj;
        CastObject      cast;
        Inter1          inter1;
        Inter6          inter6;
        byte            b1 = 0;
        short           s1 = 0;
        char            c1 = 0;
        int             i1 = 0;
        long            l1 = 0;
        float           f1 = 0;
        double          d1 = 0;

        startTest();

        println("--- (class) object");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj = dobj;
            stopTimer(cnt, ii);
        }
        baseNanos = report(null) + timerLoopNanos;

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                derived = (DerivedObject) dobj;
            stopTimer(cnt, ii);
        }
        report("1/1 generation narrow cast", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                derived = (DerivedObject) cobj;
            stopTimer(cnt, ii);
        }
        report("1/6 generation narrow cast", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                cast = (CastObject) cobj;
            stopTimer(cnt, ii);
        }
        report("6/6 generation narrow cast", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                inter1 = (Inter1) c1obj;
            stopTimer(cnt, ii);
        }
        report("1/1/1 generation interface cast", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                inter1 = (Inter1) cobj;
            stopTimer(cnt, ii);
        }
        report("6/6/6 generation interface cast", baseNanos);


        println("--- object instanceof class");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = true;
            stopTimer(cnt, ii);
        }
        baseNanos = report(null) + timerLoopNanos;

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = derived instanceof DerivedObject;
            stopTimer(cnt, ii);
        }
        report("0/0 generation instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = dobj instanceof DerivedObject;
            stopTimer(cnt, ii);
        }
        report("1/1 generation instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = cobj instanceof DerivedObject;
            stopTimer(cnt, ii);
        }
        report("1/6 generation instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = cobj instanceof CastObject;
            stopTimer(cnt, ii);
        }
        report("6/6 generation instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = c1obj instanceof Inter1;
            stopTimer(cnt, ii);
        }
        report("1/1/1 generation interface instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = cobj instanceof Inter1;
            stopTimer(cnt, ii);
        }
        report("6/6/6 generation interface instanceof", baseNanos);

        cast = (CastObject) cobj;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = cast instanceof Inter6;
            stopTimer(cnt, ii);
        }
        report("0/1/1 generation interface instanceof", baseNanos);

        inter6 = (Inter6) cobj;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = inter6 instanceof Inter1;
            stopTimer(cnt, ii);
        }
        report("5/5 generation interface instanceof", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                test = inter6 instanceof Inter6;
            stopTimer(cnt, ii);
        }
        report("0/0 generation interface instanceof", baseNanos);


        println("--- (class) int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                i1 = i1;
            stopTimer(cnt, ii);
        }
        baseNanos = report(null, 0);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                l1 = l1;
            stopTimer(cnt, ii);
        }
        baseLNanos = report(null, 0);
        baseILNanos = (baseNanos + baseLNanos) >> 1;

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                b1 = (byte) i1;
            stopTimer(cnt, ii);
        }
        report("byte = (byte) int", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                c1 = (char) i1;
            stopTimer(cnt, ii);
        }
        report("char = (char) int", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                s1 = (short) i1;
            stopTimer(cnt, ii);
        }
        report("short = (short) int", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                l1 = (long) i1;
            stopTimer(cnt, ii);
        }
        report("long = (long) int", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                f1 = (float) i1;
            stopTimer(cnt, ii);
        }
        report("float = (float) int", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                d1 = (double) i1;
            stopTimer(cnt, ii);
        }
        report("double = (double) int", baseILNanos);


        println("--- (class) class");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                i1 = (int) l1;
            stopTimer(cnt, ii);
        }
        report("int = (int) long", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                i1 = (int) f1;
            stopTimer(cnt, ii);
        }
        report("int = (int) float", baseNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                i1 = (int) d1;
            stopTimer(cnt, ii);
        }
        report("int = (int) double", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                l1 = (long) f1;
            stopTimer(cnt, ii);
        }
        report("long = (long) float", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                l1 = (long) d1;
            stopTimer(cnt, ii);
        }
        report("long = (long) double", baseLNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                f1 = (float) l1;
            stopTimer(cnt, ii);
        }
        report("float = (float) long", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                f1 = (float) d1;
            stopTimer(cnt, ii);
        }
        report("float = (float) double", baseILNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                d1 = (double) l1;
            stopTimer(cnt, ii);
        }
        report("double = (double) long", baseLNanos);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                d1 = (double) f1;
            stopTimer(cnt, ii);
        }
        report("double = (double) float", baseILNanos);


        println("--- cast examples");

        obj = new MyPoint();

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                if (obj instanceof MyPoint)
                    test = (((MyPoint)obj).x == this.x && ((MyPoint)obj).y == this.y);
                else
                    test = false;
            stopTimer(cnt, ii);
        }
        report("equals test with two casts");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                if (obj instanceof MyPoint) {
                    MyPoint p = (MyPoint)obj;
                    test = (p.x == this.x && p.y == this.y);
                }
                else
                    test = false;
            stopTimer(cnt, ii);
        }
        report("equals test with p = (MyPoint)obj;");


        useint[0] = dummy1;  useint[1] = dummy2;  useint[2] = dummy3;
        return finishTest();
    }
}  // class CastingBenchmark

// EOF
