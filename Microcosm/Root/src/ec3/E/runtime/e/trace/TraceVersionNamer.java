/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.File;
import ec.util.assertion.*;
import java.util.Enumeration;

/**
 * This class and its subclasses know how to construct backup version
 * Files for given Files.
 * <p>
 * This class constructs backup names by appending sequence numbers to
 * the original name.
 */
class TraceVersionNamer implements TraceConstants { 
    /**
     * Return a TraceTxtVersionNamer if the file ends with ".txt";
     * otherwise, return a TraceVersionNamer.
     */
    protected static TraceVersionNamer factory(File file) {
        if (file.getName().toLowerCase().endsWith(LOG_EXTENSION)) {
            return new TraceTxtVersionNamer(file);
        } else {
            return new TraceVersionNamer(file);
        }
    }

    /** The file for which a version number is being created. */
    protected File myFile;

    /** The 'filename' part of the given file, sans directory. */
    protected String myName;

    /** A directory that contains the given file. */
    protected String myDir;

    /** 
     * The basename is that part of the file that precedes a sequence
     * number. 
     */
    protected String myBasename;

    /**
     * Create a TraceVersionNamer from the given file.  The file
     * object must have a directory and name component.  It may be absolute
     * or relative.  
     */
    TraceVersionNamer(File aFile) {
        myFile = aFile;
        myName = myFile.getName();
        Assertion.test(myName != null);

        myDir = myFile.getParent();
        Assertion.test(myDir != null);
        
        myBasename = fetchBasename();
        Trace.trace.debugm("Finding next version of " +
                          myFile + "(" + 
                          myDir + " " + myName +
                          " " + myBasename + ")");
    }


    /**
     * Return the next file in the sequence <foo>, <foo>.0, <foo>.1, etc.
     * Subclasses will have their own name-construction rules.
     */
    protected File nextAvailableVersion() {
        int highestSeq = -1;
        String[] files = (new File(myDir)).list();

        if (files == null) {
            // We were asked for a file in a nonexistent directory.
            // It's safe to assume that there are no clashing names.
            return firstVersion();
        }
                                       
        for (int i = 0; i < files.length; i++) {
            if (mightHaveSeq(files[i])) {
                int possibleSeq = getSeq(files[i]);
                if (possibleSeq < 0) {
                    Trace.trace.verbosem(files[i] + " has no sequence number.");
                } else if (possibleSeq <= highestSeq) {
                    Trace.trace.verbosem(files[i] + " is too low.");
                } else {
                    highestSeq = possibleSeq;
                    Trace.trace.verbosem(highestSeq + " is the best so far.");
                }
            } else {
                Trace.trace.verbosem(files[i] + " is not in version file format.");
            }
        }
        File retval = constructVersion(highestSeq+1);
        Trace.trace.eventm("Backup version for " + myFile + " is " + retval);
        return retval;
    }

    /**
     * The backup file with sequence number 0.  Doesn't matter if it exists.
     */
    protected File firstVersion() {
        File retval = constructVersion(0);
        Trace.trace.eventm("Backup version for " + myFile + " is " + retval);
        return retval;
    }

    /** 
     * The basename of a backup version, including any trailing '.'
     * separating the basename from the sequence number. 
     */
    protected String fetchBasename() {
        return myName + ".";
    }


    /** 
     * True iff the filename is of a format that <em>could</em>
     * be a backup version of the original file.  It remains to be
     * determined whether it truly contains a sequence number.
     * <p>
     * In a stunning display of write-once-run-everywhere, the check
     * is case-insensitive.  This obeys Windows conventions about what
     * "same files" are, not Unix conventions.
     * @param filename a filename, not including any directory part.
     */
    protected boolean mightHaveSeq(String filename) {
        int minlen = myBasename.length() + 1;
        return filename.length() >= minlen
               && filename.toLowerCase().startsWith(myBasename.toLowerCase());
    }

    /**
     * Return a sequence number, if the given file contains one.  If
     * it does not contain one, return -1.  Do not call this method
     * unless mightHaveSeq has approved the filename.
     */
    protected int getSeq(String filename) {
        String possibleSeqString = filename.substring(myBasename.length());

        try { 
            return Integer.parseInt(possibleSeqString);
        } catch (NumberFormatException e) {
            Trace.trace.shred(e, filename + " is not a backup file.");
            return -1; 
        }
    }
        
    /** Create a backup file name, given a sequence number. */
    protected File constructVersion(int sequence) {
        return new File(myDir, myBasename + sequence);
    }

    static public void main(String[] args) {
        TraceController.setProperty("TraceLog_trace", "debug");
        TraceVersionNamer v;
        if (args.length > 1) {
            v = TraceVersionNamer.factory(new File(args[0], args[1]));
        } else {
            v = TraceVersionNamer.factory(new File(args[0]));
        }
        System.out.println("myFile " + v.myFile);
        System.out.println("myDir " + v.myDir);
        System.out.println("myName " + v.myName);
        System.out.println("myBasename " + v.myBasename);
        System.out.println(v.nextAvailableVersion());
        System.out.println(v.firstVersion());
    }
}
