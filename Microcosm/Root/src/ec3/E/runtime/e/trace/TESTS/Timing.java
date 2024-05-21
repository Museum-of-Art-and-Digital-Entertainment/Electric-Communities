
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Basic smoke test.  See =README=
 */
class Timing
{
    public static void main (String[] args) {
        Properties props = new Properties();
        // props.put("TraceLog_trace", "debug");
        props.put("TraceLog_ui", "debug");
        TraceController.start(props);
        TraceController.setProperty("TraceLog_ui_timing", "true");
        Trace.ui.debugm("You should see a timing message");
        if (Trace.ui.timing) Trace.ui.timingm("First timing message");
        TraceController.setProperty("TraceLog_ui_timing", "false");
        Trace.ui.debugm("You should not see 'omitted timing message'");
        if (Trace.ui.timing) Trace.ui.timingm("Omitted timing message");
        if (Trace.ui.timing) System.out.println("ERROR: CACHE OUT OF DATE");
        
        Trace tr = new Trace("ui");
        if (tr.timing) tr.timingm("Also omitted.");

        TraceController.setProperty("TraceLog_ui_timing", "on");
        Trace tr2 = new Trace("ui");

        if (Trace.ui.timing) Trace.ui.timingm("First of three messages.");
        if (tr.timing) tr.timingm("Second of three messages.");
        if (tr2.timing) tr2.timingm("Third of three messages.");

        TraceController.setProperty("TraceLog_ui_timing", "off");
        if (Trace.ui.timing) System.out.println("ERROR: CACHE OUT OF DATE");
        Trace.ui.debugm("Three messages will be omitted following this one.");
        if (Trace.ui.timing) Trace.ui.timingm("First of three messages not seen.");
        if (tr.timing) tr.timingm("Second of three messages not seen.");
        if (tr2.timing) tr2.timingm("Third of three messages not seen.");

        TraceController.setProperty("TraceBuffer_ui_timing", "on");
        TraceController.setProperty("TraceLog_ui", "world");  // independent.
        Trace.ui.usagem("This message will not be seen in the log.");
        TraceController.setProperty("TraceBuffer_ui", "world");  // independent.
        Trace.ui.usagem("This message will not be seen in either.");
        Trace.ui.worldm("Three timing messages will go to the buffer.");
        if (Trace.ui.timing) Trace.ui.timingm("First of three messages only in buffer.");
        if (tr.timing) tr.timingm("Second of three messages only in buffer.");
        if (tr2.timing) tr2.timingm("Third of three messages only in buffer.");

        TraceController.setProperty("TraceBuffer_ui", "debug");
        TraceController.setProperty("TraceLog_ui", "verbose");
        TraceController.setProperty("TraceBuffer_ui_timing", "false");

        Trace.ui.debugm("Three timing messages will go nowhere.");
        if (Trace.ui.timing) Trace.ui.timingm("First of three messages that go nowhere.");
        if (tr.timing) tr.timingm("Second of three messages that go nowhere.");
        if (tr2.timing) tr2.timingm("Third of three messages that go nowhere.");

        TraceController.setProperty("TraceLog_ui_timing", "on");
        TraceController.setProperty("TraceBuffer_ui_timing", "on");
        if (Trace.ui.timing) Trace.ui.timingm("This message will be seen in both.");

        Trace other = new Trace("other");
        if (other.timing) other.timingm("Should not be seen.");
        TraceController.setProperty("TraceLog_other_timing", "on");
        other.worldm("Timing message for 'other' coming up.");
        if (other.timing) other.timingm("This should be seen.");        


        TraceController.setProperty("TraceLog_ui_timing", "off");
        if (tr.timing) tr.timingm("This message will be seen only in the buffer.");

        TraceController.setProperty("TraceBuffer_ui_timing", "false");
        Trace.ui.worldm("Three messages go nowhere.");
        if (Trace.ui.timing) Trace.ui.timingm("First of three messages that go nowhere.");
        if (tr.timing) tr.timingm("Second of three messages that go nowhere.");
        if (tr2.timing) tr2.timingm("Third of three messages that go nowhere.");

        TraceController.dumpBufferToLog();

        // Some error cases.
        TraceController.setProperty("TraceBuffer_uitiming", "off");
        TraceController.setProperty("TraceBuffer_ui", "timing");
        TraceController.setProperty("TraceBuffer_timing", "ui");
        TraceController.setProperty("TraceBuffer_ui_timing", "of");

        String name = "odd_trace.with_embedded.stuff";
        Trace oddtr = new Trace(name);
        if (oddtr.timing) oddtr.timingm("ERROR: Should not be seen.");
        TraceController.setProperty("TraceLog_"+name+"_timing","true");
        if (oddtr.timing) oddtr.timingm("Even the oddest can be timed.");

        // make sure ordinary odd names still work.
        String name2 = "another_odd.name_with.stuff_in";
        Trace notime = new Trace(name2);
        TraceController.setProperty("TraceLog_"+name2, "debug");
        if (notime.debug && Trace.ON) notime.debugm("This BETTER be seen.");
        TraceController.setProperty("TraceLog_"+name, "debug");
        if (oddtr.debug && Trace.ON) oddtr.debugm("This BETTER be seen as well.");

        Trace.comm.worldm("That's all, folks - end of test.");
    }
}

