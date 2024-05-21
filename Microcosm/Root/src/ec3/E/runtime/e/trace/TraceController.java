/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */


package ec.e.run;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import ec.util.assertion.*;

/**
 * The single trace controller manages all the trace classes.  Most
 * notably, it has one TraceSubsystemMediator for each subsystem
 * being traced.  A TraceSubsystemMediator may mediate several Trace
 * objects.  Messages sent to those trace objects are forwarded to the
 * mediator.  The mediator, under the control of the TraceController,
 * sends the message on to TraceMessageAcceptors that are ready to
 * accept messages.  The two current TraceMessageAcceptors are the
 * TraceLog and the TraceBuffer.  Once upon a time, there was a third,
 * the TraceDisplay, which would echo to the screen the contents of
 * the TraceBuffer.  However, that proved impractical in the current
 * build environment.
 */
public class TraceController implements TraceConstants {

    /**
     * The list of TraceSubsystemMediators for the subsystems being traced.
     * Indexed by subsystem name.
     */
    static private Hashtable myTraceMediators = new Hashtable();

    /** Trace thresholds that apply to subsystems that haven't
     *  been given specific values. NUM_ACCEPTORS elements. */
    static private int myDefaultThresholds[];

    /** The on-disk log. */
    static TraceLog myLog;

    /** 
     * A temporary window into the in-core trace buffer.  Null if no 
     * window.
     */
    // Commented out because the display can't be compiled this early.
    // It would be better to have a "registration" interface that would
    // let an independent GUI announce that it would like to see all
    // new trace messages sent to the buffer.
    // static private TraceDisplay myDisplay;

    /** The in-core trace buffer. */
    static private TraceBuffer myBuffer;

    /** Have we already been initialized? */
    static private boolean myStarted = false;

    /** 
     * The complete list of acceptors, for use by mediators.  The fact
     * that there's a fixed set of acceptors that can't be changed
     * without recompiling is probably the major rigidity of the code.
     *
     * There are currently two acceptors:  on-disk logs and in-core
     * buffers.  The in-core buffer has two "variants": the buffer
     * proper and a GUI window into the buffer. 
     */
    static private TraceMessageAcceptor myAcceptors[][];

    /**
     * This object, if non-null, is informed when notifyFatal or
     * notifyOptional is called.  There is at most one such object,
     * because it may exit.
     */
    static private TraceErrorWatcher myTraceErrorWatcher;

    /**
     * Synchronized static methods are prone to deadlocks in Sun's
     * JVM.  This avoids the problem.  Initialized in the class
     * initializer to make sure it's available before the first trace
     * object is used.
     */
    private static Object synchronizationObject;

    static {
        // Initialize component objects.  Note that these constructors
        // must not call any trace functions, since the accepters and
        // thresholds haven't been set up.  The component objects
        // can't be initialized after Trace, because trace makes use
        // of them.
        synchronizationObject = new Object();
        myLog = new TraceLog();
        // Commented out because the display can't be compiled this early.
        // myDisplay = new TraceDisplay();
        myBuffer = new TraceBuffer();

        TraceMessageAcceptor[] logArray = new TraceMessageAcceptor[1];
        logArray[0] = myLog;
        // Size is set to 1 because the display can't be compiled this
        // early.
        // TraceMessageAcceptor[] bufferArray = new TraceMessageAcceptor[2];
        TraceMessageAcceptor[] bufferArray = new TraceMessageAcceptor[1];
        bufferArray[0] = myBuffer;
        // bufferArray[1] = myDisplay;

        myAcceptors = new TraceMessageAcceptor[NUM_ACCEPTORS][];
        myAcceptors[LOG] = logArray;
        myAcceptors[TRACE] = bufferArray;

        myDefaultThresholds = new int[NUM_ACCEPTORS];
        myDefaultThresholds[TRACE] = STARTING_TRACE_THRESHOLD;
        myDefaultThresholds[LOG] = STARTING_LOG_THRESHOLD;

        // Load Trace.class to define trace.trace().  Otherwise, it
        // only gets loaded when the first client refers to it.  It's
        // convenient to load it as early as possible so that the
        // tracing subsystem's startup can itself be traced.
        // It cannot be loaded earlier than this point.
        Trace.touch();
    }

    /**
     * This routine is used to start the Trace Controller.  Prior to
     * this call, Trace objects may be obtained and messages may be
     * sent to them.  However, the messages will be queued up until
     * this routine is called.  (Note that the messages will be
     * governed by the default thresholds.)
     * <p>
     * Note: This routine may be called before the user interface is
     * available.  This allows on-disk logging to be useful in the 
     * case the system fails before the UI is initialized.
     *
     * @param p the initial set of properties provided by the user.
     * They override the defaults.  They may be changed later.
     */
    static public void start(Properties p) {
        if (myStarted) {
            Trace.trace.errorm("The tracing system is being started for the second time.\n"+
                               "Ignoring the second start.");
            return;
        }       
                     
        myStarted = true;
        Trace.trace.worldm("Tracing system being started.");

        // XXX - temporary backwards compatibility.
        if (!p.containsKey("TraceLog_write")) {
            p.put("TraceLog_write", "true");
        }
        
        if (!p.containsKey("TraceLog_name") && 
            !p.containsKey("TraceLog_dir") && 
            !p.containsKey("TraceLog_tag")) {
            Trace.trace.worldm("TEMPORARY: TraceLog set to standard " +
                               "output for backwards compatibility.");
            p.put("TraceLog_name", "-");
        }

        setProperties(p);
        myLog.setupIsComplete();
        myBuffer.setupIsComplete();
        // Setup is not complete yet for the TraceDisplay.
        new TraceExceptionNoticer();
    }

    /** 
     * Invoke this to tell the TraceController that a display is 
     * ready to use.
     */
    static public void mayUseUI() {
        // Commented out because the display can't be compiled this early.
        // myDisplay.setupIsComplete();
    }

    // Not all errors are ones that should be thrust into a user's face.
    // The following two routines are used by calling code when it does
    // - or may - want to do that.

    /**
     * Notify a user that a fatal error has happened.  Tell her how to
     * report the bug.  Normally does not return.
     * <p>
     * If this method is called before a TraceErrorWatcher has
     * registered, it's not clear what the best thing to do is.  For
     * the moment, we log that as an error and return, in the hopes
     * that the system will hobble along a bit further to the point
     * where a watcher registers and a later notifyFatal causes the
     * user to be notified.
     */
    public static void notifyFatal() {
        if (myTraceErrorWatcher != null) {
            myTraceErrorWatcher.notifyFatal();
        } else {
            Trace.trace.errorm("A fatal error has been reported, " +
                               "but there is no one to report it to.");
        }
    }

    /**
     * If the user wants to hear about nonfatal bugs, notify her.
     * Does return.
     * <p>
     * It is a (non-fatal) error if no object has registered as a
     * TraceErrorWatcher. 
     */
    public static void notifyOptional() {
        if (myTraceErrorWatcher != null) {
            myTraceErrorWatcher.notifyOptional();
        } else {
            Trace.trace.errorm("An optional error has been reported, " +
                               "but there is no one to report it to.");
        }
    }

    /**
     * Register or unregister as a TraceErrorWatcher.
     * <p>
     * This is kind of a stupid interface.  I didn't think it through
     * before "publishing" it.
     *
     * @param aTraceErrorWatcher the object to be informed of an error.
     * 
     * @param add true if the object is to be added, false if it's to
     * be removed.  It's not an error to add without first removing, or
     * to remove without first adding, but it does provoke a warning.
     */
    public static void errorWatcher(TraceErrorWatcher aTraceErrorWatcher, boolean add) {
        if (add) {
            Trace.trace.usagem("Adding an object that watches for errors.");
            if (myTraceErrorWatcher != null) { 
                Trace.trace.warningm("Overriding previous TraceErrorWatcher", 
                                     myTraceErrorWatcher);
            }
            myTraceErrorWatcher = aTraceErrorWatcher;
        } else {
            Trace.trace.usagem("No object will watch for errors.");
            if (myTraceErrorWatcher == null) { 
                Trace.trace.warningm("TraceErrorWatcher has already been removed");
            } else if (myTraceErrorWatcher != aTraceErrorWatcher) {
                Trace.trace.warningm("A TraceErrorWatcher was removed by someone other than itself.");
            }
            myTraceErrorWatcher = null;
        }
    }

    /**
     * Take the contents of the internal trace buffer and
     * add them to the on-disk log.
     */
    public static void dumpBufferToLog() {
        myBuffer.dump(myLog);
    }

    /**
     * This method updates a trace controller from a given
     * set of properties.
     * <p>
     * IMPORTANT:  The properties are processed in an unpredictable
     * order.
     */
    static public void setProperties(Properties p) {
        Enumeration keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = p.getProperty(key);
            // Note that the value cannot be null, but let's check.
            Assertion.test(value != null);

            setProperty(key, value);
        }
    }

    /** 
     * If the given Key names a tracing property, process its value.
     * It is not an error for the key to have nothing to do with
     * tracing; in that case, it's ignored.
     * <p>
     * It is an error for the value to be null.
     */
    // Note: synchronization is the responsibility of the objects
    // whose properties are being changed.
    static public void setProperty(String key, String value)
    {
        Assertion.test(value != null, "Trace property value cannot be null.");
        key = key.trim();
        value = value.trim();
        Trace.trace.debugm("Setting property " + key + " to value " + value);
        try {
            boolean bufferProp = false;  // Setting a TraceBuffer property.
            boolean logProp = false;     // Setting a TraceLog property.
            String originalKey = key;
            String lowerKey = key.toLowerCase();
            String afterUnderbar = null;  // What comes after TraceLog_, for example.

            if (lowerKey.startsWith("tracebuffer_")) {
                bufferProp = true;
                afterUnderbar = originalKey.substring(12);
            } else if (lowerKey.startsWith("trace_")) {
                // This is for backwards compatibility.
                // XXX eventually will set bufferProp or be removed entirely.
                logProp = true;
                afterUnderbar = originalKey.substring(6);
            } else if (lowerKey.startsWith("tracelog_")) {
                logProp = true;
                afterUnderbar = originalKey.substring(9);
            }
            
            if (bufferProp) {
                if (afterUnderbar.equalsIgnoreCase(DEFAULT_NAME)) {
                    changeOneDefault(TRACE, TraceLevelTranslator.toInt(value));
                } else if (timingProperty(afterUnderbar)) {
                    setTiming(TRACE, afterUnderbar, value);
                } else if (afterUnderbar.equalsIgnoreCase("size")) {
                    myBuffer.changeSize(value);
                } else if (afterUnderbar.equalsIgnoreCase("display")) {
                    changeDisplay(value.equalsIgnoreCase("true"));
                } else if (afterUnderbar.equalsIgnoreCase("dump")) {
                    myBuffer.dump(value);
                } else {
                    changeOneSubsystem(TRACE, afterUnderbar, value);
                }
            } else if (logProp) {
                if (afterUnderbar.equalsIgnoreCase(DEFAULT_NAME)) {
                    changeOneDefault(LOG, TraceLevelTranslator.toInt(value));
                } else if (timingProperty(afterUnderbar)) {
                    setTiming(LOG, afterUnderbar, value);
                } else if (afterUnderbar.equalsIgnoreCase("write")) {
                    myLog.changeWrite(value);
                } else if (afterUnderbar.equalsIgnoreCase("dir")) {
                    myLog.changeDir(value);
                } else if (afterUnderbar.equalsIgnoreCase("tag")) {
                    myLog.changeTag(value);
                } else if (afterUnderbar.equalsIgnoreCase("name")) {
                    myLog.changeName(value);
                } else if (afterUnderbar.equalsIgnoreCase("size")) {
                    myLog.changeSize(value);
                } else if (afterUnderbar.equalsIgnoreCase("backups")) {
                    myLog.changeBackupFileHandling(value);
                } else if (afterUnderbar.equalsIgnoreCase("reopen")) {
                    myLog.reopen(value);
                } else {
                    changeOneSubsystem(LOG, afterUnderbar, value);
                }
            }
        } catch (IllegalArgumentException e) {
            Trace.trace.shred(e, "The exception has already been logged.");
        }
        
        // Other properties are ignored, because this method may be
        // called from setProperties, which is given a whole mess of
        // properties, some irrelevant to Trace.
    }

    /**
     * Is this a timing control statement like "TraceBuffer_ui_timing=true"?
     * @param afterFirstUnderbar text after first underbar.
     */
    private static boolean timingProperty(String afterFirstUnderbar) {
        int underbar = afterFirstUnderbar.lastIndexOf('_');
        if (underbar == -1) {
            return false;
        }
        String tail = afterFirstUnderbar.substring(underbar+1);
        return tail.equalsIgnoreCase("timing");
    }

    /**
     * Set the timing value of a given subsystem's acceptor.
     * @param acceptorIndex the acceptor.
     * @param afterFirstUnderbar is of the form <subsystem>_timing,
     *        else an assertion fails.
     * @param value is "on", "off", "true", or "false".
     */
    private static void setTiming(int acceptorIndex,
                           String afterFirstUnderbar,
                           String value) {
        int underbar = afterFirstUnderbar.lastIndexOf('_');
        Assertion.test(underbar != -1);
        String subsystem = afterFirstUnderbar.substring(0, underbar);
        if (value.equalsIgnoreCase("on") ||
            value.equalsIgnoreCase("true")) {
            findOrCreateMediator(subsystem).setTiming(acceptorIndex, true);
        } else if (value.equalsIgnoreCase("off") ||
                   value.equalsIgnoreCase("false")) {
            findOrCreateMediator(subsystem).setTiming(acceptorIndex, false);
        } else {
            Trace.trace.warningm("Unknown timing value given: " + value);
        }
    }

    /**
     * Change the default threshold for some TraceMessageAcceptor.  The
     * change must propagate to all subsystems that track that
     * acceptor's default.
     */
    static private void changeOneDefault(int acceptorIndex, int newThreshold) {
        myDefaultThresholds[acceptorIndex] = newThreshold;
        
        Trace.trace.eventm("The new default threshold for " +
                          acceptorNames[acceptorIndex] + " is " +
                          TraceLevelTranslator.terse(newThreshold));

        Enumeration e = myTraceMediators.elements();
        while (e.hasMoreElements()) {
            TraceSubsystemMediator mediator = 
                (TraceSubsystemMediator) e.nextElement();
            if (mediator.deferToDefaultThreshold[acceptorIndex]) {
                mediator.setOneThreshold(acceptorIndex, newThreshold,
                                         FROM_DEFAULT);
            }
        }
    }

    /** 
     * Change a specific TraceMessageMediator to have its own
     * trace priority threshold OR change it to resume tracking
     * the default.
     */
    static private void changeOneSubsystem(int acceptorIndex,
                                           String subsystem,
                                           String value) {
        if (value.equalsIgnoreCase(DEFAULT_NAME)) {
            // set back to default threshold, whatever that is.
            findOrCreateMediator(subsystem).setOneThreshold(
                acceptorIndex, 
                myDefaultThresholds[acceptorIndex],
                FROM_DEFAULT);
        } else {
            // set to specific threshold.
            findOrCreateMediator(subsystem).setOneThreshold(
                acceptorIndex,
                TraceLevelTranslator.toInt(value),
                FOR_SUBSYSTEM);
        }
    }

    /**
     * Request that the trace buffer be visible in a window.
     * XXX This cannot be made to work given the current layering
     * of the system / build environment.  It should probably be
     * thrown out.
     */
    static public void changeDisplay(boolean showIt) {
        /*
        if (showIt) {
            Trace.trace.eventm("Request to display the trace buffer.");
            if (myDisplay.isAcceptingMessages()) {
                Trace.trace.worldm("Request to display the trace " + 
                                     "buffer, but it's already being displayed.");
            } else { 
                myDisplay.pleaseDisplay(myBuffer.getFirstMessage());
            }
        } else {
            // Currently, this is a no-op.  We could kill the window, but
            // it's much more convenient for the user to do that.  (They'll
            // click rather than type.)
            Trace.trace.eventm("Request to turn off the trace buffer.");
        }
        */
    }
  
    /**
     * Find a TraceSubsystemMediator matching the given name.  Create one if it
     * does not exist.  The new mediator initially contains copies
     * of myDefaultThresholds and a pointer to myAcceptors.
     */
    static private TraceSubsystemMediator findOrCreateMediator(String name) {
        String key = name.toLowerCase();

        TraceSubsystemMediator mediator =
            (TraceSubsystemMediator) myTraceMediators.get(key);
        if (mediator == null) {
            if (Trace.trace != null) {
                Trace.trace.debugm("Creating mediator for " + name);
            }
            mediator = new TraceSubsystemMediator(name, myDefaultThresholds,
                                                  myAcceptors);
            myTraceMediators.put(key, mediator);
        }
        return mediator;
    }
    
    /**
     * This is called by a Trace constructor to inform the Trace
     * Controller that it exists.  The end result of this call is that
     * a TraceSubsystemMediator exists for the Trace object's
     * subsystem, and the Trace object has been initialized with the
     * values it caches.
     */
    // May be unnecessary for this to be synchronized, but it's a rare
    // operation.
    static protected void newTrace(Trace requester, String subsystem) {
        synchronized (synchronizationObject) {
            if (Trace.trace != null) {
                // guarded because this is called during class
                // initialization of Trace.
                Trace.trace.debugm("New trace for " + subsystem + " is " +
                                   requester);
            }
            TraceSubsystemMediator mediator = findOrCreateMediator(subsystem);
            mediator.newCache(requester);
            if (Trace.trace != null) {
                Trace.trace.debugm("New trace added to " + mediator);
            }
        }
    }

}



