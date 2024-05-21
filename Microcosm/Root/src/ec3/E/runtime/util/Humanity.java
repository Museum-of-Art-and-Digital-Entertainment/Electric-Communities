package ec.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Vector;

/**
 * This class collects a bunch of methods which are intended to
 * make computery output more human-friendly.
 */
public class Humanity
{
    private static final boolean DISABLE = false;

    /** No one can instantiate this class. It's all static methods. */
    private Humanity() {
    }

    static private final String deliverEcatch = "[delivery of ecatch]";
    static private final String deliverEwhen = "[delivery of ewhen]";
    static private final String synchForward = 
        "[synchronous E message forward]";
    static private final String eDelivery = "[E delivery]";

    /**
     * This is the official way to take a normal backtrace
     * string (all the lines beginning with "\tat") and turn
     * it into a human-readable form.
     */
    static public String humanizeBacktrace(String trace) {
        if (DISABLE) {
            return trace;
        }
        // Basic theory of operation: take each line and
        // see if it matches one of the known line-level
        // patterns. If so, replace it with a suitable form
        // and then do a final pass to smooth over the edges.
        Vector result = new Vector(35);
        int curpos = 0;
        int len = trace.length();
        String lastLine = null;
        while (curpos < len) {
            int lineEnd = trace.indexOf('\n', curpos);
            String line;
            if (lineEnd == -1) {
                line = trace.substring(curpos + 1);
            } else {
                line = trace.substring(curpos + 1, lineEnd);
            }
            curpos = lineEnd + 1;
            
            if (line.indexOf("$async(") != -1) {
                // $async call into e message send syntax
                int fileStart = line.lastIndexOf('(');
                String file = line.substring(fileStart);
                int lastDot = line.lastIndexOf('.', fileStart);
                String meth = line.substring(lastDot + 1, fileStart - 6);
                String cla = line.substring(3, lastDot);
                line = "at send of " + cla + "<-" + meth + file;
            } else if (line.indexOf(".closure$") != -1) {
                // closure use into clearer form (easy to spot number)
                int cloAt = line.indexOf(".closure$");
                int lastDlr = line.lastIndexOf("$");
                line = line.substring(0, cloAt) + ".closure #" +
                    line.substring(lastDlr + 1);
            } else if (line.indexOf(
                    "ECatchClosure_$_Impl.forwardException(") != -1) {
                // ecatch delivery into english
                line = deliverEcatch;
            } else if (
                (line.indexOf("EWhenClosure_$_Impl.forward(") != -1)
                || (line.indexOf("EObject_$_Impl.when(") != -1)) {
                // ewhen delivery into english
                line = deliverEwhen;
            } else if (line.indexOf("EDistributor_$_Impl.forward(") != -1) {
                // synchronous forward into english
                line = synchForward;
            } else if (line.indexOf("$closure.$apply(") != -1) {
                // ignore closure call-through; it'll be evident since
                // the next frame down is the closure method itself
                line = null;
            } else if (
                (line.indexOf("RtExceptionEnv.doEThrow(") != -1)
                || (line.indexOf("RtExceptionEnv.sendException(") != -1)
                || (line.indexOf("RtEnvelope.sendException(") != -1)) {
                // ethrow forms
                line = "[ethrow]";
            } else if (line.indexOf("java.lang.Thread.run(") != -1) {
                if (   (lastLine == eDelivery) 
                    || (lastLine == deliverEcatch)
                    || (lastLine == deliverEwhen)
                    || (lastLine == synchForward)) {
                    // don't bother if we're subsumed by an E delivery
                    line = null;
                } else {
                    line = "[thread start]";
                }
            } else if (
                   (line.indexOf("RtRun.run(") != -1)
                || (line.indexOf("RtQObj.run(") != -1)
                || (line.indexOf("RtCausality.doMessageWithCause(") != -1)
                || (line.indexOf("_$_Impl.invokeNow(") != -1)
                || (line.indexOf("_$_Impl.messageWithCause(") != -1)
                || (line.indexOf("_$_Sealer.invoke(") != -1)) {
                // the many varied forms of being in the middle of
                // E message delivery
                if (   (lastLine == deliverEcatch)
                    || (lastLine == deliverEwhen)
                    || (lastLine == synchForward)) {
                    // don't bother if we're subsumed by one of the
                    // explicit delivery types
                    line = null;
                } else {
                    line = eDelivery;
                }
            } else if (
                   (line.indexOf("RtRun.enqueue(") != -1)
                || (line.indexOf("RtRun.alwaysEnqueue(") != -1)
                || (line.indexOf("RtEnqueue.enq(") != -1)) {
                // queueing up an E message
                line = "[E queueing]";
            }
            
            // don't bother adding a duplicate or a null
            if ((line != null) && (line != lastLine)) {
                int pos = line.indexOf("_$_Impl");
                if (pos != -1) {
                    // clean off extra impls
                    line = line.substring(0, pos) + 
                        line.substring(pos + 7);
                }
                result.addElement(line);
                lastLine = line;
            }
        }

        // now collect it all into one big string
        StringBuffer resultBuf = new StringBuffer(len + 100);
        int sz = result.size();
        for (int i = 0; i < sz; i++) {
            resultBuf.append("    ");
            resultBuf.append(result.elementAt(i));
            resultBuf.append('\n');
        }

        return resultBuf.toString();
    }

    /** Given an exception, this hands back just the backtrace part,
     *  but in a human form. */
    static public String exceptionToHumanBacktrace(Throwable t) {
        // Original (by emm) note said: "should use
        // EThreadGroup.printStackTrace" but this is incorrect. It is
        // proper to use normal Throwable.printStackTrace, since what
        // we want is just to get the direct backtrace. -danfuzz
        ByteArrayOutputStream bs = new ByteArrayOutputStream(1000);
        t.printStackTrace(new PrintStream(bs));
        String trace = bs.toString();

        int firstAt = trace.indexOf("\n\tat");
        if (firstAt == -1) {
            // unknown stack trace format, nothing we can do
            return trace;
        }

        return humanizeBacktrace(trace.substring(firstAt + 1));
    }

    /**
     * This is the official way to take a normal Throwable
     * and turn it into a human-acceptable form.
     */
    static public String humanizeException(Throwable t) {
        // Original (by emm) note said: "should use
        // EThreadGroup.printStackTrace" but this is incorrect. It is
        // proper to use normal Throwable.printStackTrace, since what
        // we want is just to get the direct backtrace. -danfuzz
        ByteArrayOutputStream bs = new ByteArrayOutputStream(1000);
        t.printStackTrace(new PrintStream(bs));
        String trace = bs.toString();

        int firstAt = trace.indexOf("\n\tat");
        if (firstAt == -1) {
            // unknown stack trace format, nothing we can do
            return trace;
        }

        String prefix = trace.substring(0, firstAt + 1);
        trace = trace.substring(firstAt + 1);
        
        return prefix + humanizeBacktrace(trace);
    }

    /**
     * This is like printStackTrace, except it humanizes the exception.
     */
    static public void humanPrintStackTrace(Throwable t, PrintStream out) {
        out.print(humanizeException(t));
    }
}
