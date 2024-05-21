package ec.ez.prim;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.e.run.*;

/**
 * What a dispatch expression evaluates to.
 */
public class EZImpl_$_Impl extends EObject_$_Impl implements EZObject {

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
    public NameTable pov() { return myPov; }


    // JAY - handle delivery of conventional RtEnvelope to this object.
    public void deliver (RtEnvelope env) {
        RtSealer theSealer = env.getSealer();
        String sealerString = theSealer.toString();
        String verbName = sealerString.substring(sealerString.indexOf("<- ") + 3, sealerString.indexOf("("));
        Object args[] = env.cloneArgs();
        try {
            try {
                myScript.execute(this, verbName, args);
            }
            catch (Exception ex) {
                RtRun.exceptionEnv().doEThrow(ex);
            }
        } catch (Ejection ej) { // just eat any ejections.
        }
    }
}

