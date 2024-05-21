package ec.e.run;

import ec.util.EThreadGroup;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;

/*
 * NOTE:  This file contains three classes.  In the eventual revision
 * of the Trace package, it will live in its own subdirectory, but
 * there are some open issues before I can split this file up.
 */

/** 
 * This class decomposes the Nth stack frame above it and provides
 * accessors to useful information.  
 * <p>
 * DANGER: it is HIGHLY dependent on the particular way the 
 * implementation prints stack traces.  It should probably do some
 * syntax checking.
 * <p>
 * For reference, here's the current print format:
 * <pre>
 * java.lang.Exception
 *  at Trace.$(Trace.java:217)
 *  at Test.go(Test.java:25)
 *  at Test.main(Test.java:20)
 * </pre>
 */
class StackFrameData
{
  /**
   * The name of the method running in the targeted frame.
   * It is partly qualified, consisting of the last element of the
   * classname plus the method name (for example, "String.indexOf").
   */
  protected String methodName;

  /**
   * The name of the class that method belongs to.
   * Fully qualified.
   */ 
  protected String className;
 
  /** The file that method is in.  The full pathname is not available. */
  protected String fileName;
  
  /** The line number the trace call is on. */
  protected String lineNumber;

  /** 
   * Collect an earlier frame's data.
   * If aboveCount is 1, the data from the caller of this constructor
   * is collected. 2 means the frame above that, and so on.
   */
  StackFrameData(int aboveCount)
  {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream stream = new PrintStream(bytes);
      Exception stupid = new Exception();
      stupid.printStackTrace(stream);

      String trace = bytes.toString();
      /* System.out.println(trace); */
    

      int start = trace.indexOf('\n') + 1;   // skip "java.lang.Exception"
      start = trace.indexOf('\n', start) + 1;  // skip this routine.
      while (--aboveCount > 0)
        {
          // Skip preceding routines.
          start = trace.indexOf('\n', start) + 1;
        }

      /* We're looking at:
         at Trace.Test.main(Test.java:21)
         */
      start += 4;                 // skip "\tat "

      /* We're looking at:
         Trace.Test.main(Test.java:21)
         */

      int openParen = trace.indexOf('(', start);
      int colon = trace.indexOf(':', openParen);
      int closeParen = trace.indexOf(')', colon);
      methodName = trace.substring(start, openParen); // fully qualified.

      /* Fetch fully qualified classname. */
      int classMethodDot = methodName.lastIndexOf(".");
      className = methodName.substring(0, classMethodDot);

      /* Strip methodname down to last element of classname + method. */
      int classDot = methodName.lastIndexOf(".", classMethodDot - 1);
      methodName = methodName.substring(classDot + 1);

      fileName = trace.substring(openParen + 1, colon);
      lineNumber = trace.substring(colon + 1, closeParen);
    } catch (Exception e) {
      className = methodName = fileName = lineNumber = "unknown";
    }
    /*
      System.out.println("FINISHED:\n" +
      " class = " + className +
      " method = " + methodName +
      " file = " + fileName +
      " lineNumber = " + lineNumber);
      */
  }
}

  
public class TraceController {
    static public void start(Properties p) {
        setProperties(p);
        // This can go away in later versions.  But for now, Trace
        // predefined subsystems can only be initialized AFTER the
        // trace modes are set.
        Trace.initializePredefinedSubsystems();
    }

    // XXX. This method will be removed after we switch to the new 
    // Trace interface.
    public TraceController(Properties p) {
        setTraceModes(p);
        // This can go away in later versions.  But for now, Trace
        // predefined subsystems can only be initialized AFTER the
        // trace modes are set.
        Trace.initializePredefinedSubsystems();
    }

    // Identical to setTraceModes, except for being static.
    // XXX. This version will replace it soon.
    static public void setProperties(Properties p) {
        // In the final version, it won't matter if the default is
        // changed after particular subsystems are initialized.  But
        // it matters now, so we make two passes.
        Enumeration keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("Trace_")) {
                String value = p.getProperty(key);
                key = key.substring(6);
                if (key.equalsIgnoreCase("default")) {
                    Trace.defaultMode = value;
                }
            }
        }
        keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("Trace_")) {
                String value = p.getProperty(key);
                key = key.substring(6);
                if (key.equals("default")) {
                    // Already set, but doesn't hurt to set again.
                    Trace.defaultMode = value;
                } else if (key.equals("size")) {
                    System.err.println("Trace system doesn't support '" +
                                       key + "' yet.");
                } else if (key.equals("display")) {
                    System.err.println("Trace system doesn't support '" +
                                       key + "' yet.");
                } else {
                    Trace.traceModes.put(key, value);
                    RtWeakCell weak = (RtWeakCell)Trace.traces.get(key);
                    if (weak != null) {
                        Trace t = (Trace)weak.get();
                        t.setTraceMode(value);
                    }
                }
            } else if (key.startsWith("Log_")) {
                System.err.println("Trace system doesn't support " +
                                   "Log keywords yet.");
            }
        }
    }

    // XXX. Soon to be replaced by the static setProperties.
    public void setTraceModes(Properties p) {
        // In the final version, it won't matter if the default is
        // changed after particular subsystems are initialized.  But
        // it matters now, so we make two passes.
        Enumeration keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("Trace_")) {
                String value = p.getProperty(key);
                key = key.substring(6);
                if (key.equalsIgnoreCase("default")) {
                    Trace.defaultMode = value;
                }
            }
        }
        keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("Trace_")) {
                String value = p.getProperty(key);
                key = key.substring(6);
                if (key.equals("default")) {
                    // Already set, but doesn't hurt to set again.
                    Trace.defaultMode = value;
                } else if (key.equals("size")) {
                    System.err.println("Trace system doesn't support '" +
                                       key + "' yet.");
                } else if (key.equals("display")) {
                    System.err.println("Trace system doesn't support '" +
                                       key + "' yet.");
                } else {
                    Trace.traceModes.put(key, value);
                    RtWeakCell weak = (RtWeakCell)Trace.traces.get(key);
                    if (weak != null) {
                        Trace t = (Trace)weak.get();
                        t.setTraceMode(value);
                    }
                }
            } else if (key.startsWith("Log_")) {
                System.err.println("Trace system doesn't support " +
                                   "Log keywords yet.");
            }
        }
    }

    public Hashtable getTraceModes() {
        return (Hashtable)Trace.traceModes.clone();
    }
}

final public class Trace implements RtCodeable, RtWeakling {
    static Hashtable traces = new Hashtable();
    static Hashtable traceModes = new Hashtable();
    static String defaultMode = "error";

    /* The different tracing levels. */
    public boolean verbose;
    public boolean debug;
    public boolean event;
    public boolean usage;
    public boolean world;
    public boolean warning;
    public boolean error;
    public boolean tracing; // depricated

    // Predefined subsystems.  They can only be initialized after
    // properties are parsed.  That will change in later versions,
    // when defaults can be changed on the fly.  
    static public Trace agent;
    static public Trace comm;
    static public Trace console;
    static public Trace dgc;
    static public Trace dynamics;
    static public Trace eruntime;
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

    static protected void initializePredefinedSubsystems() {
        agent = new Trace("agent");
        comm = new Trace("comm");
        console = new Trace("console");
        dgc = new Trace("dgc");
        dynamics = new Trace("dynamics");
        eruntime = new Trace("eruntime");
        file = new Trace("file");
        fonts = new Trace("fonts");
        gui = new Trace("gui");
        io = new Trace("io");
        persist = new Trace("persist");
        plruntime = new Trace("plruntime");
        repository = new Trace("repository");
        scene = new Trace("scene");
        startup = new Trace("startup");
        timers = new Trace("timers");
        ui = new Trace("ui");
        una = new Trace("una");
        vat = new Trace("vat");
    }

    private PrintStream myPrintStream = System.out;
    private String myCaller;
    private RtWeakCell myWeakCell;
    
    public Trace() {
        this(new StackFrameData(2).className);
    }

    public Trace(String subsystem) {
        myCaller = subsystem;
        synchronized (traces) {
            myWeakCell = new RtWeakCell(this);
            traces.put(myCaller, myWeakCell);
        }
        String mode = (String)traceModes.get(myCaller);
        if (mode == null) {
            mode = defaultMode;
            // Don't set the tracemode, as this subsystem should
            // track changes to the default.
            // traceModes.put(myCaller, mode);
        }
        setTraceMode(mode);
    }

  

    // this constructor is depricated.  it's just a copy of the no
    // argument constructor.  it's not just this() beacuse that screws
    // up callingClass().
    public Trace(boolean ignored, String ignored2) {
        myCaller = callingClass();
        synchronized (traces) {
            myWeakCell = new RtWeakCell(this);
            traces.put(myCaller, myWeakCell);
        }
        String mode = (String)traceModes.get(myCaller);
        if (mode == null) {
            mode = defaultMode;
            // Don't set the tracemode, as this subsystem should
            // track changes to the default.
            // traceModes.put(myCaller, mode);
        }
        setTraceMode(mode);
    }

    // deprecated
    public void traceMode (boolean b) {
        setTraceMode (b ? "debug" : "off");
    }

    // deprecated
    public void fileMode (boolean b) {
    }

    // deprecated
    public void lineMode (boolean b) {
    }

    // deprecated
    public void methodMode (boolean b) {
    }

    // deprecated
    public void nl () {
        System.err.println ("");
    }

    // deprecated(?)
    //public void printStackTrace () {
    //    System.err.println (getStackTrace ());
    //}

    // XXX should be one of these for each trace level
    public void errorReportException(Throwable t, String msg) {
        if (error) {
            recordTraceMessage(msg);
            EThreadGroup.reportException(t);
        }
    }

    // XXX should be one of these for each trace level
    public void warningReportException(Throwable t, String msg) {
        if (warning) {
            recordTraceMessage(msg);
            EThreadGroup.reportException(t);
        }
    }

    // XXX should be one of these for each trace level
    public void debugReportException(Throwable t, String msg) {
        if (debug) {
            recordTraceMessage(msg);
            EThreadGroup.reportException(t);
        }
    }

    // XXX should be one of these for each trace level
    public void verboseReportException(Throwable t, String msg) {
        if (verbose) {
            recordTraceMessage(msg);
            EThreadGroup.reportException(t);
        }
    }

    public void setTraceMode(String mode) {
        if (mode == null) {
            mode = "off";
        }
        verbose = (mode.equals("verbose"));
        debug = (mode.equals("debug")) || verbose ;
        event = (mode.equals("event")) || debug ;
        usage = (mode.equals("usage")) || event ;
        world = (mode.equals("world")) || usage ;
        warning = (mode.equals("warning")) || world ;
        error = (mode.equals("error")) || warning ;
        tracing = debug ; // this is just to keep current code working ... depricated
        if (! error) {
            // Can only mean that no mode matched.
            //System.err.println("Warning: '" + mode +
            //                   "' is an unknown Trace mode.");
            error = true;
        }
        
    }

    public void addedToWeakCell (RtWeakCell cell) {
        myWeakCell = cell;
    }
    
    public void removedFromWeakCell (RtWeakCell cell) {
        myWeakCell = null;
    }
    
    public void finalize () {
        synchronized (traces) {
            if (myWeakCell != null) {
                traces.remove(myCaller);
            }
        }
    }

    public String classNameToEncode (RtEncoder encoder) {
        return null;
    }

    public void encode (RtEncoder encoder) {
        System.out.println("Warning, trace object being encoded!");
        try {
            encoder.writeUTF(myCaller);
        } catch (Exception e) {
        }
    }

    public Object decode (RtDecoder decoder) {
        System.out.println("Warning, trace object being decoded!");
        String tag = "???";
        try {
            tag = decoder.readUTF();
        } catch (Exception e) {
        }
        return new Trace(false, tag);
    }

    String callingClass () {
            try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes);
        Exception e = new Exception();
        e.printStackTrace(stream);
        String trace = bytes.toString();
        int start = trace.indexOf('\n') + 1;
        start = trace.indexOf('\n', start) + 1;
        start = trace.indexOf('\n', start) + 1;
        int end = trace.indexOf('\n', start);
        start = trace.indexOf('a', start);
        end = trace.indexOf('(', start);
        end = trace.lastIndexOf('.', end);
        if (trace.startsWith("_$_Impl", end-7)) {
            end -= 7;
        }
        return trace.substring(start + 3, end);
            } catch (Exception e) {
                return "<Cannot determine class>";
            }
    }

    public static String arrayToString(Object a[], String name, String pre, String sep, String post) {
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

    public synchronized static String getStackTrace () {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes);
        Exception e = new Exception();
        e.printStackTrace(stream);
        String trace = bytes.toString();
        int start = trace.indexOf('\n') + 1;
        start = trace.indexOf('\n', start) + 1;
        return trace.substring(start);
    }

  private synchronized void recordTraceMessage(String message) 
  {
    StackFrameData frameData = new StackFrameData(3);
    
    synchronized (traces) {
      myPrintStream.print(myCaller + "(");
      myPrintStream.print(frameData.methodName);
      myPrintStream.print(":");
      myPrintStream.print(frameData.fileName);
      myPrintStream.print(":" + frameData.lineNumber);
      myPrintStream.println(") " + message);
    }
  }
  

  /* For the moment, all these methods are synonyms.  That will change. */

    public void $(String message) {
      recordTraceMessage(message);
    }

    public void verbosem(String message) {
      recordTraceMessage(message);
    }

    public void debugm(String message) {
      recordTraceMessage(message);
    }

    public void eventm(String message) {
      recordTraceMessage(message);
    }

    public void usagem(String message) {
      recordTraceMessage(message);
    }

    public void worldm(String message) {
      recordTraceMessage(message);
    }

    public void warningm(String message) {
      recordTraceMessage(message);
    }

    public void errorm(String message) {
      recordTraceMessage(message);
    }

    public static String eclassString(EObject_$_Intf obj) {
        if ((Object) obj == null)
            return("<null>");
        String name = ((Object)obj).getClass().getName();
        int dot = name.lastIndexOf(".");
        if (dot >= 0)
            name = name.substring(dot + 1);

        if (name.endsWith("_$_Impl"))
            name = name.substring(0, name.length() - 7) + "/i";
        else if (name.endsWith("_$_Proxy"))
            name = name.substring(0, name.length() - 8) + "/p";
        else if (name.endsWith("_$_Channel"))
            name = name.substring(0, name.length() - 10) + "/c";
        return(name);
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
      * event.  As shreded exceptions will likely be symptoms of
      * bugs, one will XXX be able to have them traced.
      */
    // depricated, use verboseReportException instead
    public void shred(Throwable ex, String reason) {
        verboseReportException(ex, reason);
    }
}

