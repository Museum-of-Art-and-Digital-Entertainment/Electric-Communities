
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Test of Tags, log being turned off.
 */
class Tag
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "false");
        TraceController.start(props);
        Trace.trace.worldm("This message will appear only in the buffer.");
        System.out.println("First log is Taglog.txt");

        TraceController.setProperty("TraceLog_name", "TagLog.txt");
        TraceController.setProperty("TraceLog_write", "true");
        Trace.trace.worldm("This is the first message to appear in the log.");
        Trace.trace.worldm("Next log will be TagLog.<date>.0.txt");

        TraceController.setProperty("TraceLog_write", "false");
        Trace.trace.worldm("This message will appear only in the buffer.");
        
        TraceController.setProperty("TraceLog_tag", "TagLog");
        TraceController.setProperty("TraceLog_write", "true");
        Trace.trace.worldm("This is the first message to appear in the new log.");

        Trace.trace.worldm("Turn log on and off, using same log.");
        TraceController.setProperty("TraceLog_write", "false");
        Trace.trace.worldm("This message will appear only in the buffer.");
        TraceController.setProperty("TraceLog_write", "true");
        
        Trace blankie = new Trace("subsystem with blanks");
        blankie.worldm("Next logfile will use empty tag.");

        TraceController.setProperty("TraceLog_tag", "");
        TraceController.setProperty("TraceLog_write", "false");
        Trace.trace.worldm("This message will appear only in the buffer.");
        TraceController.setProperty("TraceLog_write", "true");

        blankie.worldm("Buffer dump is in Tag.dump.");
        TraceController.setProperty("TraceBuffer_dump", "Tag.dump");
        blankie.worldm("That's all, folks.");
    }
}

