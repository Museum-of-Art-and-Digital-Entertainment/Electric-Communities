
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check handling of Trace Log changes.  See =README=
 */

// Can't extend TraceConstants, which I don't want to make public.
class LogTests 
{
    static final int SMALLEST_LOG_SIZE_THRESHOLD = 1000;
    // 75 is a guess at number of characters per test message.
    // 300 is the guess for the amount of spam that's outside of the
    // messages.
    static final int SMALLEST_MESSAGES =
       (SMALLEST_LOG_SIZE_THRESHOLD - 300) / 75; 
    
    static final int STARTING_LOG_SIZE_THRESHOLD = 500000;
    static final int DEFAULT_MESSAGES = 
       STARTING_LOG_SIZE_THRESHOLD / 75; 
    

    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_tag", "LogTests");
        TraceController.start(props);

        System.out.println("Using these values for smallest and default: ");
        System.out.println(SMALLEST_LOG_SIZE_THRESHOLD + " & " + 
                           STARTING_LOG_SIZE_THRESHOLD);
        System.out.println("");

        Trace.trace.worldm("Expect error about Log size arg being too small.");
        TraceController.setProperty("TraceLog_size",
            String.valueOf(new Long(SMALLEST_LOG_SIZE_THRESHOLD - 1)));

        Trace.trace.worldm("Expect error about Log size arg.");
        TraceController.setProperty("TraceLog_size", "-1");

        TraceController.setProperty("TraceLog_size",
            String.valueOf(new Long(SMALLEST_LOG_SIZE_THRESHOLD)));

        System.out.println("<log>.0.txt should contain ~" +
            SMALLEST_MESSAGES + " messages.");
        System.out.println("There should be at least one new log file.");
        for (int i = 0; i < SMALLEST_MESSAGES; i++) {
            Trace.trace.worldm("subtest 1: " + i);
        }

        System.out.println("Switching to new logfile.");
        System.out.println("Checking switch back to default.");
        System.out.println("Expect one backup logfile.");
        TraceController.setProperty("Tracelog_reopen", "true");
        TraceController.setProperty("TraceLog_size", "default");
        for (int i = 0; i < DEFAULT_MESSAGES ; i++) {
            Trace.trace.worldm("subtest 2: " + i);
        }


        System.out.println("Switching to new logfile.");
        System.out.println("Checking switch to unlimited.");
        System.out.println("Expect no backup logfile.");
        TraceController.setProperty("Tracelog_reopen", "true");
        TraceController.setProperty("TraceLog_size", "unlimited");

        for (int i = 0; i < DEFAULT_MESSAGES * 2; i++) {
            Trace.trace.worldm("subtest 3: " + i);
        }

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

