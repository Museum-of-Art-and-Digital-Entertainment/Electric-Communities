package ec.e.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import ec.e.start.Vat;

public class EStdio {

    static {
        throw new ExceptionInInitializerError("Loaded dummy EStdio class");
    }

    public static void initialize(Vat v) {
        throw new Error("Bogus");
    }

    public static DataInputStream in() {
        throw new Error("Bogus");
    }

    public static PrintStream err() {
        throw new Error("Bogus");
    }

    public static PrintStream out() {
        throw new Error("Bogus");
    }

    public static void reportException(Throwable t) {
        throw new Error("Bogus");
    }
}
