package ec.ez.ezvm;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.ez.prim.EZSealer;
import ec.e.run.*;
import ec.e.run.EObject_$_Impl;
/**
 * What a dispatch expression evaluates to.
 */
public eclass EZImpl implements EZObject, RtUniquelyCodeable {

    static private final Trace tr = new Trace("ec.ez.ezvm.EZImpl_$_Impl");

    private NameTable myPov;
    private Script myScript;

    public EZImpl(NameTable pov, Script script) {
        myPov = pov;
        myScript = script;
    }

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection {
        return myScript.execute(this, verb, args);
    }

    public Object value() {
        return this;
    }

    /**
     * All security rests on not making this more visible than "package"
     */
    /*package*/ NameTable pov() { return myPov; }


    // JAY - handle delivery of conventional RtEnvelope to this object.
    public void invokeNow(RtSealer theSealer,
                          Object[] args,
                          RtExceptionEnv ee) {

        // Check if we are activating a when - if so do it directly so
        // as not to let the script sugaring system make an infinite
        // loop out of it.
        if(theSealer == sealer (EObject <- when(EResult))) {
            EResult runBlock = (EResult) args[0];
            runBlock <- forward (this);
            return;
        }

        String verbName = theSealer.getMethodName();

        try {
            myScript.execute(this, verbName, args);
        } catch (Exception ex) {
            RtExceptionEnv.sendException(ee, ex);
        } catch (Ejection ej) {
            tr.warningReportException(ej, "top level Ejection!?!");
        }
    }

    public String classNameToEncode(RtEncoder theCoder) {
        return "ec.ez.ezvm.EZImplProxy_$_Impl";
    };
}



/**
 * XXX Is this a proxy to a remote EZImpl?  --MarkM
 */
public eclass EZImplProxy implements EZObject, RtUniquelyCodeable {

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection {

        EUniChannel replyChannel = new EUniChannel();
        EUniDistributor distrib 
            = (EUniDistributor)replyChannel.getDistributor();

        RtExceptionEnv exEnv = new RtExceptionEnv(distrib, null);

        RtEnvelope env = EZSealer.makeEnvelope(verb, args, exEnv);
        RtRun.enqueue((EObject_$_Intf) this, env);
        return replyChannel;
    }
}
