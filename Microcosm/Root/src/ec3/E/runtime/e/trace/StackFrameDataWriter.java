/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.io.Writer;

/** 
 * This class is needed because fully 34% of the time spent writing a 
 * trace message was spent decoding the stack frame.  This is faster
 * 
 * accessors to useful information.  
 * <p>
 * DANGER: it is HIGHLY dependent on the particular way the 
 * implementation prints stack traces.  It should probably do some
 * syntax checking.
 * <p>
 * For reference, here's the current print format:
 * <pre>
 * java.lang.Exception
 *  at Trace.$(Trace.java:217)
 *  at Test.go(Test.java:25)
 *  at Test.main(Test.java:20)
 * </pre>
 * <p>
 * Stack frames from jit-ed code look like:
 *  at Trace.$(Compiled Code)
 */
class StackFrameData
{
