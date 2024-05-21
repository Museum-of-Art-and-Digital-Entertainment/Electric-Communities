package ec.e.util.crew;

/**
 * @see ec.e.util.crew.Queue
 * @see ec.e.util.crew.QueueReader
 */
public class QueueWriter {

    /**
     * Points at the last QueueEntry on the queue, which is therefore
     * the queue sentry.  While it is a sentry, it is not taken to
     * have a value.
     */
    private QueueEntry mySentry;

    /** 
     * myLock nullOk;
     */
    private Object myLock;

    /** 
     * @param lock nullOk;
     */
    QueueWriter(QueueEntry sentry, Object lock) {
        mySentry = sentry;
        myLock = lock;
    }

    /**
     * Add an element to the tail of the queue.
     *
     * @param newValue suspect, nullOk;
     */
    public void enqueue(Object newValue) {
        /*
         * The old mySentry is set to point at a new QueueEntry.  The
         * new QueueEntry, because it has no next(), is a sentry and
         * becomes the new mySentry.  The old mySentry, because it now
         * has a next() is now the last non-sentry, so we also set it
         * to hold the newValue added to the queue.  If anyone was
         * waiting for this to happen, they are woken up.
         */
        mySentry = new QueueEntry(newValue, mySentry);

        /*
         * As far as I can tell, this test is the only extra runtime
         * cost paid for using one body of code for both the
         * "QueueReader waits" case and the "QueueReader complains"
         * case. 
         */
        if (myLock != null) {
            synchronized (myLock) {
                myLock.notifyAll();
            }
        }
    }
}
