// Timer.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that causes an action to occur at a predefined rate.  For
  * example, an animation object can use a Timer as the trigger for drawing its
  * next frame.  Each Timer has a Target that receives a
  * <b>performCommand()</b> message; the command the Timer sends to its Target;
  * and a delay (the time between <b>performCommand()</b> calls).  When
  * delay milliseconds have passed, a Timer sends the <b>performCommand()</b>
  * message to its Target, passing as parameters the command and the object set
  * using the Timer's <b>setData()</b> method.  This cycle repeats until
  * <b>stop()</b> is called, or halts immediately if the Timer is configured
  * to send its message just once.<p>
  * Using a Timer involves first creating it, then starting it using
  * the <b>start()</b> method.
  * @see Target
  * @note 1.0 changes to detect and stop dealocking better
  * @note 1.0 calling setDelay() on already running Timer works correctly
  */
public class Timer extends Object implements EventProcessor, EventFilter {
    EventLoop   eventLoop;
    Target      target;
    String      command;
    Object      data;
    long        timeStamp;
    int         initialDelay, delay;
    boolean     repeats = true, coalesce = true, removeEvents;

    // These fields are maintained by TimerQueue.

    long expirationTime;
    Timer nextTimer;
    boolean running;

    /** Constructs a Timer associated with the EventLoop <b>eventLoop</b> that
      * sends <b>performCommand()</b> to <b>target</b> every <b>delay</b>
      * milliseconds.  You only call this constructor if you need to associate
      * a Timer with an EventLoop other than the application's EventLoop.
      * @see Timer(Target, String, int)
      */
    public Timer(EventLoop eventLoop, Target target, String command,
                 int delay) {
        if (eventLoop == null) {
            throw new InconsistencyException("eventLoop parameter is null");
        }
        this.eventLoop = eventLoop;
        this.target = target;
        this.command = command;
        setDelay(delay);
        setInitialDelay(delay);
    }

    /** Constructs a Timer associated with the application's EventLoop that
      * sends <b>performCommand()</b> to <b>target</b> every <b>delay</b>
      * milliseconds.
      */
    public Timer(Target target, String command, int delay) {
        this(Application.application().eventLoop(), target, command, delay);
    }

    /** Returns the timer queue. */
    TimerQueue timerQueue() {
        return Application.application().timerQueue();
    }

    /** Returns the EventLoop associated with this Timer.
      */
    public EventLoop eventLoop() {
        return eventLoop;
    }

    /** Sets the Target that will receive <b>performCommand()</b> messages
      * from the Timer.
      */
    public void setTarget(Target target) {
        this.target = target;
    }

    /** Returns the Target that will receive <b>performCommand()</b>
      * messages from the Timer.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Sets the command the Timer sends to its target in the
      * <b>performCommand()</b> method.
      * @see #setTarget
      */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Returns the command the Timer sends to its target in the
      * <b>performCommand()</b> message.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Sets the data sent with the command in the <b>performCommand()</b>
      * message to its target.
      */
    public void setData(Object data) {
        this.data = data;
    }

    /** Returns the data sent with the command in the <b>performCommand()</b>
      * message to its target.
      * @see #setData
      */
    public Object data() {
        return data;
    }

    /** Sets the Timer's delay, the number of milliseconds between successive
      * <b>performCommand()</b> messages to its Target.
      * @see #setTarget
      * @see #setInitialDelay
      */
    public void setDelay(int delay) {
        TimerQueue        queue;

        if (delay < 0) {
            throw new InconsistencyException(
                                            "Invalid initial delay: " + delay);
        }
        this.delay = delay;

        if (isRunning()) {
            queue = timerQueue();
            queue.removeTimer(this);
            removeEvents();
            queue.addTimer(this, System.currentTimeMillis() + delay);
        }
    }

    /** Returns the Timer's delay.
      * @see #setDelay
      */
    public int delay() {
        return delay;
    }

    /** Sets the Timer's initial delay.  This is the number of milliseconds
      * the Timer will wait before sending its first <b>performCommand()</b>
      * message to its Target.  This setting has no effect if the Timer is
      * already running.
      * @see #setTarget
      * @see #setDelay
      */
    public void setInitialDelay(int initialDelay) {
        if (initialDelay < 0) {
            throw new InconsistencyException("Invalid initial delay: " +
                                          initialDelay);
        }
        this.initialDelay = initialDelay;
    }

    /** Returns the Timer's initial delay.
      * @see #setDelay
      */
    public int initialDelay() {
        return initialDelay;
    }

    /** If <b>flag</b> is <b>false</b>, instructs the Timer to send a
      * <b>performCommand()</b> message to its Target only once, and then stop.
      */
    public void setRepeats(boolean flag) {
        repeats = flag;
    }

    /** Returns <b>true</b> if the Timer will send a <b>performCommand()</b>
      * message to its target multiple times.
      * @see #setRepeats
      */
    public boolean repeats() {
        return repeats;
    }

    /** Returns the time stamp, in milliseconds, associated with the Timer's
      * most recent message to its Target.
      */
    public long timeStamp() {
        return timeStamp;
    }

    /** Sets whether the Timer coalesces multiple pending
      * <b>performCommand()</b> messages.  A busy application may not be able
      * to keep up with a Timer's message generation, causing multiple
      * <b>performCommand()</b> message sends to be queued.  When processed,
      * the application sends these messages one after the other, causing the
      * Timer's Target to receive a sequence of <b>performCommand()</b>
      * messages with no delay between them. Coalescing avoids this situation
      * by reducing multiple pending messages to a single message send. Timers
      * coalesce their message sends by default.
      */
    public void setCoalesce(boolean flag) {
        coalesce = flag;
    }

    /** Returns <b>true</b> if the Timer coalesces multiple pending
      * <b>performCommand()</b> messages.
      * @see #setCoalesce
      */
    public boolean doesCoalesce() {
        return coalesce;
    }

    /** Starts the Timer, causing it to send <b>performCommand()</b> messages
      * to its Target.
      * @see #stop
      */
    public void start() {
        timerQueue().addTimer(this,
                              System.currentTimeMillis() + initialDelay());
    }

    /** Returns <b>true</b> if the Timer is running.
      * @see #start
      */
    public boolean isRunning() {
        return timerQueue().containsTimer(this);
    }

    /** Stops a Timer, causing it to stop sending <b>performCommand()</b>
      * messages to its Target.
      * @see #start
      */
    public void stop() {
        timerQueue().removeTimer(this);
        removeEvents();
    }

    synchronized void removeEvents() {
        removeEvents = true;
        eventLoop.filterEvents(this);
    }

    synchronized boolean peekEvent() {
        removeEvents = false;
        return (eventLoop.filterEvents(this) != null);
    }

    /** EventFilter interface method, implemented to perform coalescing.  You
      * never call this method.
      * @see #setCoalesce
      */
    public Object filterEvents(Vector events) {
        int count = events.count();

        while (count-- > 0) {
            Event event = (Event)events.elementAt(count);

            if (event.processor() == this) {
                if (removeEvents) {
                    events.removeElementAt(count);
                } else {
                    return event;
                }
            }
        }
        return null;
    }

    /** EventProcessor interface method implemented to perform the Timer's
      * event processing behavior, which is to send the <b>performCommand()</b>
      * message to its Target.  You never call this method.
      * @see EventProcessor
      */
    public void processEvent(Event event) {
        timeStamp = event.timeStamp;
        if (target != null) {
            target.performCommand(command, data);
        }
    }

    /** Returns the Timer's string representation.
      */
    public String toString() {
        return "Timer {target = " + target + "; command = " + command +
               "; delay = " + delay + "; initialDelay = " + initialDelay +
               "; repeats = " + repeats + "}";
    }

    void post(long timeStamp) {
        Event      newEvent;

        if (!coalesce || !peekEvent()) {
            newEvent = new Event(timeStamp);
            newEvent.setProcessor(this);
            eventLoop.addEvent(newEvent);
        }
    }
}
