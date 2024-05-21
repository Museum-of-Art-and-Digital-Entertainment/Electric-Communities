package ec.e.util;

/**
 * A singly linked list of object holders, used internally by the
 * queue classes.  The last entry on the linked list is the sentry,
 * which isn't interpreted to hold a value.  Instead, it is a
 * rendezvous point for when the reader catches up with the writer.
 */
class QueueEntry {

    /** 
     * myValue suspect, nullOk;
     */
    private Object myValue = null;

    /** 
     * myNext nullOk;
     */
    private QueueEntry myNext = null;
    
    /**
     * Only used to initialize a queue.  Makes the initial sentry.
     */
    QueueEntry() {}

    /**
     * Used to grow a queue.  Makes and returns the new sentry,
     * turning the old sentry into a non-sentry by having it point at
     * this new sentry, and setting its value to the latest element
     * added to the queue.
     *
     * @param newValue suspect, nullOk;
     */
    QueueEntry(Object newValue, QueueEntry previousSentry) {
        /*
         * Order matters here!  Once myNext is non-null, a
         * QueueReader, which may be operating in a separate thread,
         * will consider myValue to be meaningful.  Therefore we must not
         * set myNext until myValue is valid.
         */
        previousSentry.myValue = newValue;
        previousSentry.myNext = this;
    }

    /**
     * @return suspect nullOk;
     */
    Object value() {
        return myValue;
    }

    /**
     * @return nullOk; Returns the next QueueEntry, or null if this is
     * the sentry.
     */
    QueueEntry next() {
        return myNext;
    }
}
