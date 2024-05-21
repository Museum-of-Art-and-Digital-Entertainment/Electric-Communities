package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.*;

// See =README=

class MemLeak
{

    public static void main (String[] args)  {
        Properties props = new Properties();
        props.put("TraceLog_write", "false");
        props.put("TraceLog_name", "memleak.log");
        props.put("TraceBuffer_trace", "debug");
        TraceController.start(props);
        (new MemLeak()).go();
    }

    public void go() {
        Date start = new Date();
        for (int i = 0;;i++) {
            if (Trace.trace.debug && Trace.ON) Trace.trace.debugm("Message " + i);
        }
    }
}

