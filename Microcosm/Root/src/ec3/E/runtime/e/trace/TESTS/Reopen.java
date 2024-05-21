
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check various types of file reopens.  See =README=
 */

// Can't extend TraceConstants, which I don't want to make public.
class Reopen
{

    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_tag", "Reopen");
        TraceController.start(props);

        Trace.trace.worldm("TraceLog_dir set to nonexistent directory.");
        Trace.trace.worldm("Expect failure to reopen.");
        
        TraceController.setProperty("TraceLog_dir", "c:\\tempxxx");
        TraceController.setProperty("Trace_reopen", "true");

        Trace.trace.worldm("TraceLog_dir set to file.");
        Trace.trace.worldm("Expect failure to reopen.");
        
        TraceController.setProperty("TraceLog_dir", "c:\\temp\\Asfile.txt");
        TraceController.setProperty("Trace_reopen", "true");

        // Back to something reasonable
        TraceController.setProperty("TraceLog_dir", ".");

        String dest1;
        dest1 = "c:\\temp\\file.txt";
        String dest2;
        dest2 = "file.NOtxt";

        for (int i = 0; i < 4; i++) {
            Trace.trace.worldm("TraceLog_name set to " + dest1);
            TraceController.setProperty("TraceLog_name", dest1);
            TraceController.setProperty("Trace_reopen", "true");

            Trace.trace.worldm("TraceLog_name set to " + dest2);
            TraceController.setProperty("TraceLog_name", dest2);
            TraceController.setProperty("Trace_reopen", "true");
        }
        

        /** skipped the following, because can't seem to make a
         * self-unwriteable directory on Windows.
        Trace.trace.worldm("TraceLog_dir set to unwriteable file.");
        Trace.trace.worldm("Backup file also cannot be renamed.");
        Trace.trace.worldm("Expect failure to reopen.");
        
        TraceController.setProperty("TraceLog_name", "c:\\temp\\unwriteable\\unwriteable.txt");
        TraceController.setProperty("Trace_reopen", "true");

        */

        TraceController.setProperty("TraceLog_backups", "1");
        Trace.trace.worldm("Opening overwrite.txt.");
        Trace.trace.worldm("overwrite.0.txt should contain 'move to 0.txt'.");
        Trace.trace.worldm("overwrite.1.txt should contain 'ignored'.");
        
        TraceController.setProperty("TraceLog_name", "overwrite.txt");
        TraceController.setProperty("Trace_reopen", "true");

        

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

