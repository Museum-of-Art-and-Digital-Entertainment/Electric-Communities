
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Basic smoke test.  See =README=
 */
class Simple
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_ui", "usage");
        TraceController.start(props);
        Trace.comm.worldm("Here is a trace message");
        Trace.trace.usagem("Here is a trace message you won't see.");
        Trace.ui.usagem("Here is a trace message at the USE level.");
        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

