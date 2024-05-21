package ec.e.run;

/**
 * RtQ does not synchronizes itself, because it's only for the E
 * runQ, which is synchronized with the vatLock.
 * XXX-NOTE: Made public for Inspector. Was package scope.
 */
public class RtQ
{
    private static final int INITIAL_SIZE = 400;

    RtQueueTimer myTimer;
    private RtQObj[] myQObjs;
    private int myMaxSize;
    private int myCurSize;
    private int myOut;
    private int myIn;

    public RtQ() {
        myTimer = null;
        myQObjs = new RtQObj[INITIAL_SIZE];
        myMaxSize = INITIAL_SIZE;
        myCurSize = 0;
        myOut = 0;
        myIn = 0;
    }

    /** Is this queue empty? */
    public boolean empty() {
        return (myCurSize == 0);
    }

    /** How many elements in the queue? */
    public int length() {
        return myCurSize;
    }

    /** Get the least-recently-added element off of the queue. */
    public final RtQObj dequeue() {
        if (myCurSize == 0) {
            if (Trace.eruntime.debug && Trace.ON) {
                Trace.eruntime.debugm(
                    "attempt to dequeue from an empty run queue; this" +
                    " is normal but you might be interested anyway.");
            }
            return null;
        }

        RtQObj result = myQObjs[myOut];

        myQObjs[myOut] = null;
        myOut++;
        if (myOut == myMaxSize) {
            myOut = 0;
        }
        myCurSize--;

        return result;
    }

    /** Add a new element to the queue. */
    public void enqueue(RtQObj newQObj) {
        // grow array if necessary
        if (myCurSize == myMaxSize) {
            int newSize = (myMaxSize * 3) / 2 + 10;
            RtQObj[] newQObjs = new RtQObj[newSize];

            // note: careful code to avoid inadvertantly reordrering messages
            System.arraycopy(myQObjs, myOut, newQObjs, 0, myMaxSize - myOut);
            if (myOut != 0) {
                System.arraycopy(myQObjs, 0, newQObjs, myMaxSize - myOut,
                    myOut);
            }

            if (Trace.eruntime.debug && Trace.ON) {
                Trace.eruntime.debugm("increasing run queue size from " + 
                    myMaxSize + " to " + newSize);
            }

            myOut = 0;
            myIn = myMaxSize;
            myQObjs = newQObjs;
            myMaxSize = newSize;
        }

        myQObjs[myIn] = newQObj;
        myIn++;
        if (myIn == myMaxSize) {
            myIn = 0;
        }
        myCurSize++;

        if (myTimer != null) {
            myTimer.putNow(newQObj);
        }
    }

    /** Debugging aid: give this run queue a timing service to use */
    public void startTiming(RtQueueTimer aTimer) {
        myTimer = aTimer;
    }

    /** Debugging aid: get rid of any in-use timing service */
    public void stopTiming() {
        myTimer = null;
    }

    /** Debugging aid: is this queue currently using a timing service? */
    public boolean isTimed() {
        return (myTimer != null);
    }
}

