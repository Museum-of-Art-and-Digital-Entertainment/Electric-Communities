package ec.tools.bdbi;

eclass BEmethod
extends Benchmark
{
    int myIterations;
    long myOverhead;
    BEmethodPawn myPawn = new BEmethodPawn();
    BEmethodJava myJPawn = (BEmethodJava) myPawn;
    Bdbi myAfter;

    emethod run(Bdbi after) {
        System.out.println("E Method test preface: calibration");
        myAfter = after;
        this <- calibrate();
    }

    emethod calibrate() {
        EBoolean calibDone = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor calibDone_dist = EUniChannel.getDistributor(calibDone);
        myJPawn.setDone(calibDone_dist);
        myJPawn.setCount(0);
        long start = System.currentTimeMillis();
        myJPawn.setTime(start + Bdbi.TheBenchmarkTime);
        myPawn <- calibMeth();
        ewhen calibDone (Object ignored) {
            myIterations = myJPawn.getCount();
            Bdbi.report("will run " + myIterations + 
                " iterations of each emethod test");

            long ostart = System.currentTimeMillis();
            System.gc();
            System.gc();
            for (int i = 0; i < myIterations; i++) {
            }
            System.gc();
            System.gc();
            myOverhead = System.currentTimeMillis() - ostart;
            Bdbi.reportTime("emethod test overhead", myOverhead);
            System.out.println("E Method test: the real thing");
            this <- noArg();
        }
    }

    emethod noArg() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- noArg();
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-noArg", t);
            this <- oneInt();
        }
    }

    emethod oneInt() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- oneInt(5000);
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-oneInt", t);
            this <- twoInt();
        }
    }

    emethod twoInt() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- twoInt(5000, 9999);
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-twoInt", t);
            this <- oneObject();
        }
    }

    emethod oneObject() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- oneObject(null);
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-oneObject", t);
            this <- twoObject();
        }
    }

    emethod twoObject() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- twoObject(null, null);
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-twoObject", t);
            this <- twentyObject();
        }
    }

    emethod twentyObject() {
        EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor done_dist = EUniChannel.getDistributor(done);
        myJPawn.setDone(done_dist);
        myJPawn.setCount(myIterations);
        long start = System.currentTimeMillis();
        System.gc();
        System.gc();
        myPawn <- twentyObject(null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null);
        ewhen done (Object ignored) {
            double t = ((double)(myJPawn.getTime() - start - myOverhead)) /
                myIterations;
            Bdbi.annotateTime("emethod-twentyObject", t);
            myAfter <- doOne();
        }
    }
}

interface BEmethodJava
{
    void setCount(int count);
    void setTime(long time);
    void setDone(EResult done);
    int getCount();
    long getTime();
}

eclass BEmethodPawn
implements BEmethodJava
{
    int count;
    EResult done = null;
    long time;

    public void setCount(int count) {
        this.count = count;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDone(EResult done) {
        this.done = done;
    }

    public int getCount() {
        return count;
    }

    public long getTime() {
        return time;
    }

    emethod calibMeth() {
        count++;
        if ((count & 0xff) == 0) {
            if (System.currentTimeMillis() < time) {
                this <- calibMeth();
            } else {
                done <- forward(etrue);
            }
        } else {
            this <- calibMeth();
        }
    }

    emethod noArg() {
        count--;
        if (count != 0) {
            this <- noArg();
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }

    emethod oneInt(int a) { 
        count--;
        if (count != 0) {
            this <- oneInt(5000);
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }

    emethod twoInt(int a, int b) { 
        count--;
        if (count != 0) {
            this <- twoInt(5000, 9999);
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }

    emethod oneObject(Object a) { 
        count--;
        if (count != 0) {
            this <- oneObject(null);
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }

    emethod twoObject(Object a, Object b) { 
        count--;
        if (count != 0) {
            this <- twoObject(null, null);
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }

    emethod twentyObject(Object a1, Object a2, Object a3, Object a4,
        Object a5, Object a6, Object a7, Object a8, Object a9, Object a10,
        Object a11, Object a12, Object a13, Object a14, Object a15,
        Object a16, Object a17, Object a18, Object a19, Object a20) { 
        count--;
        if (count != 0) {
            this <- twentyObject(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        } else {
            System.gc();
            System.gc();
            time = System.currentTimeMillis();
            done <- forward(etrue);
        }
    }
}
