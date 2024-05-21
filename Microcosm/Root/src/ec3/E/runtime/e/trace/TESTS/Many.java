package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.*;

/*
 * Crude timings.  See =README=
 */

class Many
{
    static Trace tr;

    public static void main (String[] args)  {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", args[0]);
        TraceController.start(props);
        tr = new Trace("tr");
        (new Many()).go();
    }

    public void go() {
        Date start = new Date();
        for (int i = 0; i < 1000; i++) {
            if (tr.error) tr.errorm("classTrace.error: Test.main");
        }
        Date end = new Date();
        System.out.println("Start at " + start);
        System.out.println("End at " + end);
    }
}

