
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


class ExceptionBenchmark extends Benchmark {
    ExceptionBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) ((4 * getSampleCount() * getSampleMillis()) / 1000);
    }

    public int getRunningTime () {
        return 2 + getTestTime();
    }

    public long runTest () {
        int         dummy1 = 0, dummy2 = 0, dummy3 = 0;  // occupy implicit index slots
        int         cnt, ii, x, y;
        Exception   ex = new Exception();

        startTest();

        println("--- try { throw e; } catch (e) {}");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++)
                try { throw ex; } catch (Exception e) {}
            stopTimer(cnt, ii);
        }
        report("Throw and catch exception");

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            gc();
            startTimer(true);
            for (ii = 0;  go;  ii++)
                try { throw new Exception(); } catch (Exception e) {}
            stopTimer(cnt, ii);
        }
        report("Throw and catch new exception");


        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++) {
                try { throw ex; }
                catch (Exception e) { x = ii; }
                y = ii;
            }
            stopTimer(cnt, ii);
        }
        long base = report(null);

        for (cnt = getSampleCount();  --cnt >= 0; ) {
            startTimer(true);
            for (ii = 0;  go;  ii++) {
                try { throw ex; }
                catch (Exception e) { x = ii; }
                finally { y = ii; }
            }
            stopTimer(cnt, ii);
        }
        report("'finally' statement", base);

        return finishTest();
    }
}  // class ExceptionBenchmark

// EOF
