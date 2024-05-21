
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check handling of Trace buffer dumping.  See =README=
 */
class Dump
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "-");
        TraceController.setProperty("TraceBuffer_size", "10");
        TraceController.start(props);

        System.out.println("Expect <= 10 lines to stdout.");
        TraceController.setProperty("TraceBuffer_dump", "-");

        String dest;
        dest = "c:\\temp\\dump.txt";
        System.out.println("Expect <= 10 lines to " + dest);
        TraceController.setProperty("TraceBuffer_dump", dest);

        dest = "unwrite.txt";
        System.out.println("Expect unwriteable " + dest);
        TraceController.setProperty("TraceBuffer_dump", dest);

        dest = "\\";
        System.out.println("Expect directory " + dest);
        TraceController.setProperty("TraceBuffer_dump", dest);

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

