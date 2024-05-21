package ec.e.run;

import ec.util.EThreadGroup;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Date;

final public class RtUtil {

    private static boolean debugLibraries = false;
    
    public static void setLibraryDebugging(boolean debugState) {
        debugLibraries = debugState;
    }
    
    public static void loadLibrary (String libraryName) {
        boolean needToLoad;
        if (debugLibraries) {
            needToLoad = false; // Assume the best
            try {
                System.loadLibrary(libraryName + "_g");
            }
            catch (Exception e) {
                // Couldn't get debug version, indicate 
                // to try loading non debug version
                needToLoad = true;
            }
        }
        else {
            needToLoad = true;
        }
        if (needToLoad) System.loadLibrary(libraryName);
    }
    
    public static void writeStringInFile (String string, String fileName) {
        try {
            if (fileName != null) {
                FileOutputStream fs = new FileOutputStream(fileName);
                DataOutputStream ds = new DataOutputStream(fs);
                ds.writeUTF(string);
                fs.close();
            }
        } catch (Exception e) {
        }
    }
    
    public static String readStringFromFile (String fileName) {
        try {
            if (fileName != null) {
                FileInputStream fs = new FileInputStream(fileName);
                DataInputStream ds = new DataInputStream(fs);
                String s = ds.readUTF();
                fs.close();
                return s;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static boolean isSubclass(Class lower, Class upper) {
        do {
            if (lower == upper)
                return(true);
            lower = lower.getSuperclass();
        } while (lower != null);
        return(false);
    }

    public static void fail(String message, Exception exception) {
        System.out.println("System caught exception: " + exception);
        EThreadGroup.reportException(exception);
        System.out.println(message);
        System.exit(1);
    }

    public static void fail(String message) {
        System.out.println(message);
        System.exit(1);
    }

    public static String joinPath(String left, String delim, String right) {
        if (left.length()==0 || left.equals(delim))
            return(right);
        else if (right.length()==0 || right.equals(delim))
            return(left);
        if (left.endsWith(delim))
            left = left.substring(0, left.length() - delim.length());
        if (right.startsWith(delim))
            return(left + right);
        else
            return(left + delim + right);
    }

    // used by restriction routines to handle time based restrictions
    // can be handed either an absolute date (passed to the java Date parser)
    // or +interval.  if wantInterval is true, the interval is returned
    // otherwise an absolute date one interval past now is returned.
    // return value is milliseconds, with absolute date being milliseconds since
    // the epoch (as used in the java Date class).
    public static long parseExpirationDate(String s, boolean wantInterval) {
        if (s.equals("unlimited")) {
            return Long.MAX_VALUE ;
        }
        else if (s.startsWith("+")) {
            // looking for +ddd:hh:mm:ss.mil
            int elts[] = new int[4];
            int numelts = 0 ;
            int mils = 0 ;
            long val = 0 ;
            String tok;
            boolean wantmils = false;

            elts[0] = 0 ;
            StringTokenizer t = new StringTokenizer(s.substring(1), ":.", true) ;
            while (t.hasMoreTokens()) {
                tok = t.nextToken();
                if (tok.equals(":")) {
                    if (++numelts > 3) throw new IllegalArgumentException();
                    elts[numelts] = 0 ;
                }
                else if (tok.equals(".")) {
                    wantmils = true;
                }
                else {
                    if (wantmils) {
                        tok = tok + "000" ;
                        mils = Integer.parseInt(tok.substring(0, 3));
                        if (t.hasMoreTokens()) throw new IllegalArgumentException();
                    }
                    else {
                        elts[numelts] = Integer.parseInt(tok);
                    }
                }
            }
            val = elts[numelts--] ;                                 // seconds
            if (numelts >= 0) val += elts[numelts--] * 60 ;         // minutes
            if (numelts >= 0) val += elts[numelts--] * 60*60 ;      // hours
            if (numelts >= 0) val += elts[numelts--] * 60*60*24 ;   // days
            val = val * 1000 + mils ;
            if (wantInterval) return val;
            Date d = new Date();
            return d.getTime() + val ;
        }
        else {
            if (wantInterval) throw new IllegalArgumentException();
            return Date.parse(s);
        }
    }

    // used by restrictions to handle range restrictions
    // passed a string "min:max" and an old range returns
    // the intersection of the ranges
    // any range with max < min has no members
    // string can be "unlimited" to return original range
    // or just a number which restricts the range to just that number
    // if it is within the original range
    public static long[] parseRange(String s, long min, long max) {
        long range[] = new long[2] ;
        int i;

        if (s.equals("unlimited")) {
            range[0] = min;
            range[1] = max;
        }
        else if ((i = s.indexOf(':')) > 0) {
            long low = Long.parseLong(s.substring(0, i)) ;
            long high = Long.parseLong(s.substring(i+1)) ;
            range[0] = (low  > min) ? low  : min ;
            range[1] = (high < max) ? high : max ;
        }
        else {
            long val = Long.parseLong(s);
            range[0] = (val > min) ? val : min ;
            range[1] = (val < max) ? val : max ;
        }
        return range;
    }

    // used by RtNet restrictions to parse ip addresses and masks
    // will parse arbitrary length dotted byte strings
    // particularly useful for specifying masks shorter than 4 bytes
    public static byte[] parseInetAddr(String s) {
        byte addr[];
        int a;
        int addrLen = 1 ;
        int i = 0 ;
        int dot = -1 ;
        int dot2;

        while ((dot = s.indexOf(".", dot+1)) > 0) {
            addrLen++ ;
        }
        addr = new byte[addrLen];
        dot = -1 ;
        for (i=0; i<addrLen; i++) {
            dot2 = s.indexOf(".", dot+1);
            if (dot2 < 0) dot2 = s.length() + 1 ;
            a = Integer.parseInt(s.substring(dot+1, dot2-1)) ;
            dot = dot2 ;
            if (a < 0) a = 0 ;
            if (a > 255) a = 255;
            addr[i] = (byte)a ;
        }
        return addr;
    }
}
