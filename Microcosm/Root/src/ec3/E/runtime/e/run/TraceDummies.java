package ec.e.run;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;

  
public class TraceController {

    static {
        throw new ExceptionInInitializerError("Loaded dummy TraceController class");
    }

    static public void start(Properties p) {
        throw new Error("dummy class");
    }

    static public void setProperties(Properties p) {
        throw new Error("dummy class");
    }
}

final public class Trace {

    static {
        throw new ExceptionInInitializerError("Loaded dummy Trace class");
    }

    /** 
     * Set this to false to compile out all tracing. This also
     * has to be set in Trace.java
     */
    public static final boolean ON = true;

    /* The different tracing levels. */
    public boolean verbose;
    public boolean debug;
    public boolean event;
    public boolean usage;
    public boolean world;
    public boolean warning;
    public boolean error;
    public boolean tracing; // depricated
    public boolean timing;

    // Predefined subsystems.  They can only be initialized after
    // properties are parsed.  That will change in later versions,
    // when defaults can be changed on the fly.  
    static public Trace agent;
    static public Trace comm;
    static public Trace console;
    static public Trace dgc;
    static public Trace dynamics;
    static public Trace eruntime = new Trace(); // for minimal-test
    static public Trace file;
    static public Trace fonts;
    static public Trace gui;
    static public Trace io;
    static public Trace persist;
    static public Trace plruntime;
    static public Trace repository;
    static public Trace scene;
    static public Trace startup;
    static public Trace timers;
    static public Trace ui;
    static public Trace una;
    static public Trace vat;

    
    public Trace() {
    }

    public Trace(String subsystem) {
    }

    // this constructor is depricated.  
    public Trace(boolean ignored, String ignored2) {
    }

    // deprecated
    public void traceMode (boolean b) {
        throw new Error("dummy class");
    }

    // deprecated
    public void fileMode (boolean b) {
        throw new Error("dummy class");
    }

    // deprecated
    public void lineMode (boolean b) {
        throw new Error("dummy class");
    }

    // deprecated
    public void methodMode (boolean b) {
        throw new Error("dummy class");
    }

    // deprecated
    public void nl () {
        throw new Error("dummy class");
    }

    // deprecated(?)
    //public void printStackTrace () {
    //    System.err.println (getStackTrace ());
    //}

    // XXX should be one of these for each trace level
    public void errorReportException(Throwable t, String msg) {
        System.err.println("[DUMMY TRACE] error: " + msg);
        t.printStackTrace(System.err);
    }

    // XXX should be one of these for each trace level
    public void warningReportException(Throwable t, String msg) {
        System.err.println("[DUMMY TRACE] warning: " + msg);
        t.printStackTrace(System.err);
    }

    // XXX should be one of these for each trace level
    public void debugReportException(Throwable t, String msg) {
        System.err.println("[DUMMY TRACE] debug: " + msg);
        t.printStackTrace(System.err);
    }

    // XXX should be one of these for each trace level
    public void verboseReportException(Throwable t, String msg) {
        System.err.println("[DUMMY TRACE] verbose: " + msg);
        t.printStackTrace(System.err);
    }

    public void setTraceMode(String mode) {
        throw new Error("dummy class");
    }

    public synchronized static String getStackTrace () {
        throw new Error("dummy class");
    }

    public void $(String message) {
        throw new Error("dummy class");
    }

    public void verbosem(String message) {
        throw new Error("dummy class");
    }

    public void debugm(String message) {
        throw new Error("dummy class");
    }

    public void eventm(String message) {
        throw new Error("dummy class");
    }

    public void usagem(String message) {
        throw new Error("dummy class");
    }

    public void worldm(String message) {
        throw new Error("dummy class");
    }

    public void warningm(String message) {
        throw new Error("dummy class");
    }

    public void errorm(String message) {
        throw new Error("dummy class");
    }

    public void timingm(String message) {
        throw new Error("dummy class");
    }
    
    public void verbosem(String message, Object o) {
        throw new Error("dummy class");
    }

    public void debugm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void eventm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void usagem(String message, Object o) {
        throw new Error("dummy class");
    }

    public void worldm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void warningm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void errorm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void timingm(String message, Object o) {
        throw new Error("dummy class");
    }

    public void shred(Throwable ex, String reason) {
        throw new Error("dummy class");
    }
}

