
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


class VarTestObject {
    static int      staticInt;
    int             instanceInt;
    static long     staticLong;
    long            instanceLong;
}


class VariableBenchmark extends Benchmark {
    private static int  staticInt;
    private int         instanceInt;
    private static long staticLong;
    private long        instanceLong;

    VariableBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) (18 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (1.1 * getTestTime());
    }

    public long runTest () {
        int             quickLocalInt = 0;
        long            quickLocalLong = 0;
        int             cnt, ii;
        int             localInt = 0;
        long            localLong = 0;
        byte[]          byteArray = new byte[1];
        short[]         shortArray = new short[1];
        int[]           intArray = new int[1];
        long[]          longArray = new long[1];
        float[]         floatArray = new float[1];
        double[]        doubleArray = new double[1];
        VarTestObject   obj = new VarTestObject();

        startTest();

        println("--- int variableX = variableX");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                localInt = localInt;
            stopTimer(cnt, ii);
        }
        report("local int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                quickLocalInt = quickLocalInt;
            stopTimer(cnt, ii);
        }
        report("implicit local int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                intArray[0] = intArray[0];
            stopTimer(cnt, ii);
        }
        report("local int array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                staticInt = staticInt;
            stopTimer(cnt, ii);
        }
        report("this static int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                instanceInt = instanceInt;
            stopTimer(cnt, ii);
        }
        report("this instance int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj.staticInt = obj.staticInt;
            stopTimer(cnt, ii);
        }
        report("obj static int");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj.instanceInt = obj.instanceInt;
            stopTimer(cnt, ii);
        }
        report("obj instance int");


        println("--- long variableX = variableX");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                localLong = localLong;
            stopTimer(cnt, ii);
        }
        report("local long");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                quickLocalLong = quickLocalLong;
            stopTimer(cnt, ii);
        }
        report("implicit local long");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                longArray[0] = longArray[0];
            stopTimer(cnt, ii);
        }
        report("local long array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                staticLong = staticLong;
            stopTimer(cnt, ii);
        }
        report("this static long");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                instanceLong = instanceLong;
            stopTimer(cnt, ii);
        }
        report("this instance long");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj.staticLong = obj.staticLong;
            stopTimer(cnt, ii);
        }
        report("obj static long");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj.instanceLong = obj.instanceLong;
            stopTimer(cnt, ii);
        }
        report("obj instance long");


        println("--- local arrayX[0] = arrayX[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                byteArray[0] = byteArray[0];
            stopTimer(cnt, ii);
        }
        report("local byte array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                shortArray[0] = shortArray[0];
            stopTimer(cnt, ii);
        }
        report("local short array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                floatArray[0] = floatArray[0];
            stopTimer(cnt, ii);
        }
        report("local float array[0]");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                doubleArray[0] = doubleArray[0];
            stopTimer(cnt, ii);
        }
        report("local double array[0]");

        return finishTest();
    }
}  // class VariableBenchmark

// EOF
