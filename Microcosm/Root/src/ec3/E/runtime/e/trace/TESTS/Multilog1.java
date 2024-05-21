package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;


/**
 * Tests of switching log files.  There are three versions of this file.
 * Multilog1 starts with logging to a file.  Multilog2
 * starts with logging to standard output.  Multilog3 starts with
 * logging to a selected filename.
 */
class Multilog1
{
    public static void main (String[] args) {
        System.out.println("Start by looking in the dated ECLog file.");
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        // Tag needed because default is temporily "log to stdout".
        props.put("TraceLog_tag", "ECLog");
        
        TraceController.start(props);
        new Multilog1().go();

        Trace.comm.worldm("That's all, folks - end of test.");
    }

    void go() {
        Trace.comm.worldm("Switching from dated file to stdout");
        TraceController.setProperty("TraceLog_name", "-");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from stdout to stdout");
        TraceController.setProperty("TraceLog_name", "-");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from stdout to 1.txt");
        TraceController.setProperty("TraceLog_name", "1.txt");
        TraceController.setProperty("TraceLog_reopen", "true");
    }
}

