/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */


package ec.e.run;

/**
 * The interface for any object that wishes to be informed when a program
 * error has been logged.  To register, the caller should call:
 * <p>
 * TraceController.errorWatcher(this, true);
 * <p>
 * To stop being informed, call:
 * <p>
 * TraceController.errorWatcher(this, false);
 */
public interface TraceErrorWatcher {

    /**
     * Notify a user that a fatal error has happened.  Tell her how to
     * report the bug.  Does not return.
     */
    public void notifyFatal();

    /**
     * If the user wants to hear about nonfatal bugs, notify her.
     * Does return.
     */
    public void notifyOptional();
}



