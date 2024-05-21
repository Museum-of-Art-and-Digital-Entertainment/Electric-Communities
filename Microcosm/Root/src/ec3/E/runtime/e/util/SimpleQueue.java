package ec.e.util;

/**
 * A SimpleQueue is a stream of entries with two sides, the reader and the
 * writer.  The SimpleQueueReader will enumerate all the elements added to
 * the queue so far, in the order they were written.  Using the
 * SimpleQueueWriter, one can add further entries to the queue, which the
 * SimpleQueueReader will then be able to enumerate. <p>
 *
 * The SimpleQueue class itself is an artifact of not being able to return
 * multiple values in Java.  Better would have simply been to have a
 * constructor that directly returned both reader and writer. <p>
 *
 * The SimpleQueue's internal data structure is a "half-party", it is half
 * of the party data structure invented by Norm, Dean, et al, for use
 * in Channels.
 *
 * The SimpleQueue is "simple" by contrast with ec.e.util.crew.Queue,
 * which supports multi-threaded use.  The SimpleQueue interface is a
 * strict subset of the Queue interface, and the Queue is fully
 * upwards compatible from the SimpleQueue.
 *
 * @see ec.e.util.crew.Queue
 * @see ec.e.util.SimpleQueueReader
 * @see ec.e.util.SimpleQueueWriter
 */
public class SimpleQueue {

    private SimpleQueueReader myReader;
    private SimpleQueueWriter myWriter;

    /**
     * Creates a new SimpleQueue.  
     */
    public SimpleQueue() {
        SimpleQueueEntry firstSentry = new SimpleQueueEntry();
        myReader = new SimpleQueueReader(firstSentry);
        myWriter = new SimpleQueueWriter(firstSentry);
    }

    /**
     * for consuming queue elements
     */
    public SimpleQueueReader reader() {
        return myReader;
    }

    /**
     * for producing queue elements
     */
    public SimpleQueueWriter writer() {
        return myWriter;
    }
}

