// TimerQueue.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Private class to manage a queue of Timers. The Timers are chained
  * together in a linked list sorted by the order in which they will expire.
  * @note 1.0 changes to detect and stop dealocking better
  */
class TimerQueue implements Runnable, ApplicationObserver  {
    Timer firstTimer;
    boolean running;

    public TimerQueue() {
        super();

        Application application  = Application.application();

        application.addObserver(this);
        if (application.isRunning()) {
            start();
        }
    }

    synchronized void start() {
        if (running) {
            throw new InconsistencyException("Can't start a TimerQueue that is already running");
        } else {
            Thread timerThread = new Thread(this, "TimerQueue");

            try {
                if( timerThread.getPriority() > Thread.MIN_PRIORITY )
                   timerThread.setPriority(timerThread.getPriority() - 1);

                timerThread.setDaemon(true);
            } catch (SecurityException e) {
            }
            timerThread.start();
            running = true;
        }
    }

    synchronized void stop() {
        running = false;
        notify();
    }

    synchronized void addTimer(Timer timer, long expirationTime) {
        Timer previousTimer, nextTimer;

        // If the Timer is already in the queue, then ignore the add.

        if (timer.running)
            return;

        previousTimer = null;
        nextTimer = firstTimer;

        // Insert the Timer into the linked list in the order they will
        // expire.  If two timers expire at the same time, put the newer entry
        // later so they expire in the order they came in.

        while (nextTimer != null) {
            if (nextTimer.expirationTime > expirationTime)
                break;

            previousTimer = nextTimer;
            nextTimer = nextTimer.nextTimer;
        }

        if (previousTimer == null)
            firstTimer = timer;
        else
            previousTimer.nextTimer = timer;

        timer.expirationTime = expirationTime;
        timer.nextTimer = nextTimer;
        timer.running = true;

        notify();
    }

    synchronized void removeTimer(Timer timer) {
        boolean found;
        Timer previousTimer, nextTimer;

        if (!timer.running)
            return;

        previousTimer = null;
        nextTimer = firstTimer;
        found = false;

        while (nextTimer != null) {
            if (nextTimer == timer) {
                found = true;
                break;
            }

            previousTimer = nextTimer;
            nextTimer = nextTimer.nextTimer;
        }

        if (!found)
            return;

        if (previousTimer == null)
            firstTimer = timer.nextTimer;
        else
            previousTimer.nextTimer = timer.nextTimer;

        timer.expirationTime = 0;
        timer.nextTimer = null;
        timer.running = false;
    }

    synchronized boolean containsTimer(Timer timer) {
        return timer.running;
    }

    // If there are a ton of timers, this method may never return.  It loops
    // checking to see if the head of the Timer list has expired.  If it has,
    // it posts the Timer and reschedules it if necessary.

    synchronized long postExpiredTimers() {
        long currentTime, timeToWait;
        Timer timer;

        // The timeToWait we return should never be negative and only be zero
        // when we have no Timers to wait for.

        do {
            timer = firstTimer;
            if (timer == null)
                return 0;

            currentTime = System.currentTimeMillis();
            timeToWait = timer.expirationTime - currentTime;

            if (timeToWait <= 0) {
                timer.post(currentTime);
                removeTimer(timer);

                // This tries to keep the interval uniform at the cost of
                // drift.

                if (timer.repeats())
                    addTimer(timer, currentTime + timer.delay());

                // Allow other threads to call addTimer() and removeTimer()
                // even when we are posting Timers like mad.  Since the wait()
                // releases the lock, be sure not to maintain any state
                // between iterations of the loop.

                try {
                    wait(1);
                } catch (InterruptedException e) {
                }
            }
        } while (timeToWait <= 0);

        return timeToWait;
    }

    public synchronized void run() {
        long timeToWait;

        while (running) {
            timeToWait = postExpiredTimers();
            try {
                wait(timeToWait);
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized String toString() {
        StringBuffer buf;
        Timer nextTimer;

        buf = new StringBuffer();
        buf.append("TimerQueue (");

        nextTimer = firstTimer;
        while (nextTimer != null) {
            buf.append(nextTimer.toString());

            nextTimer = nextTimer.nextTimer;
            if (nextTimer != null)
                buf.append(", ");
        }

        buf.append(")");
        return buf.toString();
    }

    /*
     * Informs the observer that the application's focused view has changed.
     */
    public void focusDidChange(Application application,View focusedView) {
    }

    /*
     * Informs the observer that the application's current document window
     * has changed.
     */
    public void currentDocumentDidChange(Application application,Window document) {
    }

    public void applicationDidPause(Application app) {
    }

    public  void applicationDidResume(Application app) {
    }

    public synchronized void applicationDidStart(Application app) {
        start();
    }

    public synchronized void applicationDidStop(Application app) {
        stop();
    }
}
