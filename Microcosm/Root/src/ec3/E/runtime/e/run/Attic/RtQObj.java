package ec.e.run;

// A helper class for message queues
public class RtQObj
extends Object
{
    static private RtQObj[] TheCache = null;
    static private int TheCacheMax = 0;
    static private int TheCacheSize = 0;
    static private int TheNextSeqNum = 0;

    private RtTether myTarget;
    private RtSealer mySealer;
    private Object[] myArgs;
    private RtExceptionEnv myEE;
    private int mySeqNum;

    /** Only this class can instantiate itself. You get a "new" one
     * of these with the "make" method. */
    private RtQObj() {
    }

    public String toString() {
        return "#" + mySeqNum + " " + 
            RtEnvelope.messageToString(myTarget, mySealer, myArgs, myEE);
    }

    public RtTether getTarget() {
        return myTarget;
    }

    public RtSealer getSealer() {
        return mySealer;
    }

    public RtExceptionEnv getKeeper() {
        return myEE;
    }

    public int getArgCount() {
        return myArgs.length;
    }

    public Object getArg(int n) {
        return myArgs[n];
    }

    public int getSeqNum() {
        return mySeqNum;
    }

    /** Note, this intentionally clones the args. */
    public Object[] getArgs() {
        Object[] result = new Object[myArgs.length];
        System.arraycopy(myArgs, 0, result, 0, myArgs.length);
        return result;
    }

    /** Make a "new" (probably actually recycled) RtQObj, with the
     * given message parameters.
     */
    static public RtQObj make(RtTether t, RtSealer s, 
        Object[] a, RtExceptionEnv e) {
        if (t == null) {
            throw new RtRuntimeException(
                "tried to make message with null target");
        }
        if (s == null) {
            throw new RtRuntimeException(
                "tried to make message with null sealer");
        }
        if (a == null) {
            throw new RtRuntimeException(
                "tried to make message with null args");
        }

        RtQObj me;
        // get it from the cache if possible; otherwise make a new one
        if (TheCacheSize != 0) {
            TheCacheSize--;
            me = TheCache[TheCacheSize];
        } else {
            me = new RtQObj();
        }

        me.myTarget = t;
        me.mySealer = s;
        me.myArgs = a;
        me.myEE = e;
        me.mySeqNum = TheNextSeqNum;
        TheNextSeqNum++;
        
        //if (RtRun.tr.debug) {
        //    RtRun.tr.debugm("made qobj " + me);
        //}
        
        return me;
    }

    /** Deliver the message in this RtQObj and immediately recycle
     * the object. It is not appropriate to hold onto an RtQObj
     * after having called this method.
     */
    public final void run() {
        //if (RtRun.tr.debug) {
        //    RtRun.tr.debugm("delivering " + this);
        //}

        // any setting up of exception env must be done explicitly
        // by a tether/eobject
        RtRun.CurrentExceptionEnv = null;

        try {
            myTarget.invokeNow(mySealer, myArgs, myEE);
        } catch (Throwable e) {
            RtRun.tr.errorm("Uncaught Throwable made it all the way to " +
                "the top; This shouldn't happen, but we'll be nice " +
                "and turn it into an ethrow like a happyhappy citizen.");
            RtRun.tr.errorReportException(e, 
                "Uncaught Throwable during send of " + this);
            if (RtCausality.TheOne.myCausalityTracing) {
                String causalityTrace = RtCausality.getCausalityTraceString();
                RtRun.tr.errorm("Causality trace:");
                RtRun.tr.errorm(causalityTrace);
            }
            myEE.doEThrow(e);
        } finally {
            RtRun.CurrentExceptionEnv = null;
        }

        // put this into the cache
        if (TheCacheSize == TheCacheMax) {
            // we need to grow the cach
            int newMax = (TheCacheMax * 3) / 2 + 100;
            RtQObj[] newCache = new RtQObj[newMax];
            if (TheCacheMax != 0) {
                System.arraycopy(TheCache, 0, newCache, 0, TheCacheMax);
            }
            TheCache = newCache;
            TheCacheMax = newMax;
        }
        TheCache[TheCacheSize] = this;
        TheCacheSize++;

        // don't forget to null everything out--we don't want to leak
        // this stuff.
        myTarget = null;
        mySealer = null;
        myArgs = null;
        myEE = null;
    }
}
