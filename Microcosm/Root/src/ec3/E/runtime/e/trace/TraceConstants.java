/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;
import ec.util.assertion.*;
import java.io.File;

interface TraceConstants {
    /** This identifies the TraceMessageAcceptor used for the on-disk log. */
    int LOG = 0;
    /** 
     * This identifies the TraceMessageAcceptor used for the in-core trace and
     * its associated window.
     */ 
    int TRACE = 1;

    /** The number of different types of TraceMessageAcceptors. */
    int NUM_ACCEPTORS = 2;

    String acceptorNames[] = { "log", "trace" };

    /** 
     * The different trace thresholds.  See the Trace class for
     * documentation.  There is space between the levels for
     * expansion.  If you add or delete a level, you must change 
     * Trace.java to add new methods and variables.  (Don't forget the
     * fooReportException deprecated methods.)
     */

    int ERROR = 10000;  // always set.
    int WARNING = 120;
    int WORLD = 100;
    int USAGE = 80;
    int EVENT = 60;
    int DEBUG = 40;
    int VERBOSE = 20;

    int MAX_THRESHOLD=ERROR;

    /** 
     * As a late addition, there's a "timing" boolean that can be
     * set orthogonally from the thresholds.  The above values are
     * overloaded: thresholds, but also identifiers for the original message
     * (was it sent with errorm(), etc.).  The TIMING "level" is added
     * for the latter purpose, but it has nothing to do with thresholds.
     * To avoid confusion, it's set negative, thus below the minimum
     * threshold.
     */
    int TIMING = -20;

    /**
     * When referring to thresholds, are we talking about those
     * from the default thresholds, or ones specific to a subsystem?
     * XXX These could be interned strings, but interning didn't work
     * right in 1.0.4.  That is, two "default" strings weren't eq.
     */
    int FROM_DEFAULT = 0;
    int FOR_SUBSYSTEM = 1;

    String reasonNames[] = {"default", "subsystem" };

    /* Trace buffer defaults */
    int STARTING_TRACE_BUFFER_SIZE = 500;
    int STARTING_TRACE_THRESHOLD = USAGE;

    /* Trace log defaults. */
    long STARTING_LOG_SIZE_THRESHOLD = 500000;
    long SMALLEST_LOG_SIZE_THRESHOLD = 1000;
    int STARTING_LOG_THRESHOLD = WORLD;
    boolean STARTING_LOG_WRITE = false;

    // Behavior when opening files that already exist.
    // These were supposed to be strings, for debugging, but
    // interning doesn't seem to work right in 1.0.2, at least across
    // class boundaries.
    int IRRELEVANT = -1; // When opening stdout.
    int ADD = 1111;  // Add a new backup file.
    int OVERWRITE = 0;    // Overwrite any existing backup file.
    int STARTING_LOG_BACKUP_ACTION = ADD;

    // XXX At some point, this might be initialized to some default
    // directory.  In Windows, the "current working directory" has
    // a bad habit of hopping around at runtime.
    File STARTING_LOG_DIR = new File(".");
    String STARTING_LOG_TAG = "ECLog";
    String LOG_EXTENSION = ".txt";  // DON'T change this to upper case.
                                    // It causes duplicate filenames.

    // Internationalization, ho ho.
    String DEFAULT_NAME = "default";
    String UNLIMITED_NAME = "unlimited";
}
