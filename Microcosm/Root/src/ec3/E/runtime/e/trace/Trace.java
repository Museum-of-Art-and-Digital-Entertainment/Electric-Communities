/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Original version by Eric Messick.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import ec.util.assertion.*;
import ec.util.EThreadGroup;
import ec.util.ExceptionNoticer;


/**
 * This might better be called TraceCache, but the name is retained
 * for backward compatibility.  The major purpose of this class is to
 * hold a cache of a relevant trace priority threshold, so that a user
 * of the trace code can quickly decide whether to call a trace
 * method.  Methods on this object communicate with the master
 * TraceController, which does the real work of tracing.
 * <p>
 * Secondarily, this class holds some miscellaneous functions useful in
 * tracing.  Some of these are deprecated.
 */

final public class Trace implements RtCodeable, TraceConstants {
    /** 
     * Set this to false to compile out all tracing. This also has
     * to be set in TraceDummies.java
     */
    public static final boolean ON = true;

    /** 
     * Error messages report on some internal error.  They don't
     * necessarily lead to the system stopping, but they might.  Error
     * messages are always logged. 
     */
    public boolean error;
    /**
     * Warning messages are not as serious as errors, but they're
     * signs of something odd. 
     */
    public boolean warning;
    /**
     * World messages track the state of the world as a whole.   They
     * are the sort of things world operators ask for specifically, such
     * as "can you tell me when someone connects."   They should appear
     * only occasionally, much less often than once per second.  This is
     * probably the level used for the on-disk log in the shipped
     * version. 
     */
    public boolean world;
    /**
     * Usage messages are used to answer the question "who did what up
     * to the point the bug appeared?" ("Spock entered Azturf.  Spock
     * started trading with Kyra.  Kyra gave Spock a diamond in exchange
     * for a lump of coal. Kyra spoke.")  They are also used to collect
     * higher-level usability information.  This is the level probably
     * used for the transient log in the shipped version; during
     * development, we can set the on-disk log to this level. 
     */
    public boolean usage;
    /**
     * Event messages describe the major actions the system takes in
     * response to user actions.  The distinction between this category
     * and debug is fuzzy, especially since debug is already used for
     * many messages of this type.  However, it can be used to log
     * specific user gestures for usability testing, and to log information for
     * testers.  
     */
    public boolean event;
    /**
     * Debug messages provide more detail for people who want to delve
     * into what's going on, probably to figure out a bug.   
     */
    public boolean debug;
    /**
     * Verbose messages provide even more detail than debug.  They're
     * probably mainly used when first getting the code to work.  
     */
    public boolean verbose;

    /**
     * Timing messages are for performance tuning.  Whether timing
     * messages are logged is independent of the values of all the
     * other trace variables.
     */
    public boolean timing;

    /**
     * Deprecated.  Always has the same value as debug.
     */
    public boolean tracing;

    /**
     *  Predefined subsystems.  
     */
    static public Trace agent = new Trace("agent");
    static public Trace comm = new Trace("comm");
    static public Trace console = new Trace("console");
    static public Trace automation = new Trace("automation");
    /** Distributed garbage collector. */
    static public Trace dgc = new Trace("dgc");
    static public Trace dynamics = new Trace("dynamics");
    /** The E runtime */
    static public Trace eruntime = new Trace("eruntime");
    static public Trace file = new Trace("file");
    static public Trace fonts = new Trace("fonts");
    static public Trace gui = new Trace("gui");
    static public Trace io = new Trace("io");
    static public Trace persist = new Trace("persist");
        static public Trace serialstate = new Trace("serialstate");
    /** Pluribus runtime */
    static public Trace plruntime = new Trace("plruntime");
    static public Trace repository = new Trace("repository");
    static public Trace scene = new Trace("scene");
    static public Trace sound = new Trace("sound");
    /** Classes involved in starting up the world. */
    static public Trace startup = new Trace("startup");
    static public Trace timers = new Trace("timers");
    static public Trace trace = new Trace("trace");
    /** Classes involved in going from unum to framework and back. */
    static public Trace ui = new Trace("ui");
    static public Trace una = new Trace("una");
    static public Trace vat = new Trace("vat");

    /**
     * This is the mediator that actually disposes of a message. 
     * Multiple Trace objects may share the same mediator.
     */
    protected TraceSubsystemMediator myMediator;

    /**
     * The subsystem is the group of classes this Trace object applies
     * to.  This exists for encoding/decoding; it duplicates
     * information available in the Mediator.
     */
    protected String mySubsystem;

// Constructors

    /**
     * Initialize a Trace object for the given subsystem.  It is legal
     * for the subsystem to contain blanks.  Things will become confused
     * if it contains a colon, but the code does not check for that.
     */
    public Trace(String subsystem) {
        mySubsystem = subsystem;
        TraceController.newTrace(this, subsystem);
    }

    /** 
     * Invoking this method causes this class to be loaded, which
     * causes all the static trace objects to be defined.  In
     * particular, Trace.trace becomes defined.  That's convenient, in that
     * it allows more tracing of the tracing startup itself.
     */
    static protected void touch()
    {
    }

// PUBLIC INTERFACE

    /**
     * Public interface methods that accept classes are the names of
     * the boolean fields, with 'm' appended (to denote 'method').
     *
     * @note  In java, the 'm' isn't required.  However, in the past
     * fields and methods with the same names have broken ecomp.
     * Currently, it works, but danfuzz says it would be prudent to
     * avoid the case.
     */

    public void timingm(String message) {
        if (timing) recordTraceMessage(message, TIMING, null);
    }

    public void verbosem(String message) {
        if (verbose) recordTraceMessage(message, VERBOSE, null);
    }

    public void debugm(String message) {
        if (debug) recordTraceMessage(message, DEBUG, null);
    }

    public void eventm(String message) {
        if (event) recordTraceMessage(message, EVENT, null);
    }

    public void usagem(String message) {
        if (usage) recordTraceMessage(message, USAGE, null);
    }

    public void worldm(String message) {
        if (world) recordTraceMessage(message, WORLD, null);
    }

    public void warningm(String message) {
        if (warning) recordTraceMessage(message, WARNING, null);
    }

    public void errorm(String message) {
        if (error) recordTraceMessage(message, ERROR, null);
    }

    /**
     * These methods take an Object in addition to a string.
     * Note that logging a null object is the same thing as logging
     * no object at all.  (Fix this if anyone complains.)
     */

    public void timingm(String message, Object o) {
        if (timing) recordTraceMessage(message, TIMING, o);
    }

    public void verbosem(String message, Object o) {
        if (verbose) recordTraceMessage(message, VERBOSE, o);
    }

    public void debugm(String message, Object o) {
        if (debug) recordTraceMessage(message, DEBUG, o);
    }

    public void eventm(String message, Object o) {
        if (event) recordTraceMessage(message, EVENT, o);
    }

    public void usagem(String message, Object o) {
        if (usage) recordTraceMessage(message, USAGE, o);
    }

    public void worldm(String message, Object o) {
        if (world) recordTraceMessage(message, WORLD, o);
    }

    public void warningm(String message, Object o) {
        if (warning) recordTraceMessage(message, WARNING, o);
    }

    public void errorm(String message, Object o) {
        if (error) recordTraceMessage(message, ERROR, o);
    }

    private void recordTraceMessage(String message,
                                    int level, Object o) {
        StackFrameData frameData = new StackFrameData(3);

        TraceMessage traceMessage = new TraceMessage(frameData);
        traceMessage.message = message;
        traceMessage.object = o;
        traceMessage.level = level;
        traceMessage.subsystem = mySubsystem;
        myMediator.accept(traceMessage);
    }                                         

    // Not all errors are ones that should be thrust into a user's face.
    // The following two routines are used by calling code when it does
    // - or may - want to do that.

    /**
     * Notify a user that a fatal error has happened.  Tell her how to
     * report the bug.  Does not return.
     * <p>
     * The work is done by the TraceController static object.  This
     * method is a convenience for code that doesn't import TraceController.
     */
    public void notifyFatal() {
        TraceController.notifyFatal();
    }

    /**
     * If the user wants to hear about nonfatal bugs, notify her.
     * Does return.
     * <p>
     * The work is done by the TraceController static object.  This
     * method is a convenience for code that doesn't import TraceController.
     */
    public void notifyOptional() {
        TraceController.notifyOptional();
    }



// PUBLIC UTILITIES

    /**
     * Convert an array to a string.
     * @param a the string.
     * @param name string to prepend to the array.
     * @param pre string to prepend to an element.
     * @param sep string to separate elements.
     * @param post string to append to an element.
     */
    public static String arrayToString(Object a[], String name, String
                               pre, String sep, String post) { 
        int i = 0;
        String s = name + "[";
        while (i < a.length) {
            if (a[i] == null) {
                s += "null" ;
            }
            else {
                s += pre + a[i].toString() + post;
            }
            if (i++ < a.length - 1)
                s += sep;
        }
        s += "]" ;
        return s;
    }

// TRACING EXCEPTIONS

    // deprecated: parallel development.

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the ERROR level.
     * This is precisely equivalent to errorm(msg, t)
     */
    public void errorReportException(Throwable t, String msg) {
        if (error) recordTraceMessage(msg, ERROR, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the WARNING level.
     * This is precisely equivalent to warningm(msg, t)
     */                                   
    public void warningReportException(Throwable t, String msg) {
        if (warning) recordTraceMessage(msg, WARNING, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the WORLD level.
     * This is precisely equivalent to worldm(msg, t)
     */
    public void worldReportException(Throwable t, String msg) {
        if (world) recordTraceMessage(msg, WORLD, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the USAGE level.
     * This is precisely equivalent to usagem(msg, t)
     */
    public void usageReportException(Throwable t, String msg) {
        if (usage) recordTraceMessage(msg, USAGE, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the EVENT level.
     * This is precisely equivalent to eventm(msg, t)
     */
    public void eventReportException(Throwable t, String msg) {
        if (event) recordTraceMessage(msg, EVENT, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the DEBUG level.
     * This is precisely equivalent to debugm(msg, t)
     */
    public void debugReportException(Throwable t, String msg) {
        if (debug) recordTraceMessage(msg, DEBUG, t);
    }

    /**
     * Record the Throwable along with a trace message.
     * The trace message is reported at the VERBOSE level.
     * This is precisely equivalent to verbosem(msg, t)
     */
    public void verboseReportException(Throwable t, String msg) {
        if (verbose) recordTraceMessage(msg, VERBOSE, t);
    }

    /**
      * To ensure that exceptional conditions are only being ignored
      * for good reason, we adopt the discipline that a caught
      * exception should <p>
      *
      * 1) be rethrown <p>
      * 2) cause another exception to be thrown instead <p>
      * 3) be ignored, in a traceable way, for some stated reason <p>
      *
      * Only by making #3 explicit can we distinguish it from
      * accidentally ignoring the exception.  An exception should,
      * therefore, only be ignored by asking a Trace object to
      * shred it.  This request carries a string that justifies
      * allowing the program to continue normally following this
      * event.  As shredded exceptions will likely be symptoms of
      * bugs, one will be able to have them traced.
      * <p>
      * The reason for the shredding is logged at verbose level.
      * <p>
      * This now appears to be deprecated.
      */
    public void shred(Throwable ex, String reason) {
        verboseReportException(ex, reason);
    }


// DEPRECATED FUNCTIONS

    public void $(String message) {
      recordTraceMessage(message, DEBUG, null);
    }

    // deprecated
    public void traceMode (boolean b) {
        // warningm("Deprecated function traceMode called.  It is now a no-op.");
    }

    // deprecated
    public void fileMode (boolean b) {
        // warningm("Deprecated function fileMode called.  It is now a no-op.");
    }

    // deprecated
    public void lineMode (boolean b) {
        // warningm("Deprecated function lineMode called.  It is now a no-op.");
    }

    // deprecated
    public void methodMode (boolean b) {
        // warningm("Deprecated function methodMode called.  It is now a no-op.");
    }

    // deprecated
    public void nl () {
        $("");
    }

    // deprecated
    // public void printStackTrace () {
    //     $(getStackTrace ());
    // }

    // deprecated
    // public void printStackTrace(Throwable t) {
    //     // GET STACK TRACE AND SEND AS MESSAGE TO TRACECONTROLLER;
    //     t.printStackTrace(System.err);
    // }

    /**
     * It is no longer legal for anyone other than the TraceController
     * to set the trace mode.
     */
    public void setTraceMode(String mode) {
        warningm("Deprecated function setTraceMode called.  It is now a no-op.");
    }

    /**
     * Synchronized static methods are prone to deadlocks in Sun's
     * JVM.  This avoids the problem.
     */
    private static Object synchronizationObject = new Object();

    /**
     * Return the current stack trace as a string.  Deprecated in
     * favor of StackFrameData.getStackTrace().
     */
    public static String getStackTrace () {
        // Retain this code so we don't have to futz around
        // with removing this frame's entry from the string
        // getStackTrace returns.
        synchronized (synchronizationObject) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(bytes);
            Exception e = new Exception();
            e.printStackTrace(stream);
            String trace = bytes.toString();
            int start = trace.indexOf('\n') + 1;
            start = trace.indexOf('\n', start) + 1;
            return trace.substring(start);
        }
    }

// ENCODING AND DECODING

    public String classNameToEncode (RtEncoder encoder) {
        return null;
    }

    public void encode (RtEncoder encoder) {
        errorm("Trace object being encoded!");
        try {
            encoder.writeUTF(mySubsystem);
        } catch (Exception e) {
            shred(e, "don't know what an encoder exception is.");
        }
    }

    public Object decode (RtDecoder decoder) {
        errorm("Trace object being decoded!");
        String tag = "???";
        try {
            tag = decoder.readUTF();
        } catch (Exception e) {
            shred(e, "don't know what a decoder exception is.");
        }
        return new Trace(tag);
    }
}

class TraceExceptionNoticer implements ExceptionNoticer 
{
    TraceExceptionNoticer() {
        EThreadGroup.setExceptionNoticer(this);
    }
    
    public void noticeReportedException(String msg, Throwable t) {
        Trace.eruntime.errorm(msg, t);
    }
    
    public void noticeUncaughtException(String msg, Throwable t) {
        Trace.eruntime.errorm(msg, t);
        TraceController.notifyFatal();
    }
    
}
