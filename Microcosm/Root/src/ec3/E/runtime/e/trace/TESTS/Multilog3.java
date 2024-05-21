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
class Multilog3
{
    /* SEQUENCE OF EVENTS: (in a clean directory):
       ..\Test\3Log.txt is opened.
       Switch to 3aLog.txt
       Switch to ..\\Test\\3aLog.txt
            - renames above 3aLog.txt to 3aLog.0.txt.
       Switch to 3aLog.notxt
       Switch to another
       Switch to \temp\3Log.txt
       Switch to 3LOG.txt
            - renames above 3Log.txt to 3Log.0.txt.
       Switch to ""
            - error.
    */
    public static void main (String[] args) {
        System.out.println("Start by looking at 3Log.0.txt.");
        Properties props = new Properties();
        props.put("TraceLog_name","..\\Test\\3Log.txt");
        props.put("TraceLog_write", "true");
        // props.put("TraceLog_trace", "debug");
        Trace.comm.worldm("Expect log to 3Log.0.txt.");
        TraceController.start(props);
        new Multilog3().go();

        Trace.comm.worldm("That's all, folks - end of test.");
    }

    void go() {
        Trace.comm.worldm("Switching from 3Log.txt to 3aLog.txt");
        Trace.comm.worldm("Because of later renaming, look in 3aLog.0.txt");
        TraceController.setProperty("TraceLog_name", "3aLog.txt");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from 3aLog.txt to ..\\Test\\3aLog.txt");
        Trace.comm.worldm("Expect reuse of same name (w/ version #).");
        TraceController.setProperty("TraceLog_name", "..\\Test\\3aLog.txt");
        TraceController.setProperty("TraceLog_reopen", "true");

        Trace.comm.worldm("Switching from 3aLog.txt to 3aLog.notxt");
        TraceController.setProperty("TraceLog_name", "3aLog.notxt");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from 3aLog.notxt to 'another'");
        TraceController.setProperty("TraceLog_name", "another");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from 'another' to '\\temp\\3Log.txt'");
        TraceController.setProperty("TraceLog_name", "\\temp\\3Log.txt");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from '\\temp\\3Log.txt' to 3LOG.TXT");
        TraceController.setProperty("TraceLog_name", "3LOG.TXT");
        TraceController.setProperty("TraceLog_reopen", "true");
        
        Trace.comm.worldm("Switching from 3LOG.TXT to ''");
        Trace.comm.worldm("Expect error and reuse of log.");
        
        TraceController.setProperty("TraceLog_name", "");
        TraceController.setProperty("TraceLog_reopen", "true");
    }
}

