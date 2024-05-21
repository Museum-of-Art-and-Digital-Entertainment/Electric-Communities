package ec.ez.prim;

import ec.e.run.*;
import ec.ez.runtime.Ejection;
import ec.util.NestedException;

public class RespondActivator extends InternalEWhenClosure {

    static private final Trace tr = new Trace("ec.ez.prim.RespondActivator");

    private Object myPseudoWhen;

    public static EResult makeActivator(Object pseudoWhen) {
        RespondActivator theActivator = new RespondActivator(pseudoWhen);
        return new EWhenClosure(theActivator);
    }

    private RespondActivator(Object pseudoWhen) {
        myPseudoWhen = pseudoWhen;
    }

    public void doit(Object value) {    // was doit
        Object args[] = { value };
        if (use()) {
            try {
                EZUniversal.perform(myPseudoWhen, "run", args);

            } catch (Ejection ej) {
                tr.warningReportException(ej, "Ejection ignored");
            } catch (RuntimeException rex) {
                throw rex;
            } catch (Exception ex) {
                throw new NestedException("from pseudo-when closure", ex);
            }
        }
    }
}

