/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;
import ec.util.assertion.*;

/**
 * Translate numerical trace levels into strings and vice versa.
 */
class TraceLevelTranslator implements TraceConstants{

    /** 
     * Convert a string into one of the numeric trace levels.  Because
     * this method is used only to identify priority thresholds, it
     * does not return the TIMING pseudo-level.  (See TraceConstants.java.)
     * @exception IllegalArgumentException if the string is not recognized.
     */
    protected static int toInt(String level) throws IllegalArgumentException {
        if (level.equalsIgnoreCase("error")) {
            return ERROR;
        } else if (level.equalsIgnoreCase("warning")) {
            return WARNING;
        } else if (level.equalsIgnoreCase("world")) {
            return WORLD;
        } else if (level.equalsIgnoreCase("usage")) {
            return USAGE;
        } else if (level.equalsIgnoreCase("event")) {
            return EVENT;
        } else if (level.equalsIgnoreCase("debug")) {
            return DEBUG;
        } else if (level.equalsIgnoreCase("verbose")) {
            return VERBOSE;
        } else {
            String problem = "Incorrect tracing level '" + level + "'";
            Trace.trace.errorm(problem);
            throw new IllegalArgumentException(problem);
        }
    }

    /** 
     * Convert tracing thresholds into three-character synonyms.
     * Used when printing trace messages, so it does return the
     * "TIM" tag to denote a timing message.
     */
    protected static String terse(int level) {
        String retval;
        switch (level) {
            case ERROR:
                retval = "ERR";
                break;
            case WARNING:
                retval = "WRN";
                break;
            case WORLD:
                retval = "WLD";
                break;
            case USAGE:
                retval = "USE";
                break;
            case EVENT:
                retval = "EVN";
                break;
            case DEBUG:
                retval = "DBG"; 
                break;
            case VERBOSE:
                retval = "VRB";
                break;
            case TIMING:
                retval = "TIM";
                break;
            default:
                Assertion.fail("Left level out of TraceLevelTranslator.terse.");
                retval = "";    // silence compiler.
                break;
        }
        return retval;
    }
}
