// Target.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Interface enabling a generalized object request framework. Objects
  * need to ask other objects to perform certain actions. It may not be
  * feasible, however, for the sender to know the exact message to send to the
  * target object at compile time. With the Target interface, the sender does
  * not have to know anything about the target except that it implements the
  * Target interface. The string <b>command</b> describes the action that the
  * target should perform, with the arbitrary datum <b>data</b>. For example,
  * when pressed, a Button needs to ask some object to perform a specific
  * action. Rather than subclass Button to connect it to a specific method in a
  * specific class, Button sends its messages to a Target instance, passing a
  * string command (set as appropriate) and itself as the object.
  */


public interface Target {
    /** Tells the target to perform the command <b>command</b>, using datum
      * <b>data</b>.
      */
    public void performCommand(String command, Object data);
}

