// Event.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Abstract Object subclass representing a user or program action, such as a
  * mouse click, key press, and so on.  Events are created in response to these
  * actions and directed to the application's EventLoop.  The EventLoop
  * removes each Event and forwards it to the Event's processor, the object
  * responsible for determining which object should ultimately receive the
  * Event.  In most cases, this object is the application itself.<p>
  * In general, you never work directly with the event processing mechanism.
  * However, if you do need to create an Event subclass requiring special
  * processing, you can set the Event subclass' processor to the object that
  * knows how to process it, and hand the Event to the application's EventLoop
  * using the code:
  * <pre>
  *     Application.application().eventLoop().addEvent(newEvent);
  * </pre>
  *
  * @see MouseEvent
  * @see KeyEvent
  * @see EventLoop
  * @see Application
  */


public class Event implements Cloneable {
    EventProcessor      processor;
    Object              synchronousLock;
    long                timeStamp;
    int                 type;

/* constructors */

    /** Constructs an Event, setting its time stamp to the current time. */
    public Event() {
        this(System.currentTimeMillis());
    }

    /** Constructs an Event, setting its time stamp to <b>timeStamp</b>. */
    public Event(long timeStamp) {
        super();
        this.timeStamp = timeStamp;
    }

    /** Sets the Event's type to <b>aType</b>.
      */
    public void setType(int aType) {
        type = aType;
    }

    /** Returns the Event's type.
      * @see #setType
      */
    public int type() {
        return type;
    }

    /** Sets the Event's time stamp to <b>timeStamp</b>.  You will almost never
      * call this method.  Instead, construct an Event with the correct
      * time stamp.
      * @see Event(long)
      */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /** Returns the Event's time stamp.
      */
    public long timeStamp() {
        return timeStamp;
    }

    /** Sets the Event's processor to <b>aProcessor</b>, the object that
      * determines how to respond to the Event once it has been removed
      * from the EventLoop.
      * @see EventProcessor
      */
    public void setProcessor(EventProcessor aProcessor) {
        processor = aProcessor;
    }

    /** Returns the Event's processor.
      * @see #setProcessor
      */
    public EventProcessor processor() {
        return processor;
    }

    /** Clones the Event.
      */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InconsistencyException(this +
                                         ": clone() not supported :" + e);
        }
    }

    synchronized Object synchronousLock() {
        return synchronousLock;
    }

    synchronized Object createSynchronousLock() {
        if (synchronousLock != null) {
            throw new InconsistencyException(
                    "Can't create synchronous lock if one is already set");
        } else {
            synchronousLock = new Object();
            return synchronousLock;
        }
    }

    synchronized void clearSynchronousLock() {
        if (synchronousLock == null) {
            throw new InconsistencyException(
                            "Can't clear synchronous lock if one isn't set");
        } else {
            synchronousLock = null;
        }
    }
}
