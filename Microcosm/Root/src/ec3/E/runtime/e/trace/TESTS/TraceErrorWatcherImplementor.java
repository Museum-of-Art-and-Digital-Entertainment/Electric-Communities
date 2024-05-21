
package ec.e.run.test;

import ec.e.run.TraceErrorWatcher;
import ec.e.run.Trace;

public class TraceErrorWatcherImplementor implements TraceErrorWatcher {

    String myName;

    public TraceErrorWatcherImplementor(String aName) {
        myName = aName;
    }

    public void register() {
        TraceController.errorWatcher(this, true);
    }

    public void unregister() {
        TraceController.errorWatcher(this, false);
    }
        
    public void notifyFatal() {
        Trace.trace.errorm("notifyFatal called: ", myName);
        TraceController.dumpBufferToLog();
    }

    public void notifyOptional() {
        Trace.trace.errorm("notifyOptional called: ", myName);
        TraceController.dumpBufferToLog();
    }

    public String toString() {
        return "Watcher " + myName;
    }
}
