/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/util/Queue.java $
    $Revision: 1 $
    $Date: 12/27/97 4:09p $
    $Author: Sbl $

    Todo:

****************************************************************************/

package dom.util;

import java.util.*;

/**
 * A thread-safe queue for arbitrary objects.
 * <p>
 * Provides a deadlock-free way for threads to exchange objects
 * with one another.  
 * <p>
 * NOTE:  This queue assumes that there will potentially be
 * multiple producing threads (multiple threads adding to this queue),
 * and <b>one</b> consumer thread (one thread to call the dequeue/peekQueue
 * methods).  It does <b>not</b> assume that there will be multiple consumer
 * threads.
 *
 * @see java.lang.Thread
 * @version $Revision: 7 $ $Date: 12/27/97 4:09p $
 * @author  Scott B. Lewis
 */
public class Queue implements Enqueable
{
    protected Vector myQueue;
    protected boolean myStopped;

    /**
     * Constructor for a queue.
     */
    public Queue()
    {
        myQueue = new Vector();
        myStopped = false;
    }

    /**
     * Add an object to this queue.  Multiple threads can add objects to this queue
     * at will, and this method will serialize their placement onto this queue.
     *
     * @param obj the Object to add to the queue
     */
    public synchronized boolean enqueue(Object obj)
    {
        // If this queue has been stopped already, no more items can be added
        if (isStopped()) {
            return false;
        }
        // Add item to the vector
        myQueue.addElement(obj);
        // Notify any waiting thread
        notify();
        return true;
    }

    /**
     * Wait for something to do.  Blocks until something is available from queue.
     * Returns null immediately if the queue is shut down or thread is interrupted.  
     *
     * @return  an object previously added to the queue, or null if there is no more work
     */
    public synchronized Object dequeue()
    {
        Object val = peekQueue();
        if (val != null) {
            removeFirstElement();
        }
        return val;
    }

    /**
     * Look at the top object on the queue (without removing it).  Blocks until something
     * is available.  Returns null immediately if the queue is shut down or thread
     * is interrupted.
     *
     * @return  an object previously added to the queue, or null if there is no more work
     */
    public synchronized Object peekQueue()
    {
        while (isEmpty()) {
            if (myStopped) {
                return null;
            } else {
                try {
                    wait();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return myQueue.firstElement();
    }
    
    /**
     * Remove first element in queue
     * <p>
     * NOTE:  If this method is called directly from user code (rather than
     * via the dequeue method), then the caller must be sure that an element
     * currently exists on the queue (e.g. via the peekQueue method), to avoid
     * throwing a runtime exception.
     */
    public synchronized void removeFirstElement()
    {
        myQueue.removeElementAt(0);
    }

    /**
     * Check whether this queue currently has an items in it.  Returns true if
     * the queue is empty, false if it is not empty.
     *
     * @return true if empty, false if not empty
     */
    public boolean isEmpty()
    {
        return myQueue.isEmpty();
    }

    /**
     * Stop this queue.  Once called, this queue cannot be reused.
     */
    public synchronized void stop()
    {
        myStopped = true;
    }

    /**
     * Method for checking whether this queue has already been stopped.
     *
     * @return true if this queue has been stopped, false otherwise
     */
    public boolean isStopped()
    {
        return myStopped;
    }

    /**
     * Flush any/all items from this queue.  Unconditionally removes any queue elements.
     */
    public synchronized void flush()
    {
        myQueue.removeAllElements();
    }

    /**
     * Ends processing for this queue.  Once this is called, the queue can no longer be
     * used.
     */
    public synchronized void close()
    {
        stop();
        notifyAll();
    }
    
}

