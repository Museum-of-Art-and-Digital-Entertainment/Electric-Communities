package ec.ez.prim;
import ec.e.run.EZEnvelope;
import ec.e.run.*;


public class PromiseDistributor_$_Proxy extends EProxy_$_Impl
implements PromiseDistributor_$_Intf
{
  public void forward$async(EObject_$_Intf destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      EZEnvelope env = new EZEnvelope("forward", oneArg, (PromiseDistributor_$_Intf) null);
      RtRun.enqueue(this, env);
  };

    public void breakPromise$async(String reason) {
      Object oneArg[] = new Object[1];
      oneArg[0] = reason;
      EZEnvelope env = new EZEnvelope("breakPromise", oneArg, (PromiseDistributor_$_Intf) null);
      RtRun.enqueue(this, env);
    }
}
