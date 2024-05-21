
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check that no properties behaves as expected.  See =README=
 */
class Default
{
    public static void main (String[] args) {
        Properties props = new Properties();
        TraceController.start(props);
        System.out.println("////////// Check the log in its default location.");
        Trace.comm.errorm("Here is a trace message");
        Trace.comm.warningm("Here is a trace message");
        Trace.comm.worldm("Here is a trace message");
        Trace.comm.usagem("Here is a trace message");
        Trace.comm.eventm("Here is a trace message");
        Trace.comm.debugm("Here is a trace message");
        Trace.comm.verbosem("Here is a trace message");
        System.out.println("////////// Check buffer.dump.");
        TraceController.setProperty("TraceBuffer_dump", "buffer.dump");
        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

