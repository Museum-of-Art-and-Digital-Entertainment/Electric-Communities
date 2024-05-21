package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.*;
import java.io.*;

/* See =README= */

class Threads2 implements Runnable
{
    Trace tr;
    String name;
    static PrintStream out;
    boolean iPickLogName;

    public static void main (String[] args)  {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "threads2sub1.log");
        props.put("TraceLog_size", "1000");
        TraceController.start(props);
        (new Thread(new Threads2("thread1", true))).start();
        (new Thread(new Threads2("thread2", false))).start();
    }

    public Threads2(String aName, boolean pickLogName) {
        name = aName;
        tr = new Trace(aName);
        iPickLogName = pickLogName;
    }

    public void run() {
        if (iPickLogName) { 
            for (int outer = 0; outer < 10000; outer++) {
                TraceController.setProperty("TraceLog_name",
                    (outer % 2 == 1 ? "threads2sub1.log" : "threads2sub2.log"));
                                                  
                for (int i = 0; i < 10; i++) {
                    tr.worldm(name);
                }
            }
        } else {
            for (int i = 0; i < 100000; i++) {
                tr.worldm(name);
            }
        }
    }
}

