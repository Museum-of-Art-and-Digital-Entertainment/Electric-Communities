/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import ec.util.assertion.*;
import ec.e.run.TraceDate;
import ec.e.run.TraceMessage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import ec.util.EThreadGroup;

/**
 * This class is used to convert a trace message into a string.  It
 * is used (as opposed to toString()) when certain of the fields need
 * to be omitted.
 * <p>
 * Future:  IFC might allow the objects contained in trace messages to
 * be displayed as special glyphs, even clickable buttons.
 * <p>
 * By default, everything is shown.
 */
class TraceMessageStringifier {
    // See the setter methods for the meanings of these booleans.
    private boolean myShowDate = true;
    private boolean myShowTime = true;
    private boolean myShowLocation = true;
    private boolean myShowSubsystem = true;
    private boolean myShowLevel = true;
    private String lineSeparator = System.getProperty("line.separator");

    /**
     * Should the message include the date at which the
     * message was created?
     */
    protected void showDate(boolean value) {
        myShowDate = value;
    }

    /**
     * Should the message include the time at which the
     * message was created?
     */
    protected void showTime(boolean value) {
        myShowTime = value;
    }

    /**
     * Should the message include the method name, file name, and
     * line number?
     */
    protected void showLocation(boolean value) {
        myShowLocation = value;
    }

    /**
     * Should the message include the subsystem name?
     */
    protected void showSubsystem(boolean value) {
        myShowSubsystem = value;
    }

    /**
     * Should the message include an abbreviated description of
     * the level at which the message was posted?
     */
    protected void showLevel(boolean value) {
        myShowLevel = value;
    }

    // A minor concession to efficiency, as this is critical path.
    // Used in toString.
    private StringBuffer buffer = new StringBuffer(200);

    /** 
     * Convert the given message into a string, obeying 'show'
     * controls set earlier.
     */
    protected String toString(TraceMessage message) {
        buffer.setLength(0);
        buffer.append("=== ");
        if (myShowDate) {
            buffer.append(message.date.dateString());
            buffer.append(' ');
        } 
        if (myShowTime) { 
            buffer.append(message.date.timeString());
            buffer.append(' ');
        } 
        if (myShowLocation) { 
            buffer.append('(');
            buffer.append(message.methodname);
            buffer.append(':');
            buffer.append(message.filename);
            buffer.append(':');
            buffer.append(message.line);
            buffer.append(") ");
        }

        if (myShowLevel) {
            buffer.append(TraceLevelTranslator.terse(message.level));
        }
        
        // Experimenting with multi-line output.
        if (myShowDate || myShowTime || myShowLocation) {
            buffer.append(lineSeparator);
        }

        // note: subsystem must be delimited because it might
        // contain blanks.
        if (myShowSubsystem) {
            buffer.append(message.subsystem);
            buffer.append(": ");
        }
        buffer.append(message.message);

        if (message.object != null) {
            if (message.object instanceof Throwable) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                PrintStream stream = new PrintStream(bytes);
                EThreadGroup.printStackTrace((Throwable)message.object, stream);
                String stack = bytes.toString();
                buffer.append(lineSeparator);
                buffer.append(stack);
            } else {
                buffer.append(" : ");
                buffer.append(message.object);
            }
        }
        return buffer.toString();
    }
}
