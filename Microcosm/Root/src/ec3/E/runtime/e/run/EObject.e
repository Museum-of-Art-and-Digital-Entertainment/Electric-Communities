// E-Support, (C) Electric Communities (and Michael Philippsen),
// 1997, all rights reserved.
// =============================================================

package ec.e.run;

eclass EObject
implements Cloneable, Exportable
{
    // Identity is maintained invariant for all exports of this Object
    private long identity = 0L;

    emethod when(EResult method) {
        if (method != null) {
            Object val;
            val = value();

            // XXX (GJF) Seems like we can do this optimization
            // XXX (danfuzz) Not really, this should get excised if
            // it ever gets in the way of fixing other E runtime aspects
            ((RtTether)method).invokeNow(
                sealer (EResult <- forward(Object)),
                new Object[] {val},
                myKeeper());
        } else {
            System.err.println("*** error: when got a null closure");
        }
    }

    emethod order(RtEnvelope msg, EResult r) {
        if (msg != null && r != null) {
            //System.out.println ("When calling back with value " + val +
            //" to method " + method);
            invokeNow(msg.mySealer, msg.myArgs, msg.myEE);
            r <- forward(this);
        } else {
            System.err.println("order got empty envelope or result");
        }
    }

    emethod respond(EResult result) {
        if (result != null) {
            result <- forward(this);
        } else {
            System.err.println("*** error: respond got a null closure");
        }
    }

    emethod messageWithCause(RtSealer seal, RtExceptionEnv ee,
            Object/*[]*/ args, String cause) {
        RtCausality.doMessageWithCause((RtTether) this, seal, ee,
            (Object[]) args, cause);
    }

    public final long getIdentity() {
        if (identity == 0L) {
            identity = NetIdentityMaker.nextIdentity();
        }
        return identity;
    }

    protected Object clone()
            throws CloneNotSupportedException {
        EObject_$_Impl other = (EObject_$_Impl) super.clone();
        other.identity = 0L;
        return (other);
    }

    private Object value() {
        throw new RtRuntimeException(
            "Value method unimplemented for: " + this);
    }

    public boolean encodeMeForDeflector() {
        return false;
    }   

    /** tether invoke--queues up an E message. */
    public void invoke(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        RtEnqueue.enq(this, sealer, ee, args);
    }

    private void debugExceptionInDelivery(Throwable e) {
        // XXX change to debugReportException when we're tired of seeing these
        RtRun.tr.errorReportException(e, "Translating Java throw of " +
            e.getClass() + " into ethrow");
        if (RtCausality.TheOne.myCausalityTracing) {
            String causalityTrace = RtCausality.getCausalityTraceString();
            RtRun.tr.errorm("Causality trace:\n" + causalityTrace);
        }
    }

    /** tether invokeNow--calls the synchronous invoke method on the sealer,
     * which should call back in to the impl. */
    public void invokeNow(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        RtRun.CurrentExceptionEnv = ee;
        try {
            sealer.invoke (this, args);
        } catch (Exception e) {
            if (RtCausality.TheOne.myCausalityTracing) {
                RtCausality.causalityEnhancementForException(e);
            }
            // BUG--guard this with "if (RtRun.tr.tracing)" when universal
            // spam has served its purpose; ditto for the other cases
            // below.
            debugExceptionInDelivery(e);
            RtExceptionEnv.sendException(ee, e);
        } catch (Error e) {
            if (RtCausality.TheOne.myCausalityTracing) {
                RtCausality.causalityEnhancementForException(e);
            }
            debugExceptionInDelivery(e);
            RtExceptionEnv.sendException(ee, e);
            // XXX--theoretically, this should "kill" the target
        } catch (Throwable e) {
            if (RtCausality.TheOne.myCausalityTracing) {
                RtCausality.causalityEnhancementForException(e);
            }
            debugExceptionInDelivery(e);
            RtExceptionEnv.sendException(ee, e);
        }
    }
}
