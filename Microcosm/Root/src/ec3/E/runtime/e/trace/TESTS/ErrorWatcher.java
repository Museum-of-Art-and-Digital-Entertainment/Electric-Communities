
package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import ec.e.run.TraceErrorWatcher;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Basic smoke test.  See =README=
 */
class ErrorWatcher
{
    public static void main (String[] args) {
        Properties props = new Properties();
        props.put("TraceLog_name","ErrorWatcher.txt");
        props.put("TraceLog_write", "true");
        TraceController.start(props);
        
        TraceErrorWatcherImplementor watcher1 = new TraceErrorWatcherImplementor("first");
        TraceErrorWatcherImplementor watcher2 = new TraceErrorWatcherImplementor("second");

        if (args[0].equals("one")) {
            // Test some unusual registration cases
            watcher1.unregister();  // warning in the log - nothing registered.
            watcher1.register();
            watcher2.register();    // warning in the log - duplicate
                                    // register.
            watcher1.unregister();  // someone else unregisters.
        } else if (args[0].equals("three")) {
            // notifyOptional() before any watcher registered. 
            Trace.trace.notifyOptional();
        } else if (args[0].equals("four")) {
            // notifyfatal() before any watcher registered. 
            Trace.ui.notifyFatal();
        }
        watcher2.register();
        
        Trace.trace.usagem("A usage message (expected in buffer only).");
        Trace.trace.errorm("Here is an error (in both trace acceptors).");

        if (args[0].equals("one")) {
            Trace.trace.errorm("An error, followed by Trace.trace.notifyOptional");
            Trace.trace.notifyOptional();
        } else if (args[0].equals("two")) {
            Trace.trace.errorm("An error, followed by Trace.trace.notifyFatal");
            Trace.trace.notifyFatal();
        } else if (args[0].equals("three")) {
            Trace.trace.errorm("An error, followed by TraceController.notifyOptional");
            Trace.trace.notifyOptional();
            Trace.trace.errorm("DO IT AGAIN, FOR FUN.");
            Trace.trace.notifyOptional();
        } else if (args[0].equals("four")) {
            Trace.trace.errorm("An error, followed by TraceController.notifyFatal");
            Trace.trace.notifyFatal();
        }
        Trace.trace.worldm("That's all, folks - end of test.");
    }
}

