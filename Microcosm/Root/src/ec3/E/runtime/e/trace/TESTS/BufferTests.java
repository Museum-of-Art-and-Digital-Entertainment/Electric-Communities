
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check handling of Trace buffer changes.  See =README=
 */
class BufferTests
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "false");
        props.put("TraceLog_trace", "debug");
        TraceController.start(props);

        Trace.trace.worldm("Expect error about buffer size arg.");
        TraceController.setProperty("TraceBuffer_size", "0");

        Trace.trace.worldm("Expect error about buffer size arg.");
        TraceController.setProperty("TraceBuffer_size", "-1");

        System.out.println("buffertests.dump.1.txt should contain a single line.");
        System.out.println("It should be a line about the trace dump.");
        TraceController.setProperty("TraceBuffer_size", "1");
        Trace.trace.worldm("This line will NOT BE SEEN.");
        Trace.trace.worldm("NOR WILL THIS.");
        Trace.trace.worldm("NOR WILL EVEN THIS.");
        TraceController.setProperty("TraceBuffer_dump",
            "buffertests.dump.1.txt");


        System.out.println("buffertests.dump.2.txt should contain the default number of lines.");
        System.out.println("The count should start at a number > 1.");
        TraceController.setProperty("TraceBuffer_size", "default");
        for (int i = 0; i < 1000; i++) {
            Trace.trace.worldm("subtest 2: " + i);
        }
        TraceController.setProperty("TraceBuffer_dump",
            "buffertests.dump.2.txt");


        System.out.println("buffertests.dump.3.txt should contain an unlimited number of lines.");
        System.out.println("The count should contain number 0.");
        TraceController.setProperty("TraceBuffer_size", "1"); // flush
        TraceController.setProperty("TraceBuffer_size", "unlimited");
        for (int i = 0; i < 5000; i++) {
            Trace.trace.worldm("subtest 3: " + i);
        }

        TraceController.setProperty("TraceBuffer_dump",
            "buffertests.dump.3.txt");


        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

