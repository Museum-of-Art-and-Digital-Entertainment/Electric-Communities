package ec.e.inspect;

import ec.vcache.ClassCache;
import java.util.Hashtable;
import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import ec.util.NestedException;
import ec.e.run.Trace;
import java.lang.Thread;
import java.lang.ThreadGroup;
import netscape.application.*;

public class Tracer {

    // Constants defined in __DEBUG.java:
    // catch-all label for (I think) traceAddress's own references
    public static final int REF_OTHER = 0;

    // label for traceAddress references to heap (normal objects)
    public static final int REF_HEAP = 1;

    // label for traceAddress references to nonconservative stack
    // (i.e., definitely a pointer). Points to the Thread object that
    // owns this stack.

    public static final int REF_STACK = 2;

    // label for traceAddress references to conservative stack (i.e.,
    // maybe a pointer). Points to the Thread object that owns this
    // stack.

    public static final int REF_CSTACK = 3;

    // label for traceAddress references to global space
    public static final int REF_GLOBAL = 4;

    // label for traceAddress references to static variables
    public static final int REF_STATIC = 5;

    // label for traceAddress references to intern tables
    public static final int REF_INTERN = 6;

    // label for traceAddress references to finalization queue
    public static final int REF_FINAL = 7;

    /*
    final String kind0 = kindString(0);      // catch-all label for traceAddress references
    final String kind1 = kindString(1);      // references to heap (normal objects)
    final String kind2 = kindString(2);      // nonconservative stack (definitely a pointer)
    final String kind3 = kindString(3);      // conservative stack (probably a pointer)
    final String kind4 = kindString(4);      // Global space (don't know what this means)
    final String kind5 = kindString(5);      // Static variable
    final String kind6 = kindString(6);      // intern tables
    final String kind7 = kindString(7);      // Finalization queue
    */

    private static Class debugClass = null;
    private static Method addressOfMethod = null;
    private static Method objectifyMethod = null;
    private static Method addressStringMethod = null;
    private static Method parseAddressMethod = null;
    private static Method traceAddressMethod = null;
    private static Method kindStringMethod = null;
    private static Method memoryDumpMethod = null;
    private static boolean punt = false;

    /**
     * If we are running under the debugging java VM, hook up to the
     * java.lang.__DEBUG class. Otherwise just remember we failed to
     * do this. We do this using java.lang.reflect to avoid link
     * errors in case the support is not there.
     */

    private static void initialize() {
        String failure = "no VM DEBUG class found";
        if (punt) return;
        try {
            punt = true;
            debugClass = ClassCache.forName("java.lang.__DEBUG");

            Class addressOfArgsVector[] = new Class[1];
            addressOfArgsVector[0] = (new Object()).getClass();
            failure = "not running EC Debug VM";

            addressOfMethod = debugClass.getMethod("addressOf",addressOfArgsVector);

            // Test this immediately

            Object[] args = { new Object()};
            Object result = addressOfMethod.invoke(null,args);

            failure = "__DEBUG initialization problem";

            // If test went well (i.e. did not throw an UnsatisfiedLinkError), continue.

            Class objectifyArgsVector[] = new Class[1];
            objectifyArgsVector[0] = Integer.TYPE;
            objectifyMethod = debugClass.getMethod("objectify",objectifyArgsVector);

            Class parseAddressArgsVector[] = new Class[1];
            parseAddressArgsVector[0] = new String("").getClass();
            parseAddressMethod = debugClass.getMethod("parseAddress",parseAddressArgsVector);

            Class addressStringArgsVector[] = new Class[2];
            addressStringArgsVector[0] = Integer.TYPE;
            addressStringArgsVector[1] = Integer.TYPE;
            addressStringMethod = debugClass.getMethod("addressString",addressStringArgsVector);

            Class traceAddressArgsVector[] = new Class[3];
            traceAddressArgsVector[0] = Integer.TYPE;
            traceAddressArgsVector[1] = (new int[1]).getClass();
            traceAddressArgsVector[2] = (new int[1]).getClass();
            traceAddressMethod = debugClass.getMethod("traceAddress",traceAddressArgsVector);

            Class kindStringArgsVector[] = new Class[1];
            kindStringArgsVector[0] = Integer.TYPE;
            kindStringMethod = debugClass.getMethod("kindString",kindStringArgsVector);

            failure = "memoryDump() initialization problem";

            Class memoryDumpArgsVector[] = new Class[0];
            memoryDumpMethod = debugClass.getMethod("memoryDump",memoryDumpArgsVector);

            punt = false;       // We've not thrown yet so the EC debugging VM exists!
            System.out.println("Found EC debugging VM - Enabling some Inspector features");
        } catch (Throwable t) {
            System.out.println("No VM debugging (" + failure + ")");
            debugClass = null;
            punt = true;        // Remember not to try this again.
        }
    }

    /**
     * Check to see if we are running under the EC debugging VM. Note
     * that just using java_g is not enough - we must use the EC
     * version of java_g. We test this by attempting to use the
     * addressOf operator on an instance. If this throws an
     * UnsatisfiedLinkError, then we don't have the required support.
     */


    public static boolean runningUnderDebuggingVM() {
        if (punt) return false;                  // We have tried and failed
        if (debugClass != null) return true;     // We have tried and succeeded
        initialize();                            // Let's try since we haven't tried yet
        if (punt) return false;                  // We failed
        return true;                             // We succeeded
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    static public int addressOf(Object o) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("AddressOf() is available only under EC debugging Java VM");

        Object[] args = new Object[1];
        args[0] = o;
        Object result;

        try {
            result = addressOfMethod.invoke(null,args);
        } catch (IllegalAccessException iax) {
            System.out.println(iax.getMessage() + " " + iax);
            iax.printStackTrace();
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            System.out.println(iarx.getMessage() + " " + iarx);
            iarx.printStackTrace();
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            Throwable inx = itx.getTargetException();
            System.out.println(inx.getMessage() + " " + inx);
            inx.printStackTrace();
            throw new NestedException("Shouldn't happen:" + itx + " " + inx, inx);
        }
        if (result instanceof Integer) return ((Integer)result).intValue();
        else throw new RuntimeException
               ("Inspector cannot find address of object " + o + " - result is " + result);
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    static public Object objectify(int addr) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Objectify() is available only under EC debugging Java VM");

        Object[] args = new Object[1];
        args[0] = new Integer(addr);
        try {
            return objectifyMethod.invoke(null,args);
        } catch (IllegalAccessException iax) {
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            itx.printStackTrace();
            throw new NestedException("Objectify threw an exception:" + itx, itx);
        }
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    public static String addressString(int address, int minLen) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("addressString() is available only under EC debugging Java VM");

        Object[] args = new Object[2];
        args[0] = new Integer(address);
        args[1] = new Integer(minLen);
        try {
            return (String)addressStringMethod.invoke(null,args);
        } catch (IllegalAccessException iax) {
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            throw new NestedException("Shouldn't happen:", itx);
        }
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    static public int parseAddress(String address) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("parseAddress() is available only under EC debugging Java VM");

        Object[] args = new Object[1];
        args[0] = address;
        try {
            Integer result = (Integer)parseAddressMethod.invoke(null,args);
            return result.intValue();
        } catch (IllegalAccessException iax) {
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            itx.printStackTrace();
            throw new NestedException("VM address lookup problem:" + itx.getMessage(), itx);
        }
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    static public String kindString(int kind) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("KindString() is available only under EC debugging Java VM");

        Object[] args = new Object[1];
        args[0] = new Integer(kind);
        try {
            return (String)kindStringMethod.invoke(null,args);
        } catch (IllegalAccessException iax) {
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            throw new NestedException("Shouldn't happen:", itx);
        }
    }

    /**
     * This is just a convenience method which calls the equivalent
     * method in __DEBUG.
     */

    static public String memoryDump() {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException
              ("memoryDump() is available only under EC debugging Java VM");

        Object[] args = new Object[0];
        try {
            return (String)memoryDumpMethod.invoke(null,args);
        } catch (IllegalAccessException iax) {
            throw new NestedException("Shouldn't happen:", iax);
        } catch (IllegalArgumentException iarx) {
            throw new NestedException("Shouldn't happen:", iarx);
        } catch (InvocationTargetException itx) {
            throw new NestedException("Shouldn't happen:", itx);
        }
    }

    /**
     * Get an array of TraceInfo containing the immediate references
     * to the given address (which was gotten from either addressOf()
     * or from a preexisting TraceInfo.
     */

    static public TraceInfo[] traceAddress(int addr) {
        int maxSize = 70;
        int actualSize;
        int[] refs;
        int[] refKinds;

        if (!runningUnderDebuggingVM())
            throw new RuntimeException("TraceInfo is available only under EC debugging Java VM");

        Object[] args = new Object[3];
        args[0] = new Integer(addr);

        // This version does not care if the results are larger than maxsize.
        // Just truncate the results at maxsize

            refs = new int[maxSize + 20]; // Provide some margin
            args[1] = refs;
            refKinds = new int[maxSize + 20]; // Provide some margin
            args[2] = refKinds;
            Object tmp;

            try {
                tmp = traceAddressMethod.invoke(null, args);
            } catch (IllegalAccessException iax) {
                throw new NestedException("Shouldn't happen:", iax);
            } catch (IllegalArgumentException iarx) {
                throw new NestedException("Shouldn't happen:", iarx);
            } catch (InvocationTargetException itx) {
                itx.printStackTrace();
                throw new NestedException("TraceInfo threw " + itx, itx);
            }

            actualSize = ((Integer)tmp).intValue();

            if (actualSize > maxSize) actualSize = maxSize;

        TraceInfo[] result = new TraceInfo[actualSize];
        for (int i = 0; i < actualSize; i++) {
            result[i] =
              new TraceInfo(refs[i], refKinds[i]);
        }
        return result;
    }


    /**
     * Get an array of TraceInfo containing the immediate references
     * to the given address (which was gotten from either addressOf()
     * or from a preexisting TraceInfo.
     */

    static public TraceInfo[] unlimited_traceAddress(int addr) {
        int maxSize = 500;
        int actualSize;
        int[] refs;
        int[] refKinds;

        if (!runningUnderDebuggingVM())
            throw new RuntimeException("TraceInfo is available only under EC debugging Java VM");

        Object[] args = new Object[3];
        args[0] = new Integer(addr);

        for (;;) {

            refs = new int[maxSize];
            args[1] = refs;
            refKinds = new int[maxSize];
            args[2] = refKinds;
            Object result;

            try {
                result = traceAddressMethod.invoke(null, args);
            } catch (IllegalAccessException iax) {
                throw new NestedException("Shouldn't happen:", iax);
            } catch (IllegalArgumentException iarx) {
                throw new NestedException("Shouldn't happen:", iarx);
            } catch (InvocationTargetException itx) {
                itx.printStackTrace();
                throw new NestedException("TraceInfo threw " + itx, itx);
            }

            actualSize = ((Integer)result).intValue();
            if (actualSize <= maxSize) break;
            maxSize = actualSize + 200; /* extra just in case */
        }

        TraceInfo[] result = new TraceInfo[actualSize];
        for (int i = 0; i < actualSize; i++) {
            result[i] =
              new TraceInfo(refs[i], refKinds[i]);
        }
        return result;
    }

    /**
     * Recursively fill out a full trace of an address back to
     * all roots. <p>

     * This class contains a minor laundry list of classes we must not
     * recurse into, such as Thread and
     * netscape.application.MouseEvent. Many of these are harmless in
     * and of themselves but contain things like arrays of Object or
     * Vectors of Objects that can contain poisonous objects and we
     * cannot filter at the level of the positonous object since that
     * would filter out all Vectors, etc. So we filter at the higher
     * level object instead. <p>

     * We analyze *up to* certain reference types but don' trecurse
     * further in. As an example, we're happy to know something is in
     * a finalization queue without wanting to trace the queue itself,
     * so we prune when we get there.

     */

    static public TraceInfo fullTrace(int address) {
        final Hashtable infos = new Hashtable(100);
        final int[] curLabel = { 0 };

        if (!runningUnderDebuggingVM())
            throw new RuntimeException("fullTrace() is available only under EC debugging Java VM");

        class InnerTrace {
            void doit(RecursiveTraceInfo parent, int depth) {
                int addr = parent.address;
                TraceInfo[] info = traceAddress(addr);
                for (int i = 0; i < info.length; i++) {
                    TraceInfo inf = info[i];
                    Integer addressInteger = new Integer(inf.address);
                    TraceInfo already = (TraceInfo) infos.get(addressInteger);
                    if (already == null) {
                        if (inf.refKind == REF_HEAP) {
                            System.out.print(" addr=" + addressString(inf.address, 1));
                            Object object = objectify(inf.address);
                            System.out.println(" => " + object.getClass().getName());
                            if (object == null) continue;
                            if (object instanceof TraceInfo) continue;
                            if (object instanceof Tracer) continue;
                            if (object instanceof Class) continue;
                            if (object instanceof Thread) continue;
                            if (object instanceof ThreadGroup) continue;
                            if (object instanceof netscape.application.Event) continue;
                            if (object instanceof netscape.application.MouseEvent) continue;
                            if (object instanceof netscape.application.FoundationPanel) continue;
                            if (object instanceof netscape.application.FoundationFrame) continue;
                            if (object instanceof netscape.application.ScrollBar) continue;
                            if (object instanceof ec.e.openers.Surgeon) continue;
                            if (object instanceof ec.e.openers.Chef) continue;

                            // Add more classes to filter out above, as needed

                            RecursiveTraceInfo rti = new RecursiveTraceInfo(inf);
                            info[i] = rti;
                            infos.put(addressInteger, rti);
                            System.out.println(depth + " --> " + object.getClass() +
                                               " (" + inf.refKind + ")");
                            doit(rti, depth + 1);
                            System.out.println(depth + " <-- " + object.getClass());
                        }
                        // else System.out.println("Ignoring " + inf.address +
                        // " refKind is " + inf.refKind);
                    } else {
                        if (already.label == null) {
                            already.label = "" + curLabel[0];
                            curLabel[0]++;
                        }
                        info[i] = new TraceInfoBackref(already);
                    }
                }
                parent.trace = info;
            }
        };

        final RecursiveTraceInfo result =
          new RecursiveTraceInfo(address, 0);
        new InnerTrace().doit(result, 10);
        return result;
    }
}
