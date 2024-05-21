package ec.e.io.crew;

import java.io.InputStream;
import java.io.OutputStream;
import ec.e.start.Tether;

public class RtConsole implements Runnable {

    static {
        throw new ExceptionInInitializerError("Loaded dummy RtConsole class");
    }

    static public void setupConsoleReader(Tether handlerHolder,
                                          InputStream in, OutputStream out) {
        throw new Error("Bogus");
    }

    public void run() {
        throw new Error("Bogus");
    }
}
