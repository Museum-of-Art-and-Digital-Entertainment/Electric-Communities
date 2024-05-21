
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check handling of user errors.  See =README=
 */
class UserError
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_trace", "usage");
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "-");
        TraceController.start(props);
        Trace.trace.worldm("Expect error about bad trace level.");
        TraceController.setProperty("TraceBuffer_default", "errorm");

        Trace.trace.worldm("Expect error about bad trace level.");
        TraceController.setProperty("TraceLog_default", "defalt");

        Trace.trace.worldm("No error to misspell tracelog.");
        TraceController.setProperty("TraceLo_default", "defalt");

        Trace.trace.worldm("No error to misspell tracebuffer.");
        TraceController.setProperty("Tracebufer_default", "defalt");

        Trace.trace.worldm("Expect error about bad write arg.");
        TraceController.setProperty("TraceLog_write", "falses");

        Trace.trace.worldm("Expect error about log size arg.");
        TraceController.setProperty("TraceLog_size", "a0");

        Trace.trace.worldm("Expect error about log size arg.");
        TraceController.setProperty("TraceLog_size", "9a");

        Trace.trace.worldm("Expect error about log size arg.");
        TraceController.setProperty("TraceLog_size", "unlimite");

        Trace.trace.worldm("Expect error about buffer size arg.");
        TraceController.setProperty("TraceBuffer_size", "0xa0");

        Trace.trace.worldm("Expect error about buffer size arg.");
        TraceController.setProperty("TraceBuffer_size", "9.0");

        Trace.trace.worldm("Expect error about buffer size arg.");
        TraceController.setProperty("TraceBuffer_size", "defaul");

        Trace.trace.worldm("Expect error about backups arg.");
        TraceController.setProperty("TraceLog_backups", "2");

        Trace.trace.worldm("Expect error about backups arg.");
        TraceController.setProperty("TraceLog_backups", "");

        Trace.trace.worldm("Expect error about backups arg.");
        TraceController.setProperty("TraceLog_backups", "mnay");

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

