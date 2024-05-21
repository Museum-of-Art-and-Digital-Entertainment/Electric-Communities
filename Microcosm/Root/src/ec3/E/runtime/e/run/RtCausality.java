package ec.e.run;

import ec.util.Humanity;

public class RtCausality
{
    static private final int CAUSALITY_STRING_LIMIT = 8100;
    static private final String UnknownCausalityId = "unknown";

    private String myCurrentTraceString = "";
    private String myCausalityId;
    private String myMessageSepLine;
    private String myStartString;
    private String myUnavailableString;

    /*package*/ boolean myCausalityTracing = false;
    static /*package*/ boolean TheCausalityTracing = false;
    static /*package*/ RtCausality TheOne = null;

    /** Only the E runtime can instantiate this. */
    /*package*/ RtCausality() {
        if (TheOne == null) {
            TheOne = this;
        } else {
            throw new RuntimeException("Can only be one RtCausality");
        }
        setupIdStrings(null);
    }

    /** Get a current backtrace.
     * Zaplevels is the number of frames to clip off the top. */
    public static String getCausalityBacktrace(int zapLevels) {
        if (! TheCausalityTracing) {
            return TheOne.myUnavailableString;
        }

        String trace = Humanity.exceptionToHumanBacktrace(new Throwable());
        int zapto = 0;
        while (zapLevels >= 0) {
            zapto = trace.indexOf('\n', zapto) + 1;
            zapLevels--;
        }
        trace = trace.substring(zapto);

        if (trace.endsWith("\n    [E delivery]\n")) {
            trace = trace.substring(0, trace.length() - 17);
        }
        trace += TheOne.myMessageSepLine + TheOne.myCurrentTraceString;
        if (trace.length() > CAUSALITY_STRING_LIMIT) {
            zapto = trace.lastIndexOf('\n', CAUSALITY_STRING_LIMIT - 30);
            trace = trace.substring(0, zapto + 1) + "    ... (clipped) ...\n";
        }
        return trace;
    }

    /** This is the more user-friendly version of getCausalityBacktrace.
     */
    static public String getCausalityTraceString() {
        return getCausalityBacktrace(1);
    }

    /** Turn on or off causality tracing.
     */
    static public void setCausalityTracing(boolean on) {
        // only do stuff if they switch from on to off or vice versa
        if (TheCausalityTracing != on) {
            TheCausalityTracing = on;
            TheOne.myCausalityTracing = on;
            resetCausalityTrace();
        }
    }

    /** Are we causality tracing?
     */
    static public boolean isCausalityTracing() {
        return TheCausalityTracing;
    }

    /** Perform the action of messageWithCause. This should only
     * ever be called from the so-named EObject method or from
     * the RtDeflector code which emulates it.
     */
    static void doMessageWithCause(RtTether targ, RtSealer s, 
            RtExceptionEnv ee, Object[] args, String cause) {
        if (   (cause != null)
            && (TheCausalityTracing)) {
            TheOne.myCurrentTraceString = cause;
        } else {
            TheOne.myCurrentTraceString = TheOne.myUnavailableString;
        }
        try {
            targ.invokeNow(s, args, ee);
        } catch (Throwable t) {
            RtRun.tr.errorm("Uncaught Throwable in delivery of message; " +
                "translating into an ethrow");
            RtExceptionEnv.sendException(ee, t);
        } finally {
            resetCausalityTrace();
        }
    }

    /** Add the trace for the given exception to the current causality
     * trace. This is used when throws get turned into ethrows.
     */
    static void causalityEnhancementForException(Throwable t) {
        if (TheCausalityTracing) {
            TheOne.myCurrentTraceString =
                "    [throw of " + t.getClass() + 
                " translated into ethrow]\n" +
                "    [thrown message is: " + t.getMessage() + "]\n" +
                Humanity.exceptionToHumanBacktrace(t) +
                TheOne.myCurrentTraceString;
        }
    }

    static private RtSealer TheForwardExceptionSealer =
        sealer (EResult <- forwardException(Throwable));

    static private RtSealer TheMessageWithCauseSealer =
        sealer (EObject <- messageWithCause(RtSealer, RtExceptionEnv, Object,
            String));

    /** Enqueue a causality message for the given message to the given
     * queue. */
    static void enqueueWithCausality(RtQ queue, RtTether target, RtSealer seal,
        RtExceptionEnv ee, Object[] args) {
        RtQObj qobj;
        if (TheCausalityTracing) {
            String trace = getCausalityBacktrace(2);
            // XXX SEALER==
            if (seal.equals(TheForwardExceptionSealer)) {
                Throwable t = (Throwable) args[0];
                trace = "    [forward of " + t.getClass() + "]\n" +
                    "    [forwarded message is: " + t.getMessage() + "]\n"
                    + trace;
            } else {
                trace = "    send of " + seal + "\n" + trace;
            }
            
            qobj = RtQObj.make(
                target,
                TheMessageWithCauseSealer,
                ee,
                new Object[] { (Object)seal, (Object)ee, (Object)args, (Object)trace });
        } else {
            qobj = RtQObj.make(target, seal, ee, args);
        }
        queue.enqueue(qobj);
    }

    /**
     * This resets the causality trace. It's only to be used immediately
     * after dispatching a message or when turning tracing on or off.
     */
    static private void resetCausalityTrace() {
        if (TheCausalityTracing) {
            TheOne.myCurrentTraceString = TheOne.myStartString;
        } else {
            TheOne.myCurrentTraceString = TheOne.myUnavailableString;
        }
    }

    /**
     * This sets up the strings that have the id in them.
     */
    private void setupIdStrings(String id) {
        if (id == null) {
            id = UnknownCausalityId;
        }
        myCausalityId = id;
        myMessageSepLine = "    ----- message boundary ----- [receiver: " +
            id + "]\n";
        myStartString = "    [causality trace begins on receiver: " +
            id + "]\n";
        myUnavailableString = "    ----- causality trace unavailable ----- " +
            "[receiver: " + id + "]\n";
    }

    /** This sets the causality ID for this vat.
     * XXX - This is only for trusted E startup to call
     * need to prevent non TCB code from getting at it.
     */
    static public void setCausalityId(String id) {
        if (TheOne.myCausalityId == UnknownCausalityId) {
            TheOne.setupIdStrings(id);
        }
    }
}
