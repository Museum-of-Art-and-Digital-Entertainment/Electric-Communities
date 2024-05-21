package ec.ez.prim;
import ec.e.run.EZEnvelope;
import ec.e.run.EZServiceEnvelope;

import ec.e.run.*;
import ec.e.lang.EString;

public class Promise_$_Impl extends EObject_$_Impl implements EChannel_$_Intf {
    PromiseInterior myInside = null; // who to forward messages to
    EDistributor_$_Intf myDistributor;

    public static final String promise_pending = "pending";
    public static final String promise_kept = "kept";
    public static final String promise_broken = "broken";
    public static final Object[] EMPTY_ARGS = new Object[0];
    public Promise_$_Impl() {
        myInside = new PromiseInterior(this);
        myDistributor = new PromiseDistributor_$_Impl(myInside);
    }

    public void deliver (RtEnvelope envelope) {
        // Check to see if this envelope is intended
        // for the channel rather than its destination
        if(envelope instanceof EZServiceEnvelope) {
            envelope.deliverTo(this);
        } else {
           myInside.acceptEnvelope(envelope, RtRun.exceptionEnv());
        }
    }

    public EDistributor_$_Intf distributor ()
    {
        EDistributor_$_Intf result = myDistributor;
        myDistributor = null;
        return (result);
    }

    public Object reasonCode() {
      return myInside.reason();
    }

    public Object stateCode() {
      return myInside.state();
    }

    public Promise_$_Impl reason() {
        Object nullArgs = new Object[0];
        Promise_$_Impl replyPromise = new Promise_$_Impl();
        EZServiceEnvelope env = new EZServiceEnvelope
        ("reasonCode", EMPTY_ARGS,
         (PromiseDistributor_$_Intf) replyPromise.distributor());
        RtRun.enqueue(this, env);
        return replyPromise;
    }

    public Promise_$_Impl state() {
        Object nullArgs = new Object[0];
        Promise_$_Impl replyPromise = new Promise_$_Impl();
        EZServiceEnvelope env = new EZServiceEnvelope
        ("stateCode", EMPTY_ARGS,
         (PromiseDistributor_$_Intf) replyPromise.distributor());
        RtRun.enqueue(this, env);
        return replyPromise;
    }
}
