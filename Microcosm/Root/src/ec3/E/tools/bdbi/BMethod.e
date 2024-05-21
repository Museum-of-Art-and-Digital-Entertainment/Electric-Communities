package ec.tools.bdbi;

eclass BMethod
extends Benchmark
{
    int myIterationsUnsynch;
    int myIterationsSynch;
    long myOverheadUnsynch;
    long myOverheadSynch;
    BMethodPawn myPawn = new BMethodPawn();
    BMethodPawnIntf myPawnIntf = myPawn;

    emethod run(Bdbi after) {
        System.out.println("Method test preface: calibration");
        calibrate();

        System.out.println("Method test I: unsynchronized instance methods");
        Bdbi.annotateTime("unsynch-noArg", unsynchNoArg());
        Bdbi.annotateTime("unsynch-oneInt", unsynchOneInt());
        Bdbi.annotateTime("unsynch-twoInt", unsynchTwoInt());
        Bdbi.annotateTime("unsynch-oneObject", unsynchOneObject());
        Bdbi.annotateTime("unsynch-twoObject", unsynchTwoObject());
        Bdbi.annotateTime("unsynch-twentyObject", unsynchTwentyObject());

        System.out.println("Method test II: synchronized instance methods");
        Bdbi.annotateTime("synch-noArg", synchNoArg());
        Bdbi.annotateTime("synch-oneInt", synchOneInt());
        Bdbi.annotateTime("synch-twoInt", synchTwoInt());
        Bdbi.annotateTime("synch-oneObject", synchOneObject());
        Bdbi.annotateTime("synch-twoObject", synchTwoObject());
        Bdbi.annotateTime("synch-twentyObject", synchTwentyObject());

        System.out.println("Method test III: unsynchronized interface " +
            "methods");
        Bdbi.annotateTime("unsynch-intf-noArg", unsynchIntfNoArg());
        Bdbi.annotateTime("unsynch-intf-oneInt", unsynchIntfOneInt());
        Bdbi.annotateTime("unsynch-intf-twoInt", unsynchIntfTwoInt());
        Bdbi.annotateTime("unsynch-intf-oneObject", unsynchIntfOneObject());
        Bdbi.annotateTime("unsynch-intf-twoObject", unsynchIntfTwoObject());
        Bdbi.annotateTime("unsynch-intf-twentyObject", 
            unsynchIntfTwentyObject());

        System.out.println("Method test IV: synchronized interface methods");
        Bdbi.annotateTime("synch-intf-noArg", synchIntfNoArg());
        Bdbi.annotateTime("synch-intf-oneInt", synchIntfOneInt());
        Bdbi.annotateTime("synch-intf-twoInt", synchIntfTwoInt());
        Bdbi.annotateTime("synch-intf-oneObject", synchIntfOneObject());
        Bdbi.annotateTime("synch-intf-twoObject", synchIntfTwoObject());
        Bdbi.annotateTime("synch-intf-twentyObject", synchIntfTwentyObject());

        after <- doOne();
    }

    private void calibMethUnsynch(int x, int y) {
    }

    synchronized private void calibMethSynch(int x, int y) {
    }

    private void calibrate() {
        long start = System.currentTimeMillis();
        long target = start + Bdbi.TheBenchmarkTime;
        int count = 0;
        while (System.currentTimeMillis() < target) {
            for (int i = 0; i < 100; i++) {
                calibMethUnsynch(0, 0);
            }
            count += 100;
        }
        Bdbi.report("will run " + count + " iterations of each unsynch test");
        myIterationsUnsynch = count;

        start = System.currentTimeMillis();
        target = start + Bdbi.TheBenchmarkTime;
        count = 0;
        while (System.currentTimeMillis() < target) {
            for (int i = 0; i < 100; i++) {
                calibMethSynch(0, 0);
            }
            count += 100;
        }
        Bdbi.report("will run " + count + " iterations of each synch test");
        myIterationsSynch = count;

        start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
        }
        System.gc();
        System.gc();
        myOverheadUnsynch = System.currentTimeMillis() - start;
        Bdbi.reportTime("unsynch test overhead", myOverheadUnsynch);

        start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
        }
        System.gc();
        System.gc();
        myOverheadSynch = System.currentTimeMillis() - start;
        Bdbi.reportTime("synch test overhead", myOverheadSynch);
    }

    private double unsynchNoArg() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchNoArg();
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchOneInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchOneInt(5000);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchTwoInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchTwoInt(5000, 9999);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchOneObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchOneObject(null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchTwoObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchTwoObject(null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchTwentyObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawn.unsynchTwentyObject(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double synchNoArg() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchNoArg();
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double synchOneInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchOneInt(5000);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double synchTwoInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchTwoInt(5000, 9999);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double synchOneObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchOneObject(null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double synchTwoObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchTwoObject(null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double synchTwentyObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawn.synchTwentyObject(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / myIterationsSynch;
    }

    private double unsynchIntfNoArg() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchNoArg();
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchIntfOneInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchOneInt(5000);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchIntfTwoInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchTwoInt(5000, 9999);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchIntfOneObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchOneObject(null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchIntfTwoObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchTwoObject(null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double unsynchIntfTwentyObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsUnsynch; i++) {
            myPawnIntf.unsynchTwentyObject(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadUnsynch)) / 
            myIterationsUnsynch;
    }

    private double synchIntfNoArg() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchNoArg();
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }

    private double synchIntfOneInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchOneInt(5000);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }

    private double synchIntfTwoInt() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchTwoInt(5000, 9999);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }

    private double synchIntfOneObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchOneObject(null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }

    private double synchIntfTwoObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchTwoObject(null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }

    private double synchIntfTwentyObject() {
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        for (int i = 0; i < myIterationsSynch; i++) {
            myPawnIntf.synchTwentyObject(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        }
        System.gc();
        System.gc();
        long end = System.currentTimeMillis();
        return ((double)(end - start - myOverheadSynch)) / 
            myIterationsSynch;
    }
}

interface BMethodPawnIntf
{
    void unsynchNoArg();
    void unsynchOneInt(int a);
    void unsynchTwoInt(int a, int b);
    void unsynchOneObject(Object a);
    void unsynchTwoObject(Object a, Object b);
    void unsynchTwentyObject(Object a1, Object a2, Object a3, Object a4,
        Object a5, Object a6, Object a7, Object a8, Object a9, Object a10,
        Object a11, Object a12, Object a13, Object a14, Object a15,
        Object a16, Object a17, Object a18, Object a19, Object a20);

    void synchNoArg();
    void synchOneInt(int a);
    void synchTwoInt(int a, int b);
    void synchOneObject(Object a);
    void synchTwoObject(Object a, Object b);
    void synchTwentyObject(Object a1, Object a2, 
        Object a3, Object a4,
        Object a5, Object a6, Object a7, Object a8, Object a9, Object a10,
        Object a11, Object a12, Object a13, Object a14, Object a15,
        Object a16, Object a17, Object a18, Object a19, Object a20);
}

class BMethodPawn
implements BMethodPawnIntf
{
    public void unsynchNoArg() { }
    public void unsynchOneInt(int a) { }
    public void unsynchTwoInt(int a, int b) { }
    public void unsynchOneObject(Object a) { }
    public void unsynchTwoObject(Object a, Object b) { }
    public void unsynchTwentyObject(Object a1, Object a2, Object a3, Object a4,
        Object a5, Object a6, Object a7, Object a8, Object a9, Object a10,
        Object a11, Object a12, Object a13, Object a14, Object a15,
        Object a16, Object a17, Object a18, Object a19, Object a20) { }

    synchronized public void synchNoArg() { }
    synchronized public void synchOneInt(int a) { }
    synchronized public void synchTwoInt(int a, int b) { }
    synchronized public void synchOneObject(Object a) { }
    synchronized public void synchTwoObject(Object a, Object b) { }
    synchronized public void synchTwentyObject(Object a1, Object a2, 
        Object a3, Object a4,
        Object a5, Object a6, Object a7, Object a8, Object a9, Object a10,
        Object a11, Object a12, Object a13, Object a14, Object a15,
        Object a16, Object a17, Object a18, Object a19, Object a20) { }
}
