package ec.ez.prim;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import java.io.PrintStream;
import ec.e.run.*;

public class EZUniversal {

    /**
     * Only necessary because ecomp doesn't yet understand '.class'
     */
    static public final Class TYPE = new EZUniversal().getClass();
    private EZUniversal() {}

    static private final Trace tr = new Trace("ec.ez.prim.EZUniversal");

    static public Object perform(Object rec, String verb, Object[] args)
         throws Exception, Ejection
    {
        if (rec instanceof EZObject) {
            return ((EZObject)rec).perform(verb, args);
        } else {
            Class recClass = rec.getClass();
            Script script = ScriptMaker.theOne().instanceScript(recClass);
            return script.execute(rec, verb, args);
        }
    }

    /**
     * capture a dispatch expression for later execution
     * when the target object is forwarded to a receiver
     */
    static public Object respond(Object rec, Object pseudoWhen) {
        EResult theClosure
            = RespondActivator.makeActivator(pseudoWhen);
        ((EObject) rec) <- when(theClosure);
        if (tr.verbose && Trace.ON) {
            tr.verbosem("Enqueued: " + theClosure + " for: " + rec);
        }
        return rec;
    }

    /**
     * activate an "ewhen" closure, binding the value argument
     * to this object.
     *
     */
    static public Object when(Object rec, Object pseudoWhen) {
        EResult theClosure
            = RespondActivator.makeActivator(pseudoWhen);
        ((EObject) rec) <- when(theClosure);
        if (tr.verbose && Trace.ON) {
            tr.verbosem("Enqueued: " + theClosure + " for: " + rec);
        }
        return rec;
    }
}
