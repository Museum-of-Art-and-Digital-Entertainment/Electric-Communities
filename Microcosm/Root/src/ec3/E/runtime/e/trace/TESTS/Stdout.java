
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Standard output cannot fill up.
 */

class Stdout
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_size", "1000");
        props.put("TraceLog_trace", "usage");
        props.put("TraceLog_name", "-");
        TraceController.start(props);

        Trace.trace.worldm("Expect log never to be full.");
        for (int i = 0; i < 2000; i++) {
            Trace.trace.worldm("subtest 1: " + i);
        }

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

