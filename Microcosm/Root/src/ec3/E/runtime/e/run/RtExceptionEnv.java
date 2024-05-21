package ec.e.run;

public final class RtExceptionEnv
implements RtCodeable
{
    static private Trace tr = new Trace("ec.e.run.RtExceptionEnv");
    static ECatchClosure TheNullClosure = 
        new ECatchClosure(new InternalNullECatchClosure(), null);

    //XXX made first for debugging
    private /*XXX transient*/ RtExceptionEnv myPreviousEnv;
    private EResult myClosure;

    public String toString () {
        if (myClosure == null) {
            return ("{noex}" + (myPreviousEnv == null));
        } else {
            return ("{" + myClosure + "}");
        }
    }

    public RtExceptionEnv((Throwable->void) method,
                          RtExceptionEnv previousEnv) {
        myClosure = new ECatchClosure(method, previousEnv);
        myPreviousEnv = previousEnv;
    }

    public RtExceptionEnv(RtExceptionEnv ee,
                          RtExceptionEnv previousEnv) {
        if (ee == null) {
            ee = RtRun.NULL_EXCEPTION_ENV;
        } 
        myClosure = ee.myClosure;
        myPreviousEnv = previousEnv;
    }

    public RtExceptionEnv(InternalECatchClosure catcher,
                          RtExceptionEnv previousEnv) {
        myClosure = new ECatchClosure(catcher, previousEnv);
        myPreviousEnv = previousEnv;
    }

    public RtExceptionEnv (EResult closure,
                           RtExceptionEnv previousEnv) {
        myClosure = closure;
        myPreviousEnv = previousEnv;
    }

    // If this class is changed to not be final,
    // remove optimized caching of class name! BUG?
    static String ClassName = null;
    public String classNameToEncode (RtEncoder encoder) {
        if (ClassName == null) {
            ClassName = this.getClass ().getName ();
        }
        return (ClassName);
    }

    public final void encode (RtEncoder coder) {
        try {
            coder.encodeObject (myClosure);
        } catch (Exception e) {
            RtRun.tr.errorReportException(e, "Couldn't encode exception environment!");
        }
    }

    public final Object decode (RtDecoder coder) {
        try {
            myClosure = (ECatchClosure) coder.decodeObject ();
            myPreviousEnv = null;
        } catch (Exception e) {
            RtRun.tr.errorReportException(e, "Couldn't decode exception environment!");
            return (null);
        }
        return (this);
    }

    public RtExceptionEnv pushExceptionEnv((Throwable->void) method) {
        return new RtExceptionEnv(method, this);
    }

    public RtExceptionEnv pushExceptionEnv(RtExceptionEnv ee) {
        return new RtExceptionEnv(ee, this);
    }

    public RtExceptionEnv pushExceptionEnv(InternalECatchClosure catcher) {
        return new RtExceptionEnv(catcher, this);
    }

    public RtExceptionEnv pushExceptionEnv(EResult catcher) {
        return new RtExceptionEnv(catcher, this);
    }

    public RtExceptionEnv popExceptionEnv() {
        return (myPreviousEnv);
    }

    // Actually throw the exception to the current handler
    public void doEThrow(Throwable exception) {
        EResult sendTo = myClosure;
        if (sendTo == null) {
            sendTo = TheNullClosure;
        }

        if (tr.debug && Trace.ON) tr.$("ethrow " + exception + " to " + myClosure);

        ekeep (null) {
            sendTo <- forwardException(exception);
        }
    }

    /** Send an exception to the given EE. This is a static method,
     *  so that it can deal with ee == null. */
    static public void sendException(RtExceptionEnv sendTo, Throwable e) {
        if (sendTo == null) {
            sendTo = RtRun.NULL_EXCEPTION_ENV;
        }
        sendTo.doEThrow(e);
    }

    // Get the internal EResult out of an exceptionenv
    public EResult getInternalDestination() {
        EResult sendTo = myClosure;
        if (sendTo == null) {
            sendTo = TheNullClosure;
        }
        return sendTo;
    }
}

class InternalNullECatchClosure
extends InternalECatchClosure
{
    protected void catchMe(Throwable e) {
        RtRun.tr.errorReportException(e, 
            "exception ethrown to null environment");
        // XXX - When causalityTrace fixed, uncomment
        ////exception.printCausalityTrace();
    }
}

