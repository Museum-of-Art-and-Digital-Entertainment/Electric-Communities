/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import ec.util.assertion.*;

/** 
 * This class describes a trace message.  Its final destination could
 * be an on disk log (called "the log"), or an in-core buffer (called
 * "the buffer") to be retained until the user asks for it.
 */
class TraceMessage {

    // DANGER                                                   DANGER
    // If you add new data members, please note that TraceBuffer.java 
    // constructs its own private TraceMessages (in unpostedTraceMessage).
    // That code may also need to be updated.
    // DANGER                                                   DANGER

    /** The subsystem is the larger body of code the message applies to. */
    public String subsystem;
    
    /** The class that sent the trace message. */
    public String classname;

    /** The method that sent the trace message. */
    public String methodname;

    /** 
     * Filename in which that method lives.  Note that it is not the
     * full pathname.  The JVM spec doesn't allow full pathnames to
     * be stored in the class file.
     */
    public String filename;

    /** Line number within that file. */
    public String line;

    /**
     * The Date at which the method was sent (approximately).  The Date
     * is not attached to the message until the message leaves the vat.
     * (The vat does not let you find out the current time.)
     */
    public TraceDate date;

    /**
     * The level ("error", "debug", etc.) at which the message was
     * sent. This is distinct from the priority threshold that
     * determines whether the message should be sent.
     */
    public int level;

    /** The text itself. */
    public String message;

    /**
     * An arbitrary object may be attached to the message.
     * They are usually printed with toString(), but Throwables
     * are handled specially.
     */
    public Object object;

    /** The next TraceMessage in a linked list. */
    public TraceMessage next;

    TraceMessage(StackFrameData frameData) {
        if (frameData != null) { 
            classname = frameData.className;
            methodname = frameData.methodName;
            filename = frameData.fileName;
            line = frameData.lineNumber;
        } else {
            classname = "class?";
            methodname = "method?";
            filename = "file?";
            line = "line?";
        }
    }

    /**
     * This version of the constructor does not decompose the stack.
     */
    TraceMessage() {
        classname = "class?";
        methodname = "method?";
        filename = "file?";
        line = "line?";
    }

    /**
     * Default string representation displays all fields. 
     */
    public String toString() {
        return new TraceMessageStringifier().toString(this);
    }

}
