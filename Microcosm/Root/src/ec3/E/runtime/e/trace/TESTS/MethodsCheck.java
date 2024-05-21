
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * See =README=
 */
class MethodsCheck
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "-");
        props.put("TraceLog_default", "error");
        TraceController.start(props);
        Trace.trace.errorm("You should see this.");
        Trace.trace.warningm("You should NOT see this.");
        Trace.trace.worldm("You should NOT see this.");
        Trace.trace.usagem("You should NOT see this.");
        Trace.trace.eventm("You should NOT see this.");
        Trace.trace.debugm("You should NOT see this.");
        Trace.trace.verbosem("You should NOT see this.");
    }
}

