package ec.e.inspect;
import java.util.*;
import ec.e.openers.*;
import ec.e.run.OnceOnlyException;
import ec.e.run.RtRun;
import ec.e.run.RtQObj;
import ec.e.run.RtQueueTimer;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970710
 */

/*

 * Wrapper class that contains one E Runnable plus some extra
 * information. These are used to create a tree of all Runnables and
 * the consequences (Runnables enqueued as a result of running) of each
 * Runnable. <p>

 * We don't always use this wrapper class. Uninteresting events are
 * simply run the old way. Only if we want to collect the consequences
 * do we need to create an instance of this wrapper class, to hold the
 * consequences vector (and some other data).

 * In general, uninteresting events are handled quickly by the
 * RunQueueInspector in a mostly-static-method fashion in the RunQueue
 * thread, and interesting events are handled carefully by Runlet
 * instances, controlled by signals from the Runqueue Inspector UI.

 * New Runlets are created bny executing existing Runlets, or by
 * encountering a broken object in RunQueueInspector. In the latter
 * case, the event is placed in the top level vector.

 */

public class Runlet {
    public static final int NOT_RUN = 0; // We have not been run ourselves and have no consequences.
    public static final int SOME_RUN = 1; // Some consequences have been run
    public static final int ALL_RUN = 2; // All consequences have run to completion
    public static final int NONE_RUN = 3; // None of the consequences has been run yet.
    public static final int RELEASED_RUN = 4; // This Runlet has been released and must not be run again.

    public static final int VIRGIN_COLOR = NOT_RUN;
    public static final int SOME_COLOR = SOME_RUN;
    public static final int DONE_COLOR = ALL_RUN;
    public static final int LEAF_COLOR = 3;
    public static final int RELEASED_COLOR = RELEASED_RUN;

    private static int idNumber = 0;
    private Vector myConsequences = null;
    private Runlet myParent;
    private int myState = NOT_RUN; // Are we ourselves run, and if so, how many consequences have been run?
    private String myCausalityTrace;
    private Object myUIRef = null;
    private int myID;

    /*package*/ RtQObj myQObj;
    private Object myTarget; // copied out of the qobj
    private RtSealer mySealer; // copied out of the qobj
    private Object[] myArgs; // copied out of the qobj
    private RtExceptionEnv myEE; // copied out of the qobj
    private int myQSeq; // copied out of the qobj

    Runlet(RtQObj qObj, Runlet parent) {
        myQObj = qObj;
        myParent = parent;
        myID = idNumber++;
        myTarget = qObj.getTarget();
        mySealer = qObj.getSealer();
        myArgs = qObj.getArgs();
        myEE = qObj.getKeeper();
        myQSeq = qObj.getSeqNum();
    }

    public void setUIRef(Object uiRef) {
        myUIRef = uiRef;
    }

    public Object getUIRef() {
        return myUIRef;
    }

    public int getID() {
        return myID;
    }

    public Object getQObject() {
        return myQObj;
    }

    public String parentString() {
        return myTarget.toString();
    }

    public String runStateString() {
        String runState = "   "; // None run, i.e. no consequences vector at all
        if (myState == RELEASED_RUN) runState = " R "; // Rarely seen, only in released Runlets.
        if (myConsequences != null) {
            int nrConsequences = myConsequences.size();

            // We always show "0" for no consequences.

            if (nrConsequences == 0) {
                runState = " 0 ";
            } else {

                // We always show "X" if all have been run

                if (myState == ALL_RUN) runState = " X "; // all run
                else {
                    
                    // We show count of consequences if some have been run
                    // but if we have more than 9 (so one digit would not be enough), show "*"

                    if (nrConsequences <= 9) runState = " " + nrConsequences + " "; // some run, nrconseqs <= 9
                    else runState = " * "; // some run, nrconseq > 9
                }
            }
        }
        return " (" + myID + ")" + runState;
    }

    public String sourceString() {
        if (myParent == null) return "???";
        String result = myParent.parentString();
        return result;
    }

    public String messageString() {
        return RtEnvelope.messageToString(myTarget, mySealer, myArgs, myEE);
    }

    public String targetString() {
        return myTarget.toString();
    }

    public Object sourceToInspect() {
        if (myParent == null) return null;
        return myParent.targetToInspect();
    }

    // XXX--should deprecate this!
    public Object messageToInspect() {
        return new RtEnvelope(mySealer, myArgs, myEE);
    }

    public Object targetToInspect() {
        return myTarget;
    }

    public String description(boolean verbose) {
        if (! verbose) return sourceString();
        return runStateString() + targetString() + " " + messageString() + " " + sourceString(); 
    }
    
    public String toString() {
        return "Runlet #" + myID;
    }

    public boolean isRunnable() {
        return myState == NOT_RUN;
    }

    public int colorNumber() {  // Avoid relying on IFC, use own color numbers.
        if (myState == NOT_RUN)         return VIRGIN_COLOR;
        if (myState == RELEASED_RUN)    return RELEASED_COLOR;
        if (myConsequences.size() == 0) return LEAF_COLOR;
        if (myState == ALL_RUN)         return DONE_COLOR;
        return SOME_COLOR;
    }

    public boolean allHaveBeenRun() {
        if (myState == ALL_RUN) {
            return true;
        }
        if (myState == RELEASED_RUN) {
            return true;
        }
        if (myConsequences == null) { // Don't update state to NOT_RUN since it may already 
            return false;       // be NONE_RUN but just not collected.
        }
        if (myConsequences.size() == 0) {
            myState = ALL_RUN;
            return true;
        }
        Enumeration e = myConsequences.elements();
        boolean foundRunOne = false;
        boolean foundUnRunOne = false;
        while (e.hasMoreElements()) {
            if (foundRunOne && foundUnRunOne) {
                myState = SOME_RUN; // We know we are at least partially run now
                return false;
            }
            if (((Runlet)(e.nextElement())).allHaveBeenRun()) foundRunOne = true;
            else foundUnRunOne = true;
        }
        if (foundRunOne && ! foundUnRunOne) {
            myState = ALL_RUN;      // Memeized function!
            return true;
        }
        myState = SOME_RUN; // We know we are at least partially run now
        return false;
    }

    void setRun() {
        myState = NONE_RUN;
    }

    void addConsequences(RtQ q) {
        myState = NONE_RUN;
        myConsequences = new Vector(q.length());
        while (!q.empty()) {
            myConsequences.addElement(new Runlet(q.dequeue(),this));
        }
    }

    public Vector consequences() {
        return myConsequences;
    }

    public Runlet parent() {
        return myParent;
    }

    /**

     * Returns the root of the current tree of runlets
     * that this one belongs to.

     */

    public Runlet root() {
        Runlet result = this;
        while (myParent != null) result = result.myParent;
        return result;
    }

    public static int collectEventsFromVector(Vector eventVector, Vector result, int count) {
        if (count <= 0) return count;
        Enumeration e = eventVector.elements();
        while (e.hasMoreElements())
            count = ((Runlet)e.nextElement()).collectEvents(result, count--);
        return count;
    }

    public int collectEvents(Vector result, int count) {
        if (count <= 0) return count;
        result.addElement(this);
        if (myConsequences != null)
            return collectEventsFromVector(myConsequences,result, count--);
        else return count--;
    }

    public static Runlet nextUnRunInVector(Vector conseqs, Runlet lastRunlet, int depth) {
        if (depth <= 0) return null;
        if (conseqs == null) return null;

        // Don't use enumerations when the vector may change under us!

        for (int i = 0; i < conseqs.size(); i++) {
            Runlet result = ((Runlet)conseqs.elementAt(i)).nextUnRun(lastRunlet, --depth);
            if (result != null) return result;
        }
        return null;
    }

    public Runlet nextUnRun(Runlet lastRunlet, int depth) {
        Runlet result = null;
        if (depth <= 0) {
            System.out.println("NextUnRun depth cutoff - null");
            return null;
        }
        if (myState == RELEASED_RUN) return null; // Shouldn't happen, really
        if (myState == NONE_RUN && myConsequences == null)
            throw new Error("nextUnRun() - Shouldn't happen - Run but uncollected event");
        if (myState == NOT_RUN) return this;

        // Note that we use --depth instead of depth-1 in severl
        // places.  The "depth" is really an "examined runlets" count
        // rather than a recursion depth.

        if (myState != ALL_RUN) {
            result = nextUnRunInVector(myConsequences, lastRunlet, --depth); // Recurse downward
            if (result != null) return result;
            else {

                // We can recurse upward exactly once - when we turn
                // into an ALL_RUN so that next time we won't get back
                // in here. If we do, we' d just bounce around until
                // we ran out of depth.

                myState = ALL_RUN;
                if (lastRunlet != null && myParent != null) { // can and should we recurse upward?
                    if (lastRunlet != myParent) {
                        return myParent.nextUnRun(lastRunlet, --depth);
                    }
                }
            }
        }
        return result;
    }

    public void releaseAllDescendants(RtQ freeQ) {
        if (isRunnable()) {     // If I'm runnable,
            freeQ.enqueue(myQObj); // Enqueue me on freeQ
            myState = RELEASED_RUN; // Unusual case - should never be encountered again
            myConsequences = null; // Should be a no-op.
            return;
        }
        // Otherwise go through my consequences,
        // recursively, looking for runnables.

        if (myConsequences != null) { // Shouldn't happen...
            Enumeration e = myConsequences.elements();
            while (e.hasMoreElements()) {
                ((Runlet)e.nextElement()).releaseAllDescendants(freeQ);
            }
        }
    }
}
