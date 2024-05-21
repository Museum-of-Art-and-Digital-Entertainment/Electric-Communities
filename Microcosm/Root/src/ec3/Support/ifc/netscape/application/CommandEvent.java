// CommandEvent.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Event subclass that sends the <b>performCommand()</b> message to its Target
  * when processed.  To use, you must instantiate a CommandEvent, configure
  * its Target, command and data, and add it to an EventLoop.
  * @see Application#performCommandAndWait
  * @see Application#performCommandLater
  */
public class CommandEvent extends Event implements EventProcessor {
    Target      target;
    String      command;
    Object      data;

    /** Constructs a CommandEvent.
      */
    public CommandEvent() {
        super();
        setProcessor(this);
    }

    /** Convenience for constructing a CommandEvent. Equivalent
      * to the following code:
      * <pre>
      *     newEvent = new CommandEvent();
      *     newEvent.setTarget(target);
      *     newEvent.setCommand(command);
      *     newEvent.setData(data);
      * </pre>
      */
    public CommandEvent(Target target, String command, Object data) {
        this();
        setTarget(target);
        setCommand(command);
        setData(data);
    }

    /** Sets the CommandEvent's Target.
      */
    public void setTarget(Target target) {
        this.target = target;
    }

    /** Returns the CommandEvent's Target.
      * @see #setTarget
      */
    public Target target() {
        return target();
    }

    /** Sets the CommandEvent's command.
      */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Returns the CommandEvent's command.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Sets the CommandEvent's data object, the object sent in the
      * <b>performCommand()</b> message to its Target.
      * @see #setTarget
      */
    public void setData(Object data) {
        this.data = data;
    }

    /** Returns the CommandEvent's data object.
      * @see #setData
      */
    public Object data() {
        return data;
    }

    /** Called by an EventLoop to process the CommandEvent, which results in
      * the CommandEvent sending its Target the <b>performCommand()</b>
      * message. You should never call this method.
      */
    public void processEvent(Event event) {
        if (target != null) {
            target.performCommand(command, data);
        }
    }
}
