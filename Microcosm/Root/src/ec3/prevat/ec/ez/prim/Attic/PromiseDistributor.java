package ec.ez.prim;
import ec.e.run.*;

public class PromiseDistributor {
    
}

public interface PromiseDistributor_$_Intf extends EDistributor_$_Intf, EResult
{
    public void breakPromise$async(String reason);
}

public class PromiseDistributor_$_Impl extends EObject_$_Impl
        implements PromiseDistributor_$_Intf {

   PromiseInterior inside;

   PromiseDistributor_$_Impl(PromiseInterior in) {
        inside = in;
   }

   public void forward(Object target) {
        inside.forwardTo((EObject_$_Intf) target);
   }

   public void forward(EObject_$_Intf target) {
        inside.forwardTo(target);
   }

   public void forwardException$async(Throwable tossee) {
  //      inside.forwardTo(target);
   }

   public void breakPromise(String reason) {
        inside.breakPromise(reason);
   }

   public void forward$async(Object destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      RtEnvelope env = EZSealer.makeEnvelope("forward", oneArg);
      RtRun.enqueue(this, env);
   }

   public void forward$async(EObject_$_Intf destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      RtEnvelope env = EZSealer.makeEnvelope("forward", oneArg);
      RtRun.enqueue(this, env);
   }

    public void breakPromise$async(String reason) {
         inside.breakPromise(reason);

 //     Object oneArg[] = new Object[1];
 //     oneArg[0] = reason;
 //     EZEnvelope env = EZSealer.makeEnvelope("breakPromise", oneArg);
 //     RtRun.enqueue(this, env);
    }
}


public class PromiseDistributor_$_Proxy extends EProxy_$_Impl
implements PromiseDistributor_$_Intf
{
  public void forward$async(EObject_$_Intf destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      RtEnvelope env = EZSealer.makeEnvelope("forward", oneArg);
      RtRun.enqueue(this, env);
  };

  public void forward$async(Object destination) {
      Object oneArg[] = new Object[1];
      oneArg[0] = destination;
      RtEnvelope env = EZSealer.makeEnvelope("forward", oneArg);
      RtRun.enqueue(this, env);
  };

    public void breakPromise$async(String reason) {
      Object oneArg[] = new Object[1];
      oneArg[0] = reason;
      RtEnvelope env = EZSealer.makeEnvelope("breakPromise", oneArg);
      RtRun.enqueue(this, env);
    }

   public void forwardException$async(Throwable tossee) {
  //      inside.forwardTo(target);
   }

}
