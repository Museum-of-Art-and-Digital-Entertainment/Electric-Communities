package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.*;
import java.io.*;

/* See =README= */

class Threads implements Runnable
{
    Trace tr;
    String name;
    static PrintStream out;

    public static void main (String[] args)  {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "threads.log");
        props.put("TraceLog_trace", "debug");
        props.put("TraceLog_thread1", "debug");
        props.put("TraceLog_thread2", "debug");
        TraceController.start(props);
        (new Thread(new Threads("thread1"))).start();
        (new Thread(new Threads("thread2"))).start();
    }

    public Threads(String aName) {
        name = aName;
        tr = new Trace(aName);
    }

    public void run() {
        for (int i = 0; i < 1000; i++) {
            if (tr.debug && Trace.ON) tr.debugm(name);
        }
    }
}

