package ec.e.run;

import ec.util.EThreadGroup;

import ec.e.start.Vat;                  // The Vat so we can make Tethers
import ec.e.start.Tether;               // The Tether so we can make Vats
import ec.util.NestedError;             // Error reporting is good for you

public interface RunQueueDebugger {     // Added for Inspector
    public void preRunOne(RtQ theQueue, Object vatLock);
}

/**
 * In these pre-EVM days, there may only be one RtRun object, and it
 * is also a root of its (therefore singular) vat.  As long as there
 * is anything enqueued on its runQ, the RtRun's run() method will be
 * run by a Thread.  Should the runQ run out, run() will
 * return, and the Thread will die.  Should anything later get
 * enqueued, a new thread will be started.  <p>
 *
 * XXX explain reason: process exit & why daemon's don't do it. <p>
 *
 * As a special case, a freshly decoded Vat contains a freshly decoded
 * RtRun whose runQ may not be empty, but which still won't yet be
 * running in a Thread.  This allows surgery to be performed (like
 * quake damage) before setting the runQ in motion.  If this surgery
 * might enqueue messages, it should grab hold the vatLock, as
 * enqueue'ing messages will set the RtRun going.  Once the surgery is
 * done, call beRunnable() to set the RtRun going, in case no fresh
 * enqueues happened.
 */
public class RtRun implements Runnable {
    /** The number of qobjs to dequeue and run in one go. */
    static private final int DEQUEUE_GRANULARITY = 25;

    /** The max amount of time (in msec) to spend just waiting
     * for a message to be enqueued. */
    static private final int MAX_WAIT_TIME = 1000;

    static public Trace            tr = new Trace("ec.e.run.RtRun");
    static /*package*/ Object      TheVatLock;
    static private RtRun           TheRtRun;
    static /*package*/ RtCausality TheCausality;
    static /*package*/ Thread      OurThread = null;
    static public final RtExceptionEnv
                                NULL_EXCEPTION_ENV = new RtExceptionEnv
                                           ((ECatchClosure) null, null);
    static RtExceptionEnv       CurrentExceptionEnv = NULL_EXCEPTION_ENV;

    static /*package*/ RtQ TheQ;
    private RtQ    myQ = new RtQ();
    /*package*/ static boolean TheNeedsNotify = false;

    private Vat myVat;
    
    private RtCausality myCausality;

    private Tether myRunQueueDebugHookTether = null; // Added for Inspector

    static private final int FinalizersSize = 16384;
    static private Object TheFinalizers[];
    static private int ReallyFinalizeSomebodyCount;
    static private Object TheReallyFinalizeLock = null;
    // Need to preallocate errors that could be thrown in queueReallyFinalize
    private static Error ReallyFinalizeError;

    /**
     * Makes a Runnable
     */
    public RtRun(Vat vat, Object vatLock) /*throws OnceOnlyException*/ {
        synchronized(vatLock) {
            if (TheVatLock != null) {
                //throw new OnceOnlyException("Can only be one RtRun");
                throw new RuntimeException("Can only be one RtRun");
            }
            TheVatLock = vatLock;
            TheRtRun = this;
            TheQ = myQ;
            myCausality = new RtCausality();
            TheCausality = myCausality;
            myVat = vat;
            beReadyToFinalize();
            initializeThread();
        }
    }

    /**
     * public (for now) method to set the RunQueue debugger hook
     * We provide no way to turn this off.
     * XXX This needs to be changed to require some capability.
     */
    public void setRunQueueDebugger(RunQueueDebugger newHook) throws OnceOnlyException {
        if (myRunQueueDebugHookTether != null) {
            throw new OnceOnlyException("Can only have one Run Queue Debug Hook");
        }
        myRunQueueDebugHookTether = new Tether(myVat, newHook);
    }

    RunQueueDebugger getRunQueueDebugHook ()  {
        RunQueueDebugger runQueueDebugHook = null;
        try {
            runQueueDebugHook = (RunQueueDebugger)myRunQueueDebugHookTether.held();
        } catch (Exception e) {
            // Set the Tether to null so we won't waste time again
            myRunQueueDebugHookTether = null;
            if (Trace.eruntime.debug && Trace.ON) Trace.eruntime.debugm("Curiously, RunQueueDebugHook was smashed");
        }
        return runQueueDebugHook;
    }

    /**
     * Returns the one instance of RtRun.
     */
    static public RtRun theOne() {
        return TheRtRun;
    }

    /**
     * For use in reviving from a checkpoint
     */
    static public void setStatics(RtRun runner, Object vatLock)
         throws OnceOnlyException {

        if (runner == null) {
            throw new IllegalArgumentException("runner mustn't be null");
        }
        if (vatLock == null) {
            throw new IllegalArgumentException("vatLock mustn't be null");
        }
        if (TheRtRun != null) {
            throw new OnceOnlyException("Can only be one E runQ");
        }
        TheRtRun = runner;
        TheCausality = runner.myCausality;
        RtCausality.TheOne = TheCausality;
        TheVatLock = vatLock;
        // OK, I lied this isn't technically a static.
        TheRtRun.myRunQueueDebugHookTether = null; // So can be set afresh
        beReadyToFinalize();
        TheQ = runner.myQ;
        TheRtRun.initializeThread();
    }

    /** Set up the run thread. */
    private final void initializeThread() {
        OurThread = new Thread(this, "E delivery thread");
        OurThread.start();
    }

    /**
     * Returns the E runQ thread's current exception environment.
     * 'RtRun.NULL_EXCEPTION_ENV' will always be returned in
     * lieu(sp?) of null, or if called from a thread other than the E
     * runQ thread.
     */
    static public RtExceptionEnv exceptionEnv() {
        if (Thread.currentThread() != OurThread
            || CurrentExceptionEnv == null) {

            return NULL_EXCEPTION_ENV;
        }
        return CurrentExceptionEnv;
    }

    /**
     * Called from finalize methods in Objects in the Vat, which must do
     * whatever it is they want to do in Finalize from within the Vat
     * context. We'll make note of this Object and call reallyFinalize
     * on it when we get the chance from within the Vat context.
     */
    public static void queueReallyFinalize (RtFinalizer finalizer)  {
        // WARNING - can't allocate anything in here since we're
        // potentially out of memory when this is called
        if (ReallyFinalizeSomebodyCount >= TheFinalizers.length) {
            // Has to have been preallocated, can't do "new Error(...)"!!!
            throw ReallyFinalizeError;
        }
        synchronized(TheReallyFinalizeLock)  {
            TheFinalizers[ReallyFinalizeSomebodyCount++] = finalizer;
        }
    }

    /**
     * Called by Thread.start().  Pulls pending deliveries off of the
     * runQ until there aren't any more, then waits until there's more
     * to do, checking for finalizers at the predetermined wait granularity.
     */
    public final void run() {
        for (;;) {
            try {
                // do any needed finalization stuff
                checkForFinalizers();
                
                // give the inspector/debug hook a chance to play with things
                if (myRunQueueDebugHookTether != null) {
                    RunQueueDebugger hook = getRunQueueDebugHook();
                    if (hook != null)  {
                        hook.preRunOne(myQ, TheVatLock);
                    }
                    synchronized (TheVatLock) {
                        waitForMessages();
                    }
                } else {
                    synchronized (TheVatLock) {
                        for (int i = 0; i < DEQUEUE_GRANULARITY; i++) {
                            RtQObj qobj = myQ.dequeue();
                            if (qobj == null) {
                                waitForMessages();
                                break;
                            }
                            qobj.run();
                        }
                    }
                }
            } catch (Throwable t) {
                if (tr.error) {
                    tr.errorReportException(t, 
                        "Exception made it all the way out of the run " +
                        "loop. Restarting it.");
                }
            }
        }
    }

    /** Called by run() when we're out of messages in the queue,
     *  or after the debug hook does its biz. 
     *  Must be called while the VatLock is held. */
    private final void waitForMessages() {
        if (myQ.empty()) {
            if (tr.debug && Trace.ON) {
                tr.debugm("E runtime out of messages. sleeping now.");
            }
            TheNeedsNotify = true;
            try {
                TheVatLock.wait(MAX_WAIT_TIME);
            } catch (InterruptedException e) {
                // ignore
            }
            TheNeedsNotify = false;
        }
    }
        
    // BUG--should not be public, and should only take _$_Impls
    static public void enqueue(EObject obj, RtEnvelope e) {
        if (obj instanceof EObject_$_Impl) {
            RtEnqueue.enq((RtTether) obj, e.mySealer, e.myEE, e.myArgs);
        } else {
            // is a deflector, not a real eobject
            ((RtTether) obj).invoke(e.mySealer, e.myArgs, e.myEE);
        }
    }

    // BUG--this is the wrong "form factor", but we need it
    // at least temporarily until enqueue/invoke/deliver/invokeNow
    // get sorted out
    static public void alwaysEnqueue(RtTether obj, RtEnvelope e) {
        RtEnqueue.enq(obj, e.mySealer, e.myEE, e.myArgs);
    }

    /**
     * Initializes the data used for managing in Vat finalization
     */
    private static void initializeFinalizerData() {
        ReallyFinalizeSomebodyCount = 0;
        TheReallyFinalizeLock = new Object();
        TheFinalizers = new Object[FinalizersSize];
        ReallyFinalizeError = new Error("Cannot set delayed finalize for Vat Context");
        queueReallyFinalize(new FinalizerDummy()); // Cache this to avoid deadlock
    }

    /** Make sure the finalization subsystem is ready. This
     *  prevents some startup order problems in the E runtime. */
    static void beReadyToFinalize() {
        if (TheReallyFinalizeLock == null) {
            initializeFinalizerData();
        }
    }

    /**
     * Private method to check if anything in the Vat needs to be finalized. Vat
     * things can't typically do much in finalize except request to have their
     * reallyFinalize() method called from within the Vat - mainly to avoid both
     * deadlock, and corrupting the Vat.
     */
    private void checkForFinalizers ()  {
        while (ReallyFinalizeSomebodyCount > 0) {
            RtFinalizer finalizer = null;
            synchronized(TheReallyFinalizeLock)  {
                ReallyFinalizeSomebodyCount--;
                finalizer = (RtFinalizer)TheFinalizers[ReallyFinalizeSomebodyCount];
                TheFinalizers[ReallyFinalizeSomebodyCount] = null;
            }
            // At this point we're no longer holding TheReallyFinalizeLock
            if (finalizer != null)  {
                try {
                    // Need to grab VatLock inside the try, else deadlock
                    // could happen trying to report any exceptions in catch
                    synchronized (TheVatLock) {
                        if (Trace.eruntime.debug && Trace.ON) Trace.eruntime.debugm("Really finalizing " + finalizer +
                            " at index " + ReallyFinalizeSomebodyCount);
                        finalizer.reallyFinalize();
                    }
                }
                catch (Throwable t) {
                    EThreadGroup.reportException(t);
                }
            }
        }
    }

    // BUG--get rid of unused newHandler param;
    // need to modify code gen for this
    static public void pushExceptionEnv(EObject newHandler,
            (Throwable->void) method) {
        if (Thread.currentThread() != OurThread) {
            throw new Error("Cannot etry/ecatch outside the vat");
        }
        RtExceptionEnv currExEnv = exceptionEnv().pushExceptionEnv(method);
        if (tr.debug && Trace.ON) tr.debugm("now " + currExEnv);
        CurrentExceptionEnv = currExEnv;
    }

    static public void pushExceptionEnv(RtExceptionEnv ee) {
        // it's okay to say "ekeep (null)" in a non-E thread, but it's
        // only okay to say "ekeep (<non-null>)" from E computation...
        // for now. As of a future ecomp, it'll be okay.
        if (Thread.currentThread() != OurThread) {
            if (ee != null) {
                throw new Error ("ekeep (<non-null>) is only legal in the " +
                    "vat... for now");
            }
        } else {
            RtExceptionEnv currExEnv = exceptionEnv().pushExceptionEnv(ee);
            if (tr.debug && Trace.ON) tr.debugm("now " + currExEnv);
            CurrentExceptionEnv = currExEnv;
        }
    }

    static public void pushExceptionEnv(InternalECatchClosure catcher) {
        if (Thread.currentThread() != OurThread) {
            throw new Error("Cannot etry/ecatch outside the vat");
        }
        RtExceptionEnv currExEnv = exceptionEnv().pushExceptionEnv(catcher);
        if (tr.debug && Trace.ON) tr.debugm("now " + currExEnv);
        CurrentExceptionEnv = currExEnv;
    }

    static public void popExceptionEnv()
    {
        if (Thread.currentThread() != OurThread) {
            // ignore popExceptionEnv if it's from a non-E thread.
            // we need this since the ekeep(null) pattern ends up
            // spitting out a call to this, and it *is* legal from
            // non-E threads.
        } else {
            RtExceptionEnv currExEnv = exceptionEnv().popExceptionEnv();
            if (tr.debug && Trace.ON) tr.debugm("now " + currExEnv);
            CurrentExceptionEnv = currExEnv;
        }
    }

    /**
     * This is a "cheating" way to start up an E runtime. It is
     * merely provided to make some testing easier.
     */
    static public void bootCheat() {
        new RtRun(null, new Object());
    }

    /**
     * This is a "cheating" way to spit out debug messages. It is
     * merely provided to make some testing easier. This code
     * should not be called in anything that ships.
     */
    static public void debugCheat(String msg) {
        System.err.println(msg);
    }

    /**
     * This is a "cheating" way to spit out stack traces. It is
     * merely provided to make some testing easier. This code
     * should not be called in anything that ships.
     */
    static public void stackTraceCheat(Throwable t) {
        EThreadGroup.reportException(t);
    }
}
