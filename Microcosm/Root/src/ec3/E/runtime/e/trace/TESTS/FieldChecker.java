
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;
import ec.util.assertion.*;

/**
 * Checks which booleans are set.
 */
class FieldChecker
{
    static public void errorOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(!tr.debug && Trace.ON);
        Assertion.test(!tr.event && Trace.ON);
        Assertion.test(!tr.usage && Trace.ON);
        Assertion.test(!tr.world && Trace.ON);
        Assertion.test(!tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void warningOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(!tr.debug && Trace.ON);
        Assertion.test(!tr.event && Trace.ON);
        Assertion.test(!tr.usage && Trace.ON);
        Assertion.test(!tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void worldOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(!tr.debug && Trace.ON);
        Assertion.test(!tr.event && Trace.ON);
        Assertion.test(!tr.usage && Trace.ON);
        Assertion.test(tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void usageOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(!tr.debug && Trace.ON);
        Assertion.test(!tr.event && Trace.ON);
        Assertion.test(tr.usage && Trace.ON);
        Assertion.test(tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void eventOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(!tr.debug && Trace.ON);
        Assertion.test(tr.event && Trace.ON);
        Assertion.test(tr.usage && Trace.ON);
        Assertion.test(tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void debugOn(Trace tr) {
        Assertion.test(!tr.verbose && Trace.ON);
        Assertion.test(tr.debug && Trace.ON);
        Assertion.test(tr.event && Trace.ON);
        Assertion.test(tr.usage && Trace.ON);
        Assertion.test(tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

    static public void verboseOn(Trace tr) {
        Assertion.test(tr.verbose && Trace.ON);
        Assertion.test(tr.debug && Trace.ON);
        Assertion.test(tr.event && Trace.ON);
        Assertion.test(tr.usage && Trace.ON);
        Assertion.test(tr.world && Trace.ON);
        Assertion.test(tr.warning && Trace.ON);
        Assertion.test(tr.error);
    }

}

