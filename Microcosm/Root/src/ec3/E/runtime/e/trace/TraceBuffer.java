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
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Class that controls storing of TraceMessages in core, for later
 * or concurrent display.
 *
 * The tracebuffer is a linked list of trace messages.  That linked
 * list may be a subset of a larger list.  Just because an old message
 * falls off the end of this list does NOT mean it should be destroyed.
 * Someone else (e.g., the inspector) might be using it.
 */

class TraceBuffer implements TraceMessageAcceptor, TraceConstants {
    /** The first message in the buffer. */
    TraceMessage start;

    /** The last message in the buffer. */
    TraceMessage end;

    /** The max number of messages between start and end (inclusive). */
    int maxSize = STARTING_TRACE_BUFFER_SIZE;

    /** The current number. */
    int currentSize;

    TraceBuffer() {
        // DANGER:  This constructor must be called as part of static
        // initialization of TraceController.  Until that
        // initialization is done, Trace should not be loaded.
        // Therefore, nothing in this constructor should directly or
        // indirectly use a tracing function. 

        // Doing this message initialization this way is pretty icky,
        // but we can't actually *post* this message because 
        // the trace system doesn't fully exist yet.
        TraceMessage message = unpostedTraceMessage("Transient buffer begins.",
                                                    WORLD);

        start = message;
        end = message;
        currentSize = 1;
    }

    // This class does nothing differently before or after setup.
    public void setupIsComplete() {
    }

    // Note:  pretty much duplicates TraceLog.changeSize.
    protected synchronized void changeSize(String value) {
        int newSize;
        if (value.equalsIgnoreCase(DEFAULT_NAME)) {
            newSize = STARTING_TRACE_BUFFER_SIZE; 
        } else if (value.equalsIgnoreCase(UNLIMITED_NAME)) {
            newSize = Integer.MAX_VALUE; 
        } else try { 
            newSize = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Trace.trace.errorm(
                "Buffer size cannot be changed to illegal value '" +
                value + "'.");
            newSize = maxSize;  // leave unchanged.
        }

        if (newSize < 1) {
            Trace.trace.errorm(
                value + " is too small a threshold size for the log. "
                + "Ignoring.");
            newSize = maxSize;
        }
              
        maxSize = newSize;
        while (currentSize > maxSize) {
            start = start.next;
            currentSize--;
        }
    }

    public synchronized void accept(TraceMessage message) {
        Assertion.test(start != null);
        Assertion.test(end != null);
        Assertion.test(message != null);

        currentSize++;
        end.next = message;
        end = message;

        // In theory, this loop should only ever execute once.
        while (currentSize > maxSize) {
            start = start.next;
            currentSize--;
        } 

    }

    // Note:  because messages are never thrown away, 
    // except by GC, it is safe for this not to be synchronized.
    protected TraceMessage getFirstMessage() {
        Assertion.test(start != null);
        return start;
    }

    /**
     * Dump the buffer to a file.  This is a quasi-temporary function.
     * It would be better (probably) if the buffer were dumped to a 
     * window, which could then be dumped to a file.  TraceDisplay.java
     * is an early attempt at that, but foundered on lack of time and 
     * dependencies on classes that aren't built until long after trace.
     */
    protected synchronized void dump(String destination) {
        PrintStream stream;

        // outputDestination is destination, except when 
        // destination is "-", signifying stdout.  In that
        // case, it's "standard output".  Why not just pass in 
        // "standard output"?  Because the user elsewhere uses 
        // "-" to mean standard output, and might choose to use
        // it again here.  She will be unhappy if a file named
        // "-" is created.
        String outputDestination = destination;
        if (destination.equals("-")) {
            stream = System.out;
            outputDestination = "standard output";
        } else {
            try {
                stream = new PrintStream(new FileOutputStream(new File(destination)));
            } catch (SecurityException e) {
                Trace.trace.errorm(
                    "Security exception when opening dump file '" +
                    outputDestination + "'.");
                return;
            } catch (FileNotFoundException e) {
                Trace.trace.errorm(
                    "Could not open dump file '" +
                    outputDestination + "'.");
                return;
            } catch (IOException e) {
                Trace.trace.errorm(
                    "Unknown error when opening dump file '" +
                    outputDestination + "'.");
                return;
            }
        }

       Trace.trace.worldm("Dumping internal trace buffer to " +
                             outputDestination);

       TraceMessageStringifier stringifier = new TraceMessageStringifier();
       
       TraceMessage current = start;
       // The begin/end lines make the dump easier to see when it and the log are
       // going to the same place (typically stdout).
       stream.println("======================= BEGIN TRACE BUFFER DUMP =======================");
       while (current != null) {
           String output = stringifier.toString(current);
           stream.println(output);
           current = current.next;
       }    
       if (stream.checkError()) {
           Trace.trace.errorm("Could not dump trace buffer to " + 
                                outputDestination);
       }
       stream.println("======================= END TRACE BUFFER DUMP =======================");
       if (stream != System.out) { 
           stream.close();
       }
    }


    /**
     * Dump the buffer to a TraceMessageAcceptor.
     */
    protected synchronized void dump(TraceMessageAcceptor acceptor) {
       Trace.trace.worldm("Dumping internal trace buffer to " +
                             acceptor);

       // We want to demarcate the dumped trace in the new log, but
       // not have the message end up in this log, which could be
       // confusing. 

       TraceMessage traceMessage = unpostedTraceMessage("======================= BEGIN INTERNAL TRACE BUFFER DUMP =======================",
                                                        ERROR);
       acceptor.accept(traceMessage);

       TraceMessage current = start;
       while (current != null) {
           acceptor.accept(current);
           current = current.next;
       }    
       traceMessage = unpostedTraceMessage("======================= END INTERNAL TRACE BUFFER DUMP =======================",
                                           ERROR);
       acceptor.accept(traceMessage);
    }

    /**
     * Create a trace message that isn't posted to all the message
     * acceptors.  Used for messages that have one specific
     * destination.
     */
    private TraceMessage unpostedTraceMessage(String message, int level) {
        TraceMessage tm = new TraceMessage(new StackFrameData(2));
        tm.message = message;
        tm.date = new TraceDate();
        tm.object = null;
        tm.level = level;
        tm.subsystem = "trace";
        return tm;
    }
}
 
