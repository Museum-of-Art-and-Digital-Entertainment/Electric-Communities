package ec.e.run;

import ec.e.net.NetIdentityMaker;

public final class EForwardingTether implements Exportable
{
    private long identity = 0L;
    private Object myKey;
    private EObject myTarget;

    private EForwardingTether() {}

    public EForwardingTether(Object key) {
        myKey = key;
    }

    public void setTarget(Object key, EObject target) {
        if (key != myKey) {
            throw new SecurityException("tried to setTarget with wrong key");
        }
        myTarget = target;
    }

    public final long getIdentity() {
        if (identity == 0L) {
            identity = NetIdentityMaker.nextIdentity();
        }
        return identity;
    }

    /** tether invoke--queues up an E message. */
    public void invoke(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        RtEnvelope e = new RtEnvelope(sealer, args, ee);
        myTarget <- e;
    }

    /** tether invokeNow--calls the synchronous invoke method on the sealer,
     * which should call back in to the impl. */
    public void invokeNow(RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        RtRun.deliverNow(myTarget, sealer, args, ee);
    }
   
}
