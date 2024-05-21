/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.*;

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
 * <p>
 * Stack frames from jit-ed code look like:
 *  at Trace.$(Compiled Code)
 */
public class StackFrameData
{
  /**
   * The name of the method running in the targeted frame.
   * It is partly qualified, consisting of the last element of the
   * classname plus the method name (for example, "String.indexOf").
   */
  protected String methodName = "method?";

  /**
   * The name of the class that method belongs to.
   * Fully qualified.
   */ 
  protected String className = "class?";
 
  /** The file that method is in.  The full pathname is not available. */
  protected String fileName = "file?";
  
  /** The line number the trace call is on. */
  protected String lineNumber = "line?";

  /** 
   * Collect an earlier frame's data.
   * If aboveCount is 1, the data from the caller of this constructor
   * is collected. 2 means the frame above that, and so on.
   * <P>
   * Leaves fields set to their to original "?" values
   * if it can't parse the stack.
   * <P>
   * XXX: TODO:  E and Pluribus demangling. <br>
   * XXX: Would it be a good idea to dump the whole stack frame (once)
   * if the stack can't be parsed?
   * <p>
   * Note that this routine is a big time sink, because it has to 
   * create an exception, write the stack to a stream, then 
   * parse out the appropriate frame.  In the current version of the trace
   * system, it's likely that most messages will be stored to an
   * internal buffer and silently discarded.  In that case, it would
   * probably be a good idea to only create the exception, parsing it
   * as needed.  (Idea due to Mark Miller.)
   */
  StackFrameData(int aboveCount)
    {
        try {
            // I could make this static, but then I'd have to worry
            // about multiple threads.
            CharArrayWriter charSink = new CharArrayWriter(100);
            PrintWriter writer = new PrintWriter(charSink);
            Exception stupid = new Exception();
            stupid.printStackTrace(writer);

            String trace = charSink.toString();
            // System.out.println(trace);

            int start = trace.indexOf('\n') + 1;   // skip "java.lang.Exception"
            start = trace.indexOf((this.getClass()).getName(), start) + 1;  // skip this routine.
            if (start<0) throw stupid;  // unexpected format: just bail out of here without an error
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

            if (colon > 0) {
                // normal bytecodes.  
                fileName = trace.substring(openParen + 1, colon);
                lineNumber = trace.substring(colon + 1, closeParen);
            }
            // else jit-compiled:  leave fileName and lineNumber
            // with question marks.

        } catch (Throwable e) {
            // Most likely, the stack frame format has changed in an
            // unexpected way.  Leave what hasn't been set yet with
            // question marks.
        }
        /*
          System.out.println("FINISHED:\n" +
          " class = " + className +
          " method = " + methodName +
          " file = " + fileName +
          " lineNumber = " + lineNumber);
          */
    }

    /**
     * Synchronized static methods are prone to deadlocks in Sun's
     * JVM.  This avoids the problem.
     */
    private static Object synchronizationObject = new Object();

    /**
     * Return the current stack trace above this routine as a string.
     * Deprecated.
     */
    public static String getStackTrace () {
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

}

  
