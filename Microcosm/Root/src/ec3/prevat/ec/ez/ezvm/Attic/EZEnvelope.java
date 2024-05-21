package ec.e.run;
import ec.ez.ezvm.EZEObjectWrapper;
import ec.ez.ezvm.PromiseDistributor_$_Intf;
import ec.ez.prim.EZUniversal;
import ec.ez.runtime.Ejection;
import ec.ez.collect.NotFoundException;

public class EZEnvelope extends RtEnvelope {
    PromiseDistributor_$_Intf replyDistributor;
    String myVerb;

    EZEnvelope(RtSealer sealer, Object[] args) {
        super(sealer, args);
    }

    public EZEnvelope(String theVerb, Object[] args, PromiseDistributor_$_Intf replyTo) {
        super((RtSealer) null, args);
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
            // If my target is not an EObject then stage a call
                try {
                if(target instanceof EZEObjectWrapper) {
                    Object unwrappedTarget = ((EZEObjectWrapper) target).value();
                    res = EZUniversal.perform(unwrappedTarget, myVerb, myArgs);
                    if(replyDistributor != null) {
                      if(res instanceof EObject_$_Intf) {
                        ((EDistributor_$_Intf) replyDistributor).forward$async((EObject_$_Intf) res);
                      } else {
                        ((EDistributor_$_Intf) replyDistributor).forward$async(new EZEObjectWrapper (res));
                      }
                    }
                }
                // If the target is an EObject, check to see if it has a distributor
                // as the final argument.
                else if(target instanceof EObject_$_Intf) {
                      try {
                         res = EZUniversal.perform(target, myVerb, myArgs);
                         if(replyDistributor != null) {
                           if(res instanceof EObject_$_Intf) {
                            ((EDistributor_$_Intf) replyDistributor).forward$async((EObject_$_Intf) res);
                           } else {
                            ((EDistributor_$_Intf) replyDistributor).forward$async(new EZEObjectWrapper (res));
                           }
                         } // res not null
                      } catch (NotFoundException e) {
                         if(replyDistributor != null) {
                            Object myArgs1[] = new Object[myArgs.length + 1];
                            System.arraycopy(myArgs, 0, myArgs1, 0, myArgs.length);
                            myArgs1[myArgs.length] = replyDistributor;
                            EZUniversal.perform(target, myVerb, myArgs1);
                         } else {
                            throw e;
                         }
                      }
                }
                } catch (Ejection ej) {}
            }
        } catch (Exception e) { // Exception thrown, break the promise someday soon.
            //
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
}