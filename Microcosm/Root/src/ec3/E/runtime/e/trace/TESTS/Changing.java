
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;
import ec.util.assertion.*;


/**
 * Effect of changing priority level thresholds.  See =README=
 */
class Changing
{
    public static void main (String[] args) {
        (new Changing()).go();
    }

    private void go() {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "changing.txt");
        props.put("TraceLog_default", "world");
        props.put("TraceBuffer_default", "world");
        props.put("TraceBuffer_size", "unlimited");
        props.put("TraceLog_trace", "world");
        props.put("TraceBuffer_trace", "world");
        TraceController.start(props);

        // Three subsystems, with two trace objects each.
        Trace subsystem1a;
        Trace subsystem1b;
        Trace subsystem2a;
        Trace subsystem2b;
        Trace subsystem3a;
        Trace subsystem3b;


        // Set levels for one of them before it's created.
        TraceController.setProperty("TraceLog_subsystem1", "debug");

        // Create them all.
        subsystem1a = new Trace("subsystem1");
        subsystem1b = new Trace("subsystem1");
        subsystem2a = new Trace("subsystem2");
        subsystem2b = new Trace("subsystem2");
        subsystem3a = new Trace("subsystem3");
        subsystem3b = new Trace("subsystem3");

        TraceController.setProperty("TraceLog_subsystem2", "error");
        // Subsystem 3 tracks the default.

        //      Subsystem 1             2               3
        //Log           debug           error           world / def
        //Buffer        world / def     world / def     world / def

        Trace.trace.worldm("+++++++++++++++++++++++++ SUBTEST 1 ++++++++");
        FieldChecker.debugOn(subsystem1a);
        FieldChecker.debugOn(subsystem1b);
        FieldChecker.worldOn(subsystem2a);  
        FieldChecker.worldOn(subsystem2b);
        FieldChecker.worldOn(subsystem3a);
        FieldChecker.worldOn(subsystem3b);

        Trace.trace.worldm("Expect subsystem1 to LOG debug level messages.");
        Trace.trace.worldm("Expect subsystem1 to BUFFER world level messages.");
        logall(subsystem1a, subsystem1b);

        Trace.trace.worldm("Expect subsystem2 to LOG error level messages.");
        Trace.trace.worldm("Expect subsystem2 to BUFFER world level messages.");
        logall(subsystem2a, subsystem2b);

        Trace.trace.worldm("Expect subsystem3 to LOG world level messages.");
        Trace.trace.worldm("Expect subsystem3 to BUFFER world level messages.");
        logall(subsystem3a, subsystem3b);

        Trace.trace.worldm("+++++++++++++++++++++++++ SUBTEST 2 ++++++++");
        TraceController.setProperty("TraceLog_trace", "debug");
        Trace.trace.worldm("Making change to subsystem2 that does " +
            "not affect cached values.  You should see no debug " +
            "messages about changing them.");
        TraceController.setProperty("TraceLog_subsystem2", "world");

        // move it back up.  
        Trace.trace.worldm("Making change to subsystem2 that does " +
            "not affect cached values.  You should see no debug " +
            "messages about changing them.");
        TraceController.setProperty("TraceLog_subsystem2", "error");

        TraceController.setProperty("TraceLog_trace", "world");


        Trace.trace.worldm("+++++++++++++++++++++++++ SUBTEST 3 ++++++++");

        TraceController.setProperty("TraceLog_default", "usage");

        //      Subsystem 1             2               3
        //Log           debug           error           usage / def
        //Buffer        world / def     world / def     world / def

        FieldChecker.debugOn(subsystem1a);
        FieldChecker.debugOn(subsystem1b);
        FieldChecker.worldOn(subsystem2a);  
        FieldChecker.worldOn(subsystem2b);
        FieldChecker.usageOn(subsystem3a);
        FieldChecker.usageOn(subsystem3b);

        Trace.trace.worldm("Expect subsystem1 to LOG debug level messages.");
        Trace.trace.worldm("Expect subsystem1 to BUFFER world level messages.");
        logall(subsystem1a, subsystem1b);

        Trace.trace.worldm("Expect subsystem2 to LOG error level messages.");
        Trace.trace.worldm("Expect subsystem2 to BUFFER world level messages.");
        logall(subsystem2a, subsystem2b);

        Trace.trace.worldm("Expect subsystem3 to LOG usage level messages.");
        Trace.trace.worldm("Expect subsystem3 to BUFFER world level messages.");
        logall(subsystem3a, subsystem3b);


        Trace.trace.worldm("+++++++++++++++++++++++++ SUBTEST 4 ++++++++");
        TraceController.setProperty("TraceLog_subsystem2", "default");
        TraceController.setProperty("TraceLog_subsystem3", "default");

        //      Subsystem 1             2               3
        //Log           debug           usage / def     usage / def
        //Buffer        world / def     world / def     world / def

        FieldChecker.debugOn(subsystem1a);
        FieldChecker.debugOn(subsystem1b);
        FieldChecker.usageOn(subsystem2a);  
        FieldChecker.usageOn(subsystem2b);
        FieldChecker.usageOn(subsystem3a);
        FieldChecker.usageOn(subsystem3b);

        Trace.trace.worldm("Expect subsystem1 to LOG debug level messages.");
        Trace.trace.worldm("Expect subsystem1 to BUFFER world level messages.");
        logall(subsystem1a, subsystem1b);

        Trace.trace.worldm("Expect subsystem2 to LOG usage level messages.");
        Trace.trace.worldm("Expect subsystem2 to BUFFER world level messages.");
        logall(subsystem2a, subsystem2b);

        Trace.trace.worldm("Expect subsystem3 to LOG usage level messages.");
        Trace.trace.worldm("Expect subsystem3 to BUFFER world level messages.");
        logall(subsystem3a, subsystem3b);

        

        Trace.trace.worldm("+++++++++++++++++++++++++ SUBTEST 5 ++++++++");
        TraceController.setProperty("TraceLog_subsystem1", "warning");
        TraceController.setProperty("TraceBuffer_subsystem2", "error");
        TraceController.setProperty("TraceBuffer_subsystem3", "verbose");

        //      Subsystem 1             2               3
        //Log           warning         usage / def     usage / def
        //Buffer        world / def     error           verbose

        FieldChecker.worldOn(subsystem1a);
        FieldChecker.worldOn(subsystem1b);
        FieldChecker.usageOn(subsystem2a);  
        FieldChecker.usageOn(subsystem2b);
        FieldChecker.verboseOn(subsystem3a);
        FieldChecker.verboseOn(subsystem3b);

        Trace.trace.worldm("Expect subsystem1 to LOG warning level messages.");
        Trace.trace.worldm("Expect subsystem1 to BUFFER world level messages.");
        logall(subsystem1a, subsystem1b);

        Trace.trace.worldm("Expect subsystem2 to LOG usage level messages.");
        Trace.trace.worldm("Expect subsystem2 to BUFFER error level messages.");
        logall(subsystem2a, subsystem2b);

        Trace.trace.worldm("Expect subsystem3 to LOG usage level messages.");
        Trace.trace.worldm("Expect subsystem3 to BUFFER verbose level messages.");
        logall(subsystem3a, subsystem3b);

        



        TraceController.setProperty("TraceBuffer_dump", "changingdump.txt");
    }

    // Alternate between supposedly equivalent trace objects.
    private void logall(Trace tr1, Trace tr2) { 
        tr1.errorm("...");
        tr2.warningm("...");
        tr1.worldm("...");
        tr2.usagem("...");
        tr1.eventm("...");
        tr2.debugm("...");
        tr1.verbosem("...");
    }
}

