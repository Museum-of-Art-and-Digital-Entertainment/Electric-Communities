package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.e.run.*;

/**
 * What a dispatch expression evaluates to.
 */
public class EZImpl_$_Impl extends EObject_$_Impl implements EZObject {

    static private final Trace tr = new Trace("ec.ez.ezvm.EZImpl_$_Impl");

    private NameTable myPov;
    private Script myScript;

    public EZImpl_$_Impl(NameTable pov, Script script) {
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

    // XXX JAY - had to remove Package private!
    /*package*/ NameTable pov() { return myPov; }


    // JAY - handle delivery of conventional RtEnvelope to this object.
    public void deliver (RtEnvelope env) {
        RtSealer theSealer = env.getSealer();

        if(theSealer == null) {
            env.deliverTo(this);
            return;
        }

        // Check if we are activating a when - if so do it directly so as not
        // to let the script sugaring system make an infinite loop out of it.
        if(theSealer == EObject_$_Sealer.sealer_$_when$ec_dot_e_dot_run_dot_EWhenClosure) {
             EWhenClosure_$_Intf runBlock = (EWhenClosure_$_Intf) env.getArg(0);
             runBlock.doclosure$async(this);
             return;
        }

        String sealerString = theSealer.toString();
        String verbName = sealerString.substring(sealerString.indexOf("<- ")+3,
                                                 sealerString.indexOf("("));
        Object args[] = env.cloneArgs();
        try {
            myScript.execute(this, verbName, args);

        } catch (Exception ex) {
            RtRun.exceptionEnv().doEThrow(ex);
        } catch (Ejection ej) {
            tr.warningReportException(ej, "top level Ejection!?!");
        }
    }
}

