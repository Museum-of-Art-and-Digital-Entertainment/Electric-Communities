package ec.ez.ezvm;
import ec.ez.runtime.Ejection;
import ec.e.run.EZEnvelope;
import ec.e.run.*;

public class PromiseDistributor extends EObject_$_Impl
        implements EDistributor_$_Intf {

    PromiseInterior inside;

    PromiseDistributor(PromiseInterior in) {
        inside = in;
    }

   public void forward(EObject_$_Intf target) {
        inside.forwardTo(target);
   }

   public void forward$async(EObject_$_Intf destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      EZEnvelope env = new EZEnvelope("forward", oneArg, (PromiseDistributor_$_Intf) null);
      RtRun.enqueue(this, env);
   }
}
