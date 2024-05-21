package ec.e.util;

/**
 * A singly linked list of object holders, used internally by the
 * queue classes.  The last entry on the linked list is the sentry,
 * which isn't interpreted to hold a value.  Instead, it is a
 * rendezvous point for when the reader catches up with the writer.
 */
class SimpleQueueEntry {

    /** 
     * myValue suspect, nullOk;
     */
    private Object myValue = null;

    /** 
     * myNext nullOk;
     */
    private SimpleQueueEntry myNext = null;
    
    /**
     * Only used to initialize a queue.  Makes the initial sentry.
     */
    SimpleQueueEntry() {}

    /**
     * Used to grow a queue.  Makes and returns the new sentry,
     * turning the old sentry into a non-sentry by having it point at
     * this new sentry, and setting its value to the latest element
     * added to the queue.
     *
     * @param newValue suspect, nullOk;
     */
    SimpleQueueEntry(Object newValue, SimpleQueueEntry previousSentry) {
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
     * @return nullOk; Returns the next SimpleQueueEntry, or null if this is
     * the sentry.
     */
    SimpleQueueEntry next() {
        return myNext;
    }
}

