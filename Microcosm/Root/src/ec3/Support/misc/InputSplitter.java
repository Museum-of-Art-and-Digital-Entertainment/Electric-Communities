package ec.misc;

import ec.e.run.Trace;
import ec.ifc.app.ECApplication;
import ec.e.io.EInputHandler;

/**
 * InputSplitter allows multiple EInputHandlers to be connected to a single
 * input. First you give the input the input splitter. Then you register your
 * input handlers with the input splitter, giving each a prefix string.
 *
 * As input comes in the splitter matches and strips off any prefix, then hands
 * the rest of the input to the appropriate input handler.
 *
 * There is a single default input handler which gets all input which does not
 * have a recognizable prefix.
 *
 * InputSplitters are useful as the input handler for a console window, where
 * you often want the input to be split among different objects.
 *
 * You can test this class with:
 * "java ec.ui.InputSplitterTester"
 */
public eclass InputSplitter implements EInputHandler {

    /** Tracing */
    private static final Trace TheTrace = new Trace("inputsplitter");

    /** Table of input handlers */
    private java.util.Hashtable iInputHandlers = new java.util.Hashtable();


    /**
     * Constructor, creates empty input splitter. Any input will be dropped
     * on the ground until input handlers are added.
     */
    public InputSplitter() {
    }


    /**
     * Add handler for all input with the given prefix. If 'prefix' is already
     * in use the old handler for that prefix is lost. 'prefix' cannot be null.
     */
    emethod addInputHandler(String prefix, EInputHandler handler) {
        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("Adding input handler for " + prefix +
                            " to " + this + "; handler is " + handler);
        }
        iInputHandlers.put(prefix, handler);
    }


    /**
     * Add handler for all input that is not handled by other input handlers.
     * There can only be one default input handler.
     * This call is equivalent to add input handler with a null string as its
     * prefix argument.
     */
    emethod addDefaultInputHandler(EInputHandler handler) {
        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("Adding default handler to " + this +
                            "; handler is " + handler);
        }
        addInputHandler("", handler);
    }


    /**
     * Remove input handler for given prefix; both prefix and handler
     * must match the corresponding call to 'addInputHandler'
     */
    emethod removeInputHandler(String prefix, EInputHandler handler) {
        // FRF & TJM Changed to ignore requests if old handler doesn't
        // match this one XXX reconsider garbage collection ramifications.
        EInputHandler old = (EInputHandler)iInputHandlers.get(prefix);
        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("Removing handler for " + prefix + 
                            " from " + this +
                            "; handler is " + handler);
        }
        if (old == handler) {
            iInputHandlers.remove(prefix);
        }
        else {
            if (TheTrace.debug && Trace.ON) {
                TheTrace.debugm("Handler didn't match old handler: " + old);
            }
        }
    }


    /**
     * Remove the default input handler
     */
    emethod removeDefaultInputHandler(EInputHandler handler) {
        removeInputHandler("", handler);
    }


    /**
     * Responsibility from EInputHandler - handle a line of input
     */
    emethod handleInput(String line) {

        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("Handle input line: '" + line +
                            "', handler table is " + iInputHandlers);
        }

        // Ignore null lines
        if (line == null) {
            return;
        }

        // Split line at first space
        String fullLine = line.trim();
        String startOfLine = fullLine;
        String endOfLine = "";

        int length = startOfLine.length();
        for (int index = 0; index < length; index++) {
            if (Character.isSpace(startOfLine.charAt(index))) {
                endOfLine = startOfLine.substring(index).trim();
                startOfLine = startOfLine.substring(0, index);
                break;
            }
        }

        // Try to find handler for the given start of line
        EInputHandler handler = (EInputHandler)iInputHandlers.get(startOfLine);
        if (handler == null) {
            // No handler, back off default
            if (TheTrace.debug && Trace.ON) {
                TheTrace.debugm("No handler found, using default");
            }
            handler = (EInputHandler)iInputHandlers.get("");
            endOfLine = fullLine;
        }

        // If we have a handler, dispatch the string
        if (handler != null) {
            if (TheTrace.debug && Trace.ON) {
                TheTrace.debugm("Found handler " + handler +
                                ", sending endOfLine '" + endOfLine + "'");
            }
            handler <- handleInput(endOfLine);
        } else {
            if (TheTrace.debug && Trace.ON) {
                TheTrace.debugm("No handler found");
            }
        }
    }

}

