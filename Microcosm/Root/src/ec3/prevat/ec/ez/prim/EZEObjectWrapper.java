package ec.ez.prim;
import ec.e.run.*;
import ec.ez.runtime.Ejection;

/**
 * Objects that handle EZ messages themselves.
 */
public eclass EZEObjectWrapper implements RtCodeable, EZEObjectValueInterface {

    Object myWrapped;

    public EZEObjectWrapper(Object wrappee) {
        myWrapped = wrappee;
    }

    local Object value() {
        return myWrapped;
    }

    public void encode (RtEncoder coder) {
        try  {
            coder.encodeObject (myWrapped);

        } catch (Exception e) {
            System.err.println ("Couldn't encode EZEObjectWrapper!");
            e.printStackTrace ();
        }
    }

    public  Object decode (RtDecoder coder) {
        try {
            myWrapped = coder.decodeObject();

        } catch (Exception e) {
            System.err.println ("Couldn't decode EZEObjectWrapper!");
            e.printStackTrace ();
            return (null);
        }
        return (this);
    }

    public String classNameToEncode(RtEncoder theCoder) {
        return "ec.ez.prim.EZEObjectWrapper";
    }

/*
    public void deliver (RtEnvelope env) {
        if(env instanceof EZEnvelope) {
            super.deliver(env);
        } else {
            RtSealer theSealer = env.getSealer();
            if(theSealer == sealer (EObject <- when(EResult))) {
                EResult runBlock = (EResult) env.getArg(0);
                runBlock <- forward(myWrapped);
                return;
            }

            String methodName = theSealer.getMethodName();
            String verbName = methodName.substring(0, methodName.indexOf("$"));
            Object args[] = env.cloneArgs();

            try {
                EZUniversal.perform(myWrapped, verbName, args);

            } catch (Exception ex) {
                RtRun.exceptionEnv().doEThrow(ex);
                // ex.printStackTrace();
                // XXX Jay - throw an E-Exception soon!
            } catch (Ejection ej) {
                // just eat any ejections.
            }
        }
    }
*/

}

