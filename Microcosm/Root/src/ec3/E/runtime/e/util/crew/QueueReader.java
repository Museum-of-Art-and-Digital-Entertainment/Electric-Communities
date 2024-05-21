package ec.e.util.crew;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.InterruptedException;


/**
 * A QueueReader is an Enumerator that will enumerate the elements of
 * the queue so far.  Unlike other Enumerators, this one can report
 * later that it hasMoreElements() despite having earlier reported
 * that it hasn't, since these could have been added in the
 * meantime.  If one asks for the nextElement() without first checking
 * that the queue hasMoreElements(), one will either get a 
 * NoSuchElementException or wait until the next element shows up,
 * depending on whether the Queue constructor was provided with a lock
 * object.  Once the queue report that it hasMoreElements(), the next
 * nextElement() is guaranteed to succeed without blocking.
 *
 * @see ec.e.util.crew.Queue
 * @see ec.e.util.crew.QueueWriter
 */
public class QueueReader implements Enumeration {

    /**
     * myHead is the QueueEntry which does or will hold the next
     * value.  If its next() is null, it's a sentry and does not yet
     * hold a value.  Otherwise, the value is there.
     */
    private QueueEntry myHead;

    /**
     * myLock nullOk;
     */
    private Object myLock;

    /** 
     * @param lock nullOk;
     */
    QueueReader(QueueEntry first, Object lock) {
        myHead = first;
        myLock = lock;
    }

    /**
     * Are there <em>currently</em> any more elements in the queue?
     */
    public boolean hasMoreElements() {
        /*
         * Only if myHead isn't a sentry.
         */
        return myHead.next() != null;
    }

    /**
     * If the Queue constructor was provided with a lock object, then
     * nextElement() waits until there is a next element and returns
     * it.  Otherwise, it returns the next element if there already is
     * one, or complains if not.
     *
     * @return suspect, nullOk;
     * @exception NoSuchElementException thrown if the queue is empty
     * and no lock was provided to the Queue constructor.
     */
    public Object nextElement() throws NoSuchElementException {
        QueueEntry next = myHead.next();
        if (next == null) {
            try {
                synchronized (myLock) {
                    while (next == null) {
                        try {
                            myLock.wait();
                            next = myHead.next();
                        } catch (InterruptedException ex) {
                            /*
                             * An InterruptedException is ignored on
                             * purpose, ie, we keep waiting until next
                             * != null, because if someone wants to
                             * wake up a queue reading thread by a
                             * pre-arranged convention, it's better to
                             * just put a marker into the queue.
                             */
                        }
                    }
                }
            } catch (NullPointerException ex) {
                /*
                 * Assumes that 'myLock == null' is the problem.
                 * We're already paying for the null check in
                 * 'synchronized', we might as use it to realize we
                 * are a non-synchronizing queue.
                 */
                throw new NoSuchElementException("queue is empty");
            }
        }
                    
        //at this point, myHead is known not to be a sentry
        Object result = myHead.value();
        myHead = next;
        return result;
    }


    /**
     * Count the number of elements on the queue.
     *
     * @return count of queue elements.
     */
    public int queueCount()  {
        QueueEntry next = myHead.next();
        int count = 0;

        while (null != next) {
            count++;
            next = next.next();
        }
                    
        return count;
    }
}
