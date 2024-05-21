package ec.e.run;
import ec.ez.prim.EZEObjectWrapper;
import ec.ez.prim.PromiseDistributor_$_Intf;
import ec.ez.prim.EZUniversal;
import ec.ez.prim.EZPromiseBreaker;
import ec.ez.runtime.Ejection;
import ec.ez.collect.NotFoundException;

public class EZEnvelope extends RtEnvelope {
    PromiseDistributor_$_Intf replyDistributor;
    String myVerb;

    EZEnvelope(RtSealer sealer, Object[] args) {
        super(sealer, args, null);
    }

    public EZEnvelope(String theVerb, Object[] args, PromiseDistributor_$_Intf replyTo) {
        super((RtSealer) null, args, null);
        myVerb = theVerb;
        replyDistributor = replyTo;
    }

    public void deliverTo (Object target)
    {
        Object res = null;
        try {
            // If the sealer is known...
            if(mySealer != null) {
                mySealer.invoke (target, myArgs);
            } else {
                Object unwrappedTarget;
                if(target instanceof EZEObjectWrapper) {
                    unwrappedTarget = ((EZEObjectWrapper) target).value();
                  }
                  else {
                    unwrappedTarget = target;
                  }
                try { // Set things up to intercept ethrows so we can convert them to broken
                      // promises.
                      EZPromiseBreaker breaker = new EZPromiseBreaker(replyDistributor);
                      RtRun.pushExceptionEnv(breaker);
                      try {
                         res = EZUniversal.perform(unwrappedTarget, myVerb, myArgs);
                         if(replyDistributor != null) {
                           if(res instanceof EObject_$_Intf) {
                            ((EDistributor_$_Intf) replyDistributor).forward$async((EObject_$_Intf) res);
                           } else {
                            ((EDistributor_$_Intf) replyDistributor).forward$async(new EZEObjectWrapper (res));
                           }
                         } // res not null
                      } catch (Exception e) { // was NotFoundException
                         if(replyDistributor != null) {
                            Object myArgs1[] = new Object[myArgs.length + 1];
                            System.arraycopy(myArgs, 0, myArgs1, 0, myArgs.length);
                            myArgs1[myArgs.length] = replyDistributor;

                            EZUniversal.perform(unwrappedTarget, myVerb, myArgs1);
                         } else {
                            throw e;
                         }
                      }
                } catch (Ejection ej) {}
                RtRun.popExceptionEnv();
            }
        }
         catch (Exception ex) { // Exception thrown, break the promise
            String report;
            if(ex instanceof NotFoundException) {
                report = "Verb not found: " + ex.getMessage();
            } else {
                report = ex.getMessage();
            }

            if(replyDistributor != null) {
                replyDistributor.breakPromise$async(report);
            }
        }
      }


    public void encode (RtEncoder coder) {
        try  {
           coder.encodeObject (myVerb);
           coder.encodeObject (mySealer);
           coder.encodeObject (myArgs);
           coder.encodeObject (replyDistributor);
           }
           catch (Exception e)         {
               System.err.println ("Couldn't encode envelope!");
                e.printStackTrace ();
           }
         }

    public  Object decode (RtDecoder coder)
    {
        try
        {
            myVerb = (String) coder.decodeObject();
            mySealer = (RtSealer) coder.decodeObject ();
            myArgs = (Object[]) coder.decodeObject ();
            replyDistributor = (PromiseDistributor_$_Intf) coder.decodeObject();
        }
        catch (Exception e)
        {
            System.err.println ("Couldn't decode envelope!");
            e.printStackTrace ();
            return (null);
        }
        return (this);
    }


    public String toString()
    {
        String result =
            "#<" + getClass().getName() + " " + myVerb + " (";
        if (myArgs != null)
        {
            for (int i = 0; i < myArgs.length; i++)
            {
                if (i != 0)
                {
                    result += ", ";
                }
                try {
                    result += myArgs[i];
                } catch (RuntimeException e) {
                    result += "<unprintable " + myArgs[i].getClass() + ">";
                }
            }
        }
        result += ")>";

        return (result);
    }
}