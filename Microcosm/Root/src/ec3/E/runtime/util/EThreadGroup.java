package ec.util;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

public class EThreadGroup extends ThreadGroup
{
    static private ExceptionNoticer myNoticer = null;
    
    private EThreadGroup(String name) {
        super(name);
    }
    
    public static void callEMain(String mainClassName, String args[]) {
        try {
            EThreadGroup newThreadGroup = new EThreadGroup("EMainThreadGroup");
            new EMainThread(newThreadGroup, mainClassName, args).start();
        }
        catch (Throwable t) {
            EThreadGroup.reportException(t);
        }
    }

    public static void setExceptionNoticer(ExceptionNoticer noticer) {
        if (myNoticer != null) {
            throw new SecurityException("cannot reset EThreadGroup.ExceptionNoticer");
        }
        myNoticer = noticer;
    }
    
    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof ThreadDeath)) {
            EThreadGroup.reportException(e, "Uncaught exception in thread " + t.getName(), true);
        }
    }

    public static void reportException(Throwable t) {
        EThreadGroup.reportException(t, "", false);
    }

    public static void reportException(Throwable t, String msg) {
        EThreadGroup.reportException(t, msg, false);
    }

    public static void reportException(Throwable t, boolean nonLocal) {
        EThreadGroup.reportException(t, "", nonLocal);
    }

    public static void reportException(Throwable t, String msg, boolean nonLocal) {
        if (myNoticer != null) {
            if (nonLocal) {
                t = new ReportedByException("", t);
            }
            myNoticer.noticeReportedException(msg, t);
        }
        else {
            System.err.println(msg);
            EThreadGroup.printStackTrace(t, System.err, nonLocal);
        }
    }

    public static void printStackTrace(Throwable t) {
        EThreadGroup.printStackTrace(t, System.err, false);
    }
    
    public static void printStackTrace(Throwable t, PrintStream out) {
        EThreadGroup.printStackTrace(t, out, false);
    }
    
    public static void printStackTrace(Throwable t, PrintStream out, boolean nonLocal) {
        out.println("--vvvv--");
        while (t != null) {
            Humanity.humanPrintStackTrace(t, out);
            if (t instanceof NestedThrowable) {
                t = ((NestedThrowable)t).getNestedThrowable();
            } else if (t instanceof ExceptionInInitializerError) {
                t = ((ExceptionInInitializerError)t).getException();
            } else if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException)t).getTargetException();
            } else if (t instanceof NestedThrowableVector) {
                NestedThrowableVector ntv = (NestedThrowableVector)t;
                int size = ntv.size();
                for (int i=0; i<size; i++) {
                    out.println(i + ": " + ntv.getLabel(i));
                    printStackTrace(ntv.getThrowable(i), out, false);
                }
                t = null;
            }
            else if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException)t).getTargetException();
            }
            else {
                t = null;
            }
        }
        if (nonLocal) {
            Humanity.humanPrintStackTrace(new ReportedByException("", null),
                out);
        }
        out.println("--^^^^--");
    }
}

class ReportedByException extends NestedException 
{
    ReportedByException(String msg, Throwable t) { super(msg, t); }
}
