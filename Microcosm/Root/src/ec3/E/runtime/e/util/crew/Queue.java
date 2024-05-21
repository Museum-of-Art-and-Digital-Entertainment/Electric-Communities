package ec.e.util.crew;

/**
 * A Queue is a stream of entries with two sides, the reader and the
 * writer.  The QueueReader will enumerate all the elements added to
 * the queue so far, in the order they were written.  Using the
 * QueueWriter, one can add further entries to the queue, which the
 * QueueReader will then be able to enumerate. <p>
 *
 * These queue classes are intended for multi-threaded use, but under
 * a simplifying (and lock-reducing) assumption: Only one thread will
 * be invoking the object at each side of the queue, so the queue can
 * be used in a consumer/producer pattern between these <em>two</em>
 * threads. <p>
 *
 * The Queue class itself is an artifact of not being able to return
 * multiple values in Java.  Better would have simply been to have a
 * constructor that directly returned both reader and writer. <p>
 *
 * The Queue's internal data structure is a "half-party", it is half
 * of the party data structure invented by Norm, Dean, et al, for use
 * in Channels.
 *
 * @see ec.e.util.crew.QueueReader
 * @see ec.e.util.crew.QueueWriter
 */
public class Queue {

    private QueueReader myReader;
    private QueueWriter myWriter;

    /**
     * Creates a new Queue.  
     *
     * @param lock nullOk; The lock passed in is the one to be used
     * for synchronization.  If the lock should be private to this
     * queue, 'new Object()' is a fine expression for the lock
     * argument.  If 'lock' is null the QueueReader will complain
     * (with a NoSuchElementException) rather than wait.
     */
    public Queue(Object lock) {
        QueueEntry firstSentry = new QueueEntry();
        myReader = new QueueReader(firstSentry, lock);
        myWriter = new QueueWriter(firstSentry, lock);
    }

    /**
     * Identical to Queue(null);
     */
    public Queue() {
        this(null);
    }

    /**
     * for consuming queue elements
     */
    public QueueReader reader() {
        return myReader;
    }

    /**
     * for producing queue elements
     */
    public QueueWriter writer() {
        return myWriter;
    }
}
