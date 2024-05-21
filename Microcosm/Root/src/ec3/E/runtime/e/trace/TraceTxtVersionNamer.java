/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.File;
import ec.util.assertion.*;

/**
 * This class and its subclasses know how to construct backup version
 * Files for files that end in ".txt".
 */

class TraceTxtVersionNamer extends TraceVersionNamer
        implements TraceConstants { 

    /** The length of the ".txt" extension, for convenience. */
    static private final int extlen = LOG_EXTENSION.length();

    TraceTxtVersionNamer(File aFile) {
        super(aFile);
    }

    protected String fetchBasename() {
        // Showing that the trailing "." is part of the basename, for clarity.
        return myName.substring(0, myName.length() - extlen) + ".";
    }

    protected boolean mightHaveSeq(String filename) {
        int minlen = myBasename.length() + 1 + extlen;
            return filename.length() >= minlen &&
                   filename.toLowerCase().startsWith(myBasename.toLowerCase()) &&
                   filename.toLowerCase().endsWith(LOG_EXTENSION);
    }
        
    protected int getSeq(String filename) {
            String possibleSeqString =
                filename.substring(0, filename.length() - extlen).
                         substring(myBasename.length());

        try { 
            return Integer.parseInt(possibleSeqString);
        } catch (NumberFormatException e) {
            Trace.trace.shred(e, filename + " is not a backup file.");
            return -1; 
        }
    }
        
    protected File constructVersion(int sequence) {
            return new File(myDir, myBasename + sequence + LOG_EXTENSION);
    }
}
