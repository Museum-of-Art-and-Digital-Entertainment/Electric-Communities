package ec.ez.ezvm;
import ec.e.run.EZEnvelope;
import ec.e.run.*;

public class Promise extends EObject_$_Impl implements EChannel_$_Intf {
    PromiseInterior inside = null; // who to forward messages to
    EDistributor_$_Intf myDistributor;

    public static final int promise_pending = 0;
    public static final int promise_kept = 1;
    public static final int promise_broken = -1;

    public Promise() {
        inside = new PromiseInterior(this);
        myDistributor = new PromiseDistributor(inside);
    }

    public int state() {
        return inside.state;
    }

    public void deliver (RtEnvelope envelope) {
        inside.acceptEnvelope(envelope, RtRun.exceptionEnv());
    }

    public EDistributor_$_Intf distributor ()
    {
        EDistributor_$_Intf result = myDistributor;
        myDistributor = null;
        return (result);
    }
}
