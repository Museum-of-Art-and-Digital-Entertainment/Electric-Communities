package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;


/**
 * Tests of switching log files.  There are three versions of this file.
 * Multilog1 starts with logging to the default name.  Multilog2
 * starts with logging to standard output.  Multilog3 starts with
 * logging to a selected filename.
 */
class Multilog2
{
    public static void main (String[] args) {
        System.out.println("Start by looking at standard output.");

        Properties props = new Properties();
        props.put("TraceLog_write", "true");

        props.put("TraceLog_name","-");
        Trace.comm.worldm("Expect log to stdout.");
        TraceController.start(props);
        new Multilog2().go();

        Trace.comm.worldm("That's all, folks - end of test.");
    }

    void go() {
        Trace.comm.worldm("Switching from stdout to my.log");
        TraceController.setProperty("TraceLog_name", "my.log");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from my.log to stdout");
        TraceController.setProperty("TraceLog_name", "-");
        TraceController.setProperty("TraceLog_reopen", "true");
    }
}

