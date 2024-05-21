package ec.ez.ezvm;
import ec.e.run.EZEnvelope;
import ec.e.run.EZServiceEnvelope;
import ec.e.run.*;

public class Promise_$_Proxy extends EChannel_$_Proxy {

    public Promise_$_Impl reason() {
        Object nullArgs = new Object[0];
        Promise_$_Impl replyPromise = new Promise_$_Impl();
        EZServiceEnvelope env = new EZServiceEnvelope("reasonCode", Promise_$_Impl.EMPTY_ARGS, (PromiseDistributor_$_Intf) replyPromise.distributor());
        RtRun.enqueue(this, env);
        return replyPromise;
    }

    public Promise_$_Impl state() {
        Object nullArgs = new Object[0];
        Promise_$_Impl replyPromise = new Promise_$_Impl();
        EZServiceEnvelope env = new EZServiceEnvelope("stateCode", Promise_$_Impl.EMPTY_ARGS, (PromiseDistributor_$_Intf) replyPromise.distributor());
        RtRun.enqueue(this, env);
        return replyPromise;
    }
}
