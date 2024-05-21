package ec.ez.prim;
import ec.e.run.EZEnvelope;
import ec.e.run.*;

public class PromiseDistributor_$_Impl extends EObject_$_Impl
        implements PromiseDistributor_$_Intf {

    PromiseInterior inside;

   PromiseDistributor_$_Impl(PromiseInterior in) {
        inside = in;
   }

   public void forward(EObject_$_Intf target) {
        inside.forwardTo(target);
   }

   public void breakPromise(String reason) {
        inside.breakPromise(reason);
   }

   public void forward$async(EObject_$_Intf destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      EZEnvelope env = new EZEnvelope("forward", oneArg, (PromiseDistributor_$_Intf) null);
      RtRun.enqueue(this, env);
   }

    public void breakPromise$async(String reason) {
         inside.breakPromise(reason);

 //     Object oneArg[] = new Object[1];
 //     oneArg[0] = reason;
 //     EZEnvelope env = new EZEnvelope("breakPromise", oneArg, (PromiseDistributor_$_Intf) null);
 //     RtRun.enqueue(this, env);
    }
}
