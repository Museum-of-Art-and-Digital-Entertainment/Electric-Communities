package ec.e.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.util.EThreadGroup;

/**
 * A convenient holder for the standard I/O pathways that, outside the Vat, are
 * accessed off of the System class. However, inside the Vat we want to be a
 * little more careful. Fortunately, we can treat the System class as a sturdy
 * holder for these; all we need to do is wrap it appropriately, and that is
 * what this class does.
 */
public class EStdio {
    private static DataInputStream theIn;
    private static PrintStream theErr;
    private static PrintStream theOut;
    private static boolean initialized = false;

    /**
     * Initializes the standard I/O variables. This should only be
     * called once.
     */
    public static void initialize(Vat vat) throws IOException {
        if (initialized) {
            throw new Error("EStdio should only be init'ed once");
        }
        theIn = new DataInputStream(new EInputStream(new EStdinHolder(vat)));
        theErr = new PrintStream(new EOutputStream(new EStderrHolder(vat)));
        theOut = new PrintStream(new EOutputStream(new EStdoutHolder(vat)));
        initialized = true;
    }

    /**
     * Return study stdin.
     */
    public static DataInputStream in() {
        return theIn;
    }

    /**
     * Return sturdy stderr.
     */
    public static PrintStream err() {
        return theErr;
    }

    /**
     * Return sturdy stdout.
     */
    public static PrintStream out() {
        return theOut;
    }

    /**
     * Provide an exception report to the user.
     */
    public static void reportException(Throwable t) {
        EThreadGroup.reportException(t, false);
    }

    /**
     * Provide an exception report to the user.
     */
    public static void reportException(Throwable t, boolean nonLocal) {
        EThreadGroup.reportException(t, nonLocal);
    }
}

/**
 * Sturdy leaf to hold onto stdin. Reconstruct via System.in
 */
class EStdinHolder extends Tether {
    EStdinHolder(Vat vat) {
        super(vat, (Object)System.in);
    }

    protected Object reconstructed() {
        return System.in;
    }
}

/**
 * Sturdy leaf to hold onto stderr. Reconstructed via System.err
 */
class EStderrHolder extends Tether {
    EStderrHolder(Vat vat) {
        super(vat, (Object)System.err);
    }

    protected Object reconstructed() {
        return System.err;
    }
}

/**
 * Sturdy leaf to hold onto stdout. Reconstructed via System.out
 */
class EStdoutHolder extends Tether {
    EStdoutHolder(Vat vat) {
        super(vat, (Object)System.out);
    }

    protected Object reconstructed() {
        return System.out;
    }
}
