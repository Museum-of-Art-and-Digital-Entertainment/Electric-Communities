
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;


/** See =README= */
class Throws
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_write", "true");
        props.put("TraceLog_name", "throws.txt");
        props.put("TraceLog_comm", "verbose");
        TraceController.start(props);
        (new Throws()).go();
    }

    public void deep (int level, Vector v) {
        if (level == 0) 
            throw new Error("level is " + level);
        else
            v.addElement(new Integer(level));
            deep(level-1, v);
    }

    public void go() {
        Vector v;
        v = new Vector();
        try {
            deep(0, v);
        } catch (Error e) {
            Trace.comm.errorReportException(e, "error");
            Trace.comm.errorm("error direct", e);
            Trace.comm.errorm("error vector", v);
        }

        v = new Vector();
        try {
            deep(1, v);
        } catch (Error e) {
            Trace.comm.warningReportException(e, "warning");
            Trace.comm.warningm("warning direct", e);
            Trace.comm.warningm("warning vector", v);
        }

        v = new Vector();
        try {
            deep(2, v);
        } catch (Error e) {
            Trace.comm.worldReportException(e, "world");
            Trace.comm.worldm("world direct", e);
            Trace.comm.worldm("world vector", v);
        }

        v = new Vector();
        try {
            deep(3, v);
        } catch (Error e) {
            Trace.comm.usageReportException(e, "usage");
            Trace.comm.usagem("usage direct", e);
            Trace.comm.usagem("usage vector", v);
        }

        v = new Vector();
        try {
            deep(4, v);
        } catch (Error e) {
            Trace.comm.eventReportException(e, "event");
            Trace.comm.eventm("event direct", e);
            Trace.comm.eventm("event vector", v);
        }

        v = new Vector();
        try {
            deep(5, v);
        } catch (Error e) {
            Trace.comm.debugReportException(e, "debug");
            Trace.comm.debugm("debug direct", e);
            Trace.comm.debugm("debug vector", v);
        }

        v = new Vector();
        try {
            deep(6, v);
        } catch (Error e) {
            Trace.comm.verboseReportException(e, "verbose");
            Trace.comm.verbosem("verbose direct", e);
            Trace.comm.verbosem("verbose vector", v);
        }
    }
}

