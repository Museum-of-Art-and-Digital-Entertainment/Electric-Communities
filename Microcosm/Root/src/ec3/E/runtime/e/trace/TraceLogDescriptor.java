/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import ec.util.assertion.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class describes the file system interface to a log file.
 * The standard format is <tag>.<date>.txt.  The standard backup
 * filename format is then <tag>.<date>.<sequence>.txt. 
 * <p>
 * The entire format may be overridden, the tag may be changed, or the
 * class can be instructed to use System.out.
 * <p>
 * This class is responsible for opening new files.  Importantly, that
 * includes renaming old versions.
 */
class TraceLogDescriptor implements Cloneable, TraceConstants {
    /**
     * The date this trace system was initialized; used as part of the
     * standard name format.
     */
    private static TraceDate initDate = new TraceDate();

    /** The directory in which the log lives. */
    private File dir = STARTING_LOG_DIR;

    /**
     * The 'tag' is the first component of the log filename.  
     */
    private String tag = STARTING_LOG_TAG;

    /** Determine whether System.out is used instead of a file. */
    private boolean useStdout = false;

    /** 
     * True if the user overrode the standard tag.date.txt format.
     */
    private boolean usePersonalFormat = false;

    /** The file being used for the log, if format chosen by user. */
    private String personalFile;

    /** The stream open to the log.  Clients print to this. */
    public PrintStream stream;

    
    /** 
     * This log descriptor always represents standard output. 
     * It should never be modified.  It starts out "not in use".
     */
    protected static TraceLogDescriptor stdout;

    static {
        stdout = new TraceLogDescriptor();
        stdout.usePersonalFormat = false;
        stdout.useStdout = true;
    }
        

// SETTERS

    /**
     * The user wishes to use a directory component different than
     * the default.  The file used is unchanged.
     */
    protected void setDir(String value) {
        Assertion.test(value != null);
        useStdout = false;
        // Don't change value of usePersonalFormat, as the directory
        // is independent of the filename format.
        dir = new File(value);
        Trace.trace.eventm("Log directory will be changed to '" +
                            value + "'.");
        if (!dir.isDirectory()) {
            Trace.trace.warningm("The log directory was set to '"
                + value + "', which is not currently a directory.");
        }
    }

    /**
     * The tag is the initial part of the standard filename.
     * Setting this implies that the date should be included in 
     * the filename.
     */
    protected void setTag(String value) {
        Assertion.test(value != null);
        useStdout = false;
        usePersonalFormat = false;
        tag = value;
        Trace.trace.eventm("Log tag set to '" + value + "'.");
    }

    /**
     * If the argument is "-", standard output is used.  If the argument
     * is something else, that becomes the complete filename,
     * overriding the tag, eliminating use of the date/time field, and
     * not using the default extension.  It does not affect the directory
     * the file is placed in.
     */
    protected void setName(String value) { 
        Assertion.test(value != null);
        if (value.equals("-")) {
            useStdout = true;
            usePersonalFormat = false;
            Trace.trace.eventm("Log destination set to standard output.");
        } else {
            useStdout = false;
            usePersonalFormat = true;
            personalFile = value;
            Trace.trace.eventm("Log destination will be changed to " +
                "file '" + personalFile + "'.");
        }
    }

// MAIN OPERATORS

    /**
     * Two LogDescriptors are equal iff they refer to the same (canonical) file.
     */
    public boolean equals(TraceLogDescriptor other) {
        return this.printName().equals(other.printName());
    }

    /**
     * Enables this LogDescriptor for use.  Most obvious effect is
     * that 'stream' is initialized.  
     *
     * @param clashAction determines what to do if the target logfile
     * already exists.  The two options are ADD (to add a new backup file)
     * or OVERWRITE (to overwrite an existing one).
     * IRRELEVANT should be used when
     * the destination is <em>known</em> to be standard output, which never
     * clashes. 
     *
     * @exception Exception is thrown if a logfile could not be
     * opened.  The contents of the exception are irrelevant, as this
     * method logs the problem.
     */
    protected void startUsing(int clashAction) throws Exception {
        Assertion.test(!inUse());

        if (useStdout) {
            Trace.trace.eventm("Logging has been directed to standard output.");
            stream = System.out;
            return;
        }

        Assertion.test(clashAction == ADD || clashAction == OVERWRITE);

        File nextFile = desiredLogFile();
        Trace.trace.eventm("Logging has been directed to '" + 
                          nextFile + "'.");

        if (nextFile.exists()) {
            if (nextFile.isDirectory()) {
                Trace.trace.errorm("Attempt to open directory " +
                                   nextFile + " as a logfile failed.");
                throw new IOException("opening directory as a logfile");
            }
            // Try to back it up.  If that fails, oh well.  Open
            // the desiredLogFile anyway.  That is less harmful than
            // the alternative, which would be to spew output to
            // stdout.  I think.
            renameToBackup(nextFile, clashAction);
        }

        try {
            stream = new PrintStream(new FileOutputStream(nextFile));
        } catch (SecurityException e) {
            Trace.trace.errorm(
                "Security exception when opening new trace file '" + 
                nextFile + "'.");
            throw e;
        } catch (FileNotFoundException e) {
            Trace.trace.errorm(
                "Could not open new trace file '" + 
                nextFile + "'.");
            throw e;
        } catch (IOException e) {
            Trace.trace.errorm(
                "Unknown error when opening new trace file '" + 
                nextFile + "'.");
            throw e;
        }
    }

    /**
     * Cease using this LogDescriptor.  The most obvious effect is
     * that 'stream' is now null.  Behind the scenes, any open file is
     * closed.
     * You can alternate stopUsing() and
     * startUsing() an arbitrary number of times.  
     */
    protected void stopUsing() {
        Assertion.test(inUse());
        if (stream != System.out) {
            // I don't trust finalizers to do it, at least not in time.
            Trace.trace.eventm("Closing " + printName());
            stream.close();
        }
        stream = null;
    }

    protected boolean inUse() { 
        return stream != null;
    }

// UTILITIES

    /** 
     * Attempt to rename this file to a backup file with a version number.
     * Returns true iff the rename succeeds.  The name of the backup
     * file is constructed by a TraceVersionName, using the current
     * name of the file.
     * <p>
     * If the backup file does not exist (and can be written), all is
     * fine. Otherwise:
     * @param clashAction is ADD if a new backup file should be added,
     * otherwise OVERWRITE if an existing one should be overwritten.
     * @returns true if the rename was successful.
     */
    private boolean renameToBackup(File file, int clashAction) {
        Assertion.test(clashAction == ADD || clashAction == OVERWRITE);

        File backupFile = backupFile(file, clashAction);
        if (clashAction == ADD) {
            // backupFile must return a fresh name if the clashAction
            // is ADD.  It *may* return a fresh name if the
            // clashAction is OVERWRITE.
            Assertion.test(!backupFile.exists());
        }

        Trace.trace.worldm("Renaming previous version of " +
                          file + " to " + backupFile + ".");
        try {
            if (backupFile.exists()) {  // clashAction == OVERWRITE
                if (!backupFile.delete()) {
                    Trace.trace.warningm("The previous version of " +
                                     file + " could not be put in backup " + 
                                     "file " + backupFile + " because the " +
                                     " existing file could not be deleted.");
                    return false;
                }
                Trace.trace.eventm("The previous version of " +
                                   backupFile + " has been deleted.");
            }
        } catch (SecurityException e) {
            Trace.trace.warningm("The previous version of " +
                                 file + " could not be put in backup " + 
                                 "file " + backupFile + " because the " +
                                 " existing file could not be deleted.");
            return false;
        }

        try {
            boolean renamed = file.renameTo(backupFile);
            if (!renamed) {
                Trace.trace.warningm(file +
                                     " could not be renamed to backup " +
                                     backupFile);
            }
            return renamed;
        } catch (SecurityException e) { 
            Trace.trace.warningm(file +
                                 " could not be renamed to backup " +
                                 backupFile +
                                 " because of a security violation.");
            return false;
        }
    }

    /** 
     * Say what renameToBackupFile will try to do when it's called
     * by startUsing. This is used
     * when the log file will be closed before the renaming is done.
     * It's a way to get some information in the old log file.
     */
    protected void describeFutureBackupAction(int clashAction) {
        if (useStdout) return;  // No backup file.
        
        File nextFile = desiredLogFile();
        if (!nextFile.exists()) return;         // no backup.

        File backupFile = backupFile(nextFile, clashAction);

        Trace.trace.worldm("The file will be backed up as " + backupFile);
    }

    /** 
     * Return the file to use as a backup.
     * Stdout is never backed up, so useStdout should be false.
     * @param clashAction determines which name the backup file will
     * have. ADD means a file with the next highest sequence number.
     * OVERWRITE means a file with the smallest sequence number.
     */
    protected File backupFile(File file, int clashAction) {
        Assertion.test(!useStdout);

        if (clashAction == ADD) {
            return TraceVersionNamer.factory(file).nextAvailableVersion();
        } else if (clashAction == OVERWRITE) {
            return TraceVersionNamer.factory(file).firstVersion();
        } else { 
            Assertion.fail("Bad clashAction " + clashAction);
            return null;
        }
    }

    /** 
     * Return a name of this descriptor, suitable for printing.
     * System.out is named "standard output".  Real files are named
     * by their canonical pathname (surrounded by single quotes).
     * <p>
     * Note that the printname may be the absolute pathname if the
     * canonical path could not be discovered (which could happen
     * if the file does not exist.)
     */
    protected String printName() {
        if (useStdout) {
            return "standard output";
        } else {
            String canonical;
            try {
                canonical = desiredLogFile().getCanonicalPath();
            } catch (IOException e) {
                // The canonical path was undiscoverable.  Punt by 
                // returning the absolute pathname.
                canonical = desiredLogFile().getAbsolutePath();
            }
            if (canonical == null) {
                // Quoth the java language spec:
                // "The canonical form of a pathname of a nonexistent 
                // file may not be defined."
                // What happens in that case is ALSO not defined.  Null
                // seems like a possibility.
                canonical = desiredLogFile().getAbsolutePath();
            }
             
            return "'" + canonical + "'";
        }
    }

    /** 
     * Given the current state of this object's fields, construct the
     * file the user wants.  It is a program error to call this
     * routine if the user wants System.out, not a file.
     */
    private File desiredLogFile() {
        Assertion.test(!useStdout);
        if (usePersonalFormat) {
            if ((new File(personalFile)).isAbsolute()) {
                return new File(personalFile);
            } else {
                return new File(dir, personalFile);
            }
        } else {
            return new File(dir, tag + "." +
                            initDate.terseCompleteDateString() +
                            LOG_EXTENSION);
        }
    }

    /**
     * A clone of a TraceLogDescriptor is one that, when startUsing()
     * is called, will use the same descriptor, be it a file or
     * System.out.  The clone is not inUse(), even if what it was
     * cloned from was.
     */
    protected Object clone() {
        try {
            TraceLogDescriptor cl = (TraceLogDescriptor) super.clone();
            cl.stream = null;
            Assertion.test(!cl.inUse());
            return cl;
        } catch (CloneNotSupportedException e) {
            Assertion.fail("Clone IS SO supported.");
            // Someday I gotta figure out the right way to declare
            // clone.
            return null;
        }
    }
}
