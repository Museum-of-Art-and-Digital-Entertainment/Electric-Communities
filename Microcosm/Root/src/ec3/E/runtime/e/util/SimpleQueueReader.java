package ec.e.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A SimpleQueueReader is an Enumerator that will enumerate the elements of
 * the queue so far.  Unlike other Enumerators, this one can report
 * later that it hasMoreElements() despite having earlier reported
 * that it hasn't, since these could have been added in the
 * meantime.  If one asks for the nextElement() without first checking
 * that the queue hasMoreElements(), one will get a 
 * NoSuchElementException.  Once the queue report that it
 * hasMoreElements(), the next nextElement() is guaranteed to succeed.
 *
 * @see ec.e.util.SimpleQueue
 * @see ec.e.util.SimpleQueueWriter
 */
public class SimpleQueueReader implements Enumeration {

    /**
     * myHead is the SimpleQueueEntry which does or will hold the next
     * value.  If its next() is null, it's a sentry and does not yet
     * hold a value.  Otherwise, the value is there.
     */
    private SimpleQueueEntry myHead;

    /** 
     */
    SimpleQueueReader(SimpleQueueEntry first) {
        myHead = first;
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
     * It returns the next element if there already is one, or
     * complains if not. 
     *
     * @return suspect, nullOk;
     * @exception NoSuchElementException thrown if the queue is empty
     */
    public Object nextElement() /*throws NoSuchElementException*/ {
        SimpleQueueEntry next = myHead.next();
        if (next == null) {
            throw new NoSuchElementException("queue is empty");
        }
                    
        //at this point, myHead is known not to be a sentry
        Object result = myHead.value();
        myHead = next;
        return result;
    }
}


