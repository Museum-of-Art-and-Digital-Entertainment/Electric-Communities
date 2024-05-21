package ec.e.io.crew;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import ec.e.start.Tether;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.start.SmashedException;
import ec.e.io.EInputHandler;
import ec.e.io.EConsoleMaker;

/* This is a CREW class. */


public class EConsoleMakerMaker implements MagicPowerMaker {

    public Object make(EEnvironment eEnv) {
        return new EConsoleMaker(eEnv.vat());
    }
}

/**
  Implements a seperate thread to manage the user console (i.e., the keyboard
  and tty). This needs to be in its own thread so that the user pondering
  what to type next doesn't block the entire E runQ! <p>

  The console is given an input handler when it is created, which is an EObject
  that implements the EInputHandler einterface. Each line of input typed on the
  console is sent as a String in an E-message to the input handler. End of file
  or any other error or exception condition is signalled to the input handler
  by sending it a null. XXX The null is probably a bad way to handle such
  conditions, but we can't do an ethrow from an external thread such as this.
*/
public class RtConsole implements Runnable {
    static private Trace tr = new Trace("ec.e.io.RtConsole");
    private Tether myHandlerHolder;
    private DataInputStream myIn;
    private PrintStream myOut;
    private boolean nowait;
    
    /**
     * Constructs a new console object given a handler and the console input
     * and output. Note that this only constructs the console object; it does
     * not start the console thread running!
     *
     * @param handlerHolder A Tether to the E object that is to receive input
     *  lines.
     * @param in The console input source.
     * @param out The console output sink
     */
    private RtConsole(Tether handlerHolder, InputStream in,
                      OutputStream out) {
        if (in == null) {
            return;
        }
        if (in instanceof DataInputStream) {
            myIn = (DataInputStream) in;
            /* XXX Prior to cleanup, 'nowait' was uninitialized at this point.
               This would make the default taken here be false, which is
               harmless in terms of correctness but very wasteful. I think it
               was false by default by mistake, so I'm setting it to true here,
               but maybe it really *should* be false, so I'm putting this
               comment here as a flag that we really should check on this! */
        } else {
            myIn = new DataInputStream(in);
        }
        if (in instanceof FileInputStream) {
            nowait = true;
        } else {
            /* On Solaris, need to check available() before reading or
               else the whole process will block! */
            String osname = System.getProperty("os.name");
            if (osname.equals("Solaris"))
                nowait = false;
            else
                nowait = true;
        }
        myHandlerHolder = handlerHolder;
        if (out == null || (out instanceof PrintStream))
            myOut = (PrintStream)out;
        else
            myOut = new PrintStream(out);
    }
    
    /**
     * Set up a console on a given input with no output. Don't start it up.
     *
     * @param handlerHolder Tether to the E object that is to receive input
     *  lines.
     * @param in The console input source.
     */
    static public void setupConsoleReader(Tether handlerHolder,
                                          InputStream in) {
        setupConsoleReader(handlerHolder, in, null);
    }
    
    /**
     * Set up a console with a given input and output, then start the thread.
     *
     * @param handlerHolder Tether to the E object that is to receive input
     *  lines.
     * @param in The console input source.
     * @param out The console output sink
     */
    static public void setupConsoleReader(Tether handlerHolder,
                                          InputStream in, OutputStream out) {
        RtConsole newConsole = new RtConsole(handlerHolder, in, out);
        Thread consoleThread = new Thread((Runnable) newConsole);
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /**
     * This is the actual thread code for the RtConsole.
     */
    public void run () {
        if (tr.debug && Trace.ON) tr.$("Entering run loop");
        /*
          Read lines from myIn in a loop, only exiting on error or EOF. <p>

          Lines are echoed on myOut if myOut is not null, and then sent to
          the EObject held by myHandlerHolder in an E-message.  Error or EOF is
          signalled to the handler by sending it null instead of a line, which,
          *sigh*, appears to be the way DataInputStream.readLine() handles
          EOF. <p>

          If the constructor had determined that myIn is something that can
          block the entire process and not just this thread (which,
          appallingly, can actually happen due to a Solaris bug), we poll
          every 200ms rather than just blindly reading.
        */
        try {
            while (true) {
                if (nowait || (myIn.available() > 0)) {
                    String line = myIn.readLine();
                    if (tr.debug && Trace.ON) tr.$("read: " + line);
                    if (myOut != null)
                        myOut.println(line);
                    sendLineToHandler(line);
                    if (line == null)
                        return;
                    else
                        Thread.yield();
                } else {
                    synchronized (this) {
                        try {
                            if (tr.verbose && Trace.ON) tr.$("waiting");
                            wait(200);
                        } catch (Exception e) {
                            /* If we're tickled, just keep going */
                        }
                    }
                }
            }
        } catch (Exception ex) {
            tr.errorReportException(ex, "caught exception, exiting Console run loop");
            /* XXX Arguably we should do something smarter here. */
            sendLineToHandler(null);
        }           
    }

    /**
     * Internal routine to safely send line to handler.
     */
    private void sendLineToHandler(String line) {
        synchronized (myHandlerHolder.vat().vatLock()) {
            try {
                if (tr.debug && Trace.ON) tr.$("sending to handler: " + line);
                ((EInputHandler) myHandlerHolder.held()) <- handleInput(line);
            } catch (SmashedException ex) {
                throw new RuntimeException("smashed " + ex);
            }
        }
    }
}
