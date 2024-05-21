package ec.e.util;

/**
 * @see ec.e.util.SimpleQueue
 * @see ec.e.util.SimpleQueueReader
 */
public class SimpleQueueWriter {

    /**
     * Points at the last SimpleQueueEntry on the queue, which is therefore
     * the queue sentry.  While it is a sentry, it is not taken to
     * have a value.
     */
    private SimpleQueueEntry mySentry;

    /** 
     */
    SimpleQueueWriter(SimpleQueueEntry sentry) {
        mySentry = sentry;
    }

    /**
     * Add an element to the tail of the queue.
     *
     * @param newValue suspect, nullOk;
     */
    public void enqueue(Object newValue) {
        /*
         * The old mySentry is set to point at a new SimpleQueueEntry.  The
         * new SimpleQueueEntry, because it has no next(), is a sentry and
         * becomes the new mySentry.  The old mySentry, because it now
         * has a next() is now the last non-sentry, so we also set it
         * to hold the newValue added to the queue.
         */
        mySentry = new SimpleQueueEntry(newValue, mySentry);
    }
}
    

