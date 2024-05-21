package ec.ez.ezvm;
import ec.ez.prim.ScriptMaker;
import java.io.PrintStream;
import ec.ez.runtime.Script;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.e.run.*;

public class EZUniversal {

    // XXX JAY -- Don't let MarkM catch me doing this!
    static public PrintStream ezPrinter = System.err;

    static public Object perform(Object rec, String verb, Object[] args)
         throws Exception, Ejection {

        if (rec instanceof EZObject) {
            return ((EZObject)rec).perform(verb, args);
        } else {
            Class recClass = rec.getClass();
            Script script = ScriptMaker.theOne().instanceScript(recClass);
            return script.execute(rec, verb, args);
        }
    }

    static public Object print(Object rec) {
        EZUniversal.ezPrinter.print(rec.toString());
        return rec;
    }

    static public Object println(Object rec) {
        EZUniversal.ezPrinter.println(rec.toString());
        return rec;
    }

    // capture a dispatch expression for later execution
    // when the target object is forwarded to a receiver
    static public Object respond(Object rec, Object closure) {
         EWhenClosure_$_Impl theClosure = RespondActivator.makeActivator((EZImpl) closure);
         ((EObject_$_Intf) rec).when$async(theClosure);
         EZUniversal.ezPrinter.println("Enqueued:" + theClosure + " for: " + rec);
         return rec;
    }
}
