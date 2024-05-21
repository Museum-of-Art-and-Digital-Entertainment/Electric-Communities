/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DObjectRunner.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.util.Queue;

/**
 * A Runnable class to provide the behavior for DObject message processing.
 * This is the actual message processing loop for all DObjects.
 * The thread created by the View (upon request of a newly constructed DObject)
 * is passed an instance of this class, and the run method below is what
 * the given Thread executes.  In this implementation, the thread simply
 * peels instances of the Closure class off of the given Queue object, and
 * calls the 'runClosure' method on the owning DObject with the given Closure.
 * If an exception occurs, the DObject's handleMessageException method is
 * called to deal with the problem.
 */
public class DObjectRunner implements Runnable {

    private DObject myTarget;
    private Queue myQueue;

    /**
     * Protected constructor means that only the View code can create i
     * instances of this class.
     *
     * @param target the DObject that this belongs to
     * @param aQueue the Queue instance that will hold messages for
     * the given DObject
     */
    protected DObjectRunner(DObject target, Queue aQueue) {
        myTarget = target;
        myQueue = aQueue;
    }

    /**
     * Actual message processing loop right here
     */
    public void run()
    {
       Closure closureToRun = null;
        while ((closureToRun = (Closure) myQueue.dequeue()) != null) {
            try {
                // Right here is where all asynchronous messages to our DObject instance
                // get processed.  This is *the* message processing loop for the owning
                // DObject, and it *only* executes code for this DObject
                myTarget.runClosure(closureToRun);
                // Catch everything...this way this thread will never terminate until
                // the message queue is closed (in DObject.destroy())
            } catch (Throwable e) {
                myTarget.handleMessageException(closureToRun,e);
            }
        }
        myTarget.debug("Exiting run queue thread for "+myTarget+" with id "+myTarget.getID());
    }

}
