/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */


package ec.e.run;
import java.util.Date;

/** 
 * A version of the Date class that provides some more convenient
 * reporting functions.
 * <p>
 * Note that the construction of strings in this class is one of the
 * bottlenecks in this code.
 */
class TraceDate extends Date {
    TraceDate () {
        super();
    }

    /** 
     * Returns a string like 1997/04/23.  Note that strings of this
     * form can be sorted by date, unlike M/D/Y or D/M/Y, or the
     * standard java form that omits zero padding.  By being in obviously
     * non-European and non-US order (because of the year), no one
     * gets confused as to which field is the day and which is the
     * month.  Note finally:  no Year 2000 problem.
     */
    protected final String dateString () {
        String retval = String.valueOf(getYear() + 1900);
        retval += "/" +
            zeroFill(getMonth()+1) + "/" +
            zeroFill(getDate());
        return retval;
    }

    /**
     * Return time in form HH:MM:SS.  Fields are zero-padded as
     * needed.
     */
    protected final String timeString () {
        return zeroFill(getHours()) + ":" + 
            zeroFill(getMinutes()) + ":" + 
            zeroFill(getSeconds()) + "." +
            zero3Fill(getTime() % 1000);
    }

    /**
     * Return time in form YYYYMMDDHHMMSS.  This is a terse sortable
     * time.  Fields are zero-padded as needed.
     */
    protected final String terseCompleteDateString() {
        String retval = String.valueOf(getYear() + 1900);
        retval +=
            zeroFill(getMonth()+1) +
            zeroFill(getDate()) +
            zeroFill(getHours()) +
            zeroFill(getMinutes()) +
            zeroFill(getSeconds());
        return retval;
    }

    private final String zeroFill(int number) {
        if (number <= 9) {
            return "0" + String.valueOf(number);
        } else {
            return String.valueOf(number);
        }
    }

    private final String zero3Fill(long number) {
        if (number <= 9) {
            return "00" + String.valueOf(number);
        } else if (number <= 99) {
            return "0" + String.valueOf(number);
        } else {
            return String.valueOf(number);
        }
    }


}
