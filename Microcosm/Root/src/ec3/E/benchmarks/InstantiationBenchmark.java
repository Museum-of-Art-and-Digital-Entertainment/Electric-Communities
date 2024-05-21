
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


class VarObject1 { int i; }
class VarObject4 { int i, j, k, l; }


class InstantiationBenchmark extends Benchmark {
    private int     objectsPerSample;

    InstantiationBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    static long objectSize (Object obj) {
        long mem = 0, size;
        do {
            size = mem;
            mem = Runtime.getRuntime().freeMemory();
            try {
                obj.getClass().newInstance();
            }
            catch (Exception e) {
                System.err.println("InstantiationBenchmark.objectSize:" + e);
                return -1;
            }
            mem -= Runtime.getRuntime().freeMemory();
        } while (size != mem);
        return size;
    }

    static long intArraySize (int dim) {
        int[]   array;
        long    mem = 0, size;
        do {
            size = mem;
            mem = Runtime.getRuntime().freeMemory();
            array = new int[dim];
            mem -= Runtime.getRuntime().freeMemory();
        } while (size != mem);
        return size;
    }

    private void setMillis (long objsize, long sampleMillis) {
        gc();
        long mem = Math.max(20000, Runtime.getRuntime().freeMemory());
        long memObjs = (mem - 1000) / objsize - 1;
        long memMicros = (memObjs * 1000000L) / objectsPerSample;  // microseconds to fill memory
        long sampleMicros = sampleMillis * 1000;
        if (memMicros >= sampleMicros)
            setSampleMillis(sampleMillis);
        else
            setSampleMillis(Math.max((memMicros / 1000) - 10, 10));
    }

    public int getTestTime () {
        return (int) (9 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (1.6 * getTestTime());
    }

    public long runTest () {
        int         cnt, ii;
        long        objsize, millis;
        Object      obj;
        int[]       array;

        startTest();

        println("--- local = new Class()");

        millis = getSampleMillis();

        // get rough idea of how many Objects can be created during the test
        gc();
        setSampleMillis(100);
        objectsPerSample = 0;
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                obj = new Object();
            long time = stopTimer(cnt, ii);
            objectsPerSample = Math.max(objectsPerSample, (int) (((long) ii * millis) / time));
        }

        objsize = objectSize(new Object());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new Object();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("Object [" + objsize + " bytes]");

        objsize = objectSize(new DerivedObject());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new DerivedObject();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("extends Object [" + objsize + " bytes]");

        objsize = objectSize(new DerivedObject5());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new DerivedObject5();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("extends*5 Object [" + objsize + " bytes]");

        objsize = objectSize(new VarObject1());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new VarObject1();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("class {int i;} [" + objsize + " bytes]");

        objsize = objectSize(new VarObject4());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new VarObject4();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("class {int i,j,k,l} [" + objsize + " bytes]");

        objsize = objectSize(new Exception());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new Exception();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("Exception [" + objsize + " bytes]");

        objsize = objectSize(new Thread());
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new Thread();
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("Thread [" + objsize + " bytes]");

        objsize = intArraySize(1);
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new int[1];
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("int[1] [" + objsize + " bytes]");

        objsize = intArraySize(1000);
        for (cnt = getSampleCount();  --cnt >= 0; ) {
            do {
                setMillis(objsize, millis);
                startTimer(true);
                for (ii = 0;  go;  ii++)
                    obj = new int[1000];
            } while (pauseTimer(ii) < millis);
            stopTimer(cnt, 0);
        }
        report("int[1000] [" + objsize + " bytes]");

        setSampleMillis(millis);  // restore to original setting

        return finishTest();
    }
}  // class InstantiationBenchmark

// EOF
