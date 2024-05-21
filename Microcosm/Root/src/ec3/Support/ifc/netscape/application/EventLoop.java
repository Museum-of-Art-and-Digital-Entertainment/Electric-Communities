// EventLoop.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Class for managing a sequence of Events. In general, you never create
  * an EventLoop.  Instead you retrieve the main one from the Application.
  * @see Application#eventLoop
  * @note 1.0 protected code around thread priority switching
  * @note 1.0 adde internal calls for will/didProcessEvent
  */
public class EventLoop implements Runnable {
    Vector events = new Vector();
    Thread mainThread;
    private boolean shouldRun=false;
    static boolean letAwtThreadRun = true;

    /** Constructs an EventLoop. */
    public EventLoop() {
        super();
    }

    /** Places <b>anEvent</b> on the EventLoop's event queue.
     */
    public void addEvent(Event anEvent) {
        synchronized (events) {
            events.addElement(anEvent);
            events.notify();
        }
    }

    final void letAWTThreadRun() {
        if (letAwtThreadRun) {
            Thread thread;
            int priority, tmpPriority;

            if (!shouldProcessSynchronously()) {
                return;
            }

            thread = Thread.currentThread();
            priority = thread.getPriority();
            tmpPriority = priority - 1;

            if (tmpPriority < Thread.MIN_PRIORITY)
                tmpPriority = Thread.MIN_PRIORITY;

            try {
                thread.setPriority(tmpPriority);
                Thread.yield();
                thread.setPriority(priority);
            } catch (SecurityException e) {
                letAwtThreadRun = false;
            }
        }
    }

    /** Removes all occurrences of <b>anEvent</b> from the event queue.
      */
    public void removeEvent(Event anEvent) {
        synchronized (events) {
            events.removeElement(anEvent);
        }
    }

    /** Allows an object to search or modify the EventLoop's Vector of
      * outstanding events.  Upon calling <b>filterEvents()</b>, <b>filter</b>
      * receives a <b>filterEvents()</b> message, with
      * the events Vector as an argument.  The EventLoop locks the Vector
      * before calling the filter's <b>filterEvents()</b> method, so no other
      * threads can modify its contents.  This method returns the results of
      * the filter's <b>filterEvents()</b> method.
      * @see EventFilter#filterEvents
      */
    public Object filterEvents(EventFilter filter) {
        Object returnValue;

        letAWTThreadRun();

        synchronized (events) {
            returnValue = filter.filterEvents(events);
            events.notify();
        }
        return returnValue;
    }

    /** Removes and returns the next Event from the EventLoop's event queue.
      */
    public Event getNextEvent() {
        Event        nextEvent = null;

        synchronized(events) {
            while (events.count() == 0) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                }
            }
            nextEvent = (Event)events.removeFirstElement();
        }
// ALERT! THIS SHOULD NOT HAPPEN
//        if (nextEvent == null) {
//          print something bad here
//        }
        return nextEvent;
    }


    /** Returns the next Event from the EventLoop's event queue, but does
      * not remove it.
      */
    public Event peekNextEvent() {
        Event        nextEvent;

        letAWTThreadRun();

        synchronized (events) {
            nextEvent = (Event)events.firstElement();
        }

        return nextEvent;
    }

    /** This method is called to process each Event as the EventLoop removes
      * it from its queue.
      */
    public void processEvent(Event nextEvent) {
        Object lock = nextEvent.synchronousLock();
        Application application = Application.application();

        // ALERT!  May want a try/catch here so that didProcessEvent() can
        // clean up.

        application.willProcessInternalEvent(nextEvent);
        application.willProcessEvent(nextEvent);

        nextEvent.processor().processEvent(nextEvent);
        if (lock != null) {
            synchronized (lock) {
                nextEvent.clearSynchronousLock();
                lock.notify();
            }
        }

        application.didProcessEvent(nextEvent);
        application.didProcessInternalEvent(nextEvent);
    }

    /** Runnable interface method implemented to process Events as they
      * appear in the queue.
      */
    public void run() {
        Event   nextEvent, secondEvent;

        synchronized (this) {
            if (mainThread != null) {
                throw new InconsistencyException(
                                    "Only one thread may run an EventLoop");
            }

            mainThread = Thread.currentThread();
            shouldRun = true;
        }
        while (shouldRun) {
            /* remove the next event from the queue and release it */
            nextEvent = getNextEvent();
            if (shouldRun) { /* shouldRun might have changed during
                                getNextEvent() */
                try {
                    processEvent(nextEvent);
                } catch (Exception e) {
                    System.err.println(
                        Application.application().exceptionHeader());
                    e.printStackTrace(System.err);
                    System.err.println("Restarting EventLoop.");
                }
            }
        }

        /* ALERT: We may want to wakeup any thread
         * waiting for an event to be processed.
         * This is risky though since the event has not
         * been really processed.
         */

        synchronized (this) {
            mainThread = null;
        }
    }

    /** Stops the EventLoop once control returns from processing the current
      * Event. This method might be called from the AWT thread.
      */
    synchronized public void stopRunning() {
        Event stopEvent = new ApplicationEvent();

        shouldRun = false; /* Set shouldRun to false to make sure that
                              we don't compute any other events
                            */
        stopEvent.type = ApplicationEvent.STOP;
        addEvent(stopEvent);
    }

    /** Returns <b>true</b> if the EventLoop is currently running. */
    public synchronized boolean isRunning() {
        return shouldRun;
    }

    synchronized boolean shouldProcessSynchronously() {
        return (mainThread == null || Thread.currentThread() == mainThread);
    }

    /** Adds <b>event</b> to the EventLoop's event queue and waits for the main
      * application thread to process it.  This method should only be called
      * from within threads <i>other than</i> the main thread.
      */
    public void addEventAndWait(Event event) {
        if (Thread.currentThread() == mainThread) {
            throw new InconsistencyException(
                        "Can't call addEventAndWait from " +
                                "within the EventLoop's main thread");
        } else {
            Object lock = event.createSynchronousLock();

            synchronized (lock) {
                addEvent(event);
                while (event.synchronousLock() != null) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    /** Returns the EventLoop's String representation. */
    public synchronized String toString() {
        return events.toString();
    }
}
