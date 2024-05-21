package ec.ez.prim;
import ec.e.run.*;
import ec.ez.runtime.Ejection;
import ec.ez.collect.NotFoundException;
import ec.ez.prim.EZEObjectValueInterface;

public class EZSealer extends RtSealer {
    public EZSealer(String name) {
        super(0, name);
     //   myToString = name;
    }

  public static RtEnvelope makeEnvelope(String verb, Object[] args, RtExceptionEnv exEnv) {
        EZSealer theSealer = new EZSealer( verb + "$async()V" );
        return new RtEnvelope(theSealer, args, exEnv);
  }

  public static RtEnvelope makeEnvelope(String verb, Object[] args) {
        EZSealer theSealer = new EZSealer( verb + "$async()V" );
        return new RtEnvelope(theSealer, args, null);
  }

  public void invoke(Object target, Object[] args) throws Exception {
   EUniDistributor_$_Intf replyDistributor = null;
   RtExceptionEnv myExcept = RtRun.exceptionEnv();

  if(myExcept != null) {
        EResult_$_Intf result = myExcept.getInternalDestination();
        if(result instanceof EUniDistributor_$_Intf) {
            replyDistributor = (EUniDistributor_$_Intf) result;
        }
  }
  Object res = null;
  try {
   Object unwrappedTarget;
   if(target instanceof EZEObjectValueInterface) {
       unwrappedTarget = ((EZEObjectValueInterface) target).value();
     }
     else {
       unwrappedTarget = target;
     }
    String methodName = getMethodName();
    String verbName = methodName.substring(0, methodName.indexOf("$"));

   try { // Set things up to intercept ethrows so we can convert them to broken
         // promises.
         EZPromiseBreaker breaker = new EZPromiseBreaker(replyDistributor);
         RtRun.pushExceptionEnv(breaker);
         try {
            res = EZUniversal.perform(unwrappedTarget, verbName, args);
            if(replyDistributor != null) {
              if(res instanceof EObject_$_Intf) {
                replyDistributor.forward$async((EObject_$_Intf) res);
              } else {
                EZEObjectWrapper_$_Impl catfud = new EZEObjectWrapper_$_Impl(res);
                replyDistributor.forward$async(catfud);
              } 
            } // res not null
         } catch (Exception e) { // was NotFoundException
            if(replyDistributor != null) {
               Object myArgs1[] = new Object[args.length + 1];
               System.arraycopy(args, 0, myArgs1, 0, args.length);
               myArgs1[args.length] = replyDistributor;

               EZUniversal.perform(unwrappedTarget, verbName, myArgs1);
            } else {
               throw e;
            }
         }
   } catch (Ejection ej) {}
      RtRun.popExceptionEnv();
  }
  catch (Exception ex) { // Exception thrown, break the promise
    String report;
    if(ex instanceof NotFoundException) {
          report = "Verb not found: " + ex.getMessage();
      } else {
          report = ex.getMessage();
      }

    if(replyDistributor != null) {
       //  replyDistributor.breakPromise$async(report);
    }
   }
  }      
}
