// TextFieldOwner.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Interface implemented by objects wanting information on important TextField
  * events, such as when editing has completed.  An object implementing this
  * interface must make itself the TextField's owner using the TextField's
  * <b>setOwner()</b> method.
  */

public interface TextFieldOwner {
    /** Edit mode will end (or did end) because the TextField received a Tab
      * key.
      */
    public static final int     TAB_KEY = 0;

    /** Edit mode will or did end because the TextField received a
      * BackTab key.
      */
    public static final int     BACKTAB_KEY = 1;

    /** Edit mode will or did end because the TextField received a
      * Return key.
      */
    public static final int     RETURN_KEY = 2;

    /** Edit mode will or did end because another TextField acquired
      * key focus.
      */
    public static final int     LOST_FOCUS = 3;

    /** Edit mode will or did end  because the TextField resigned key
      * focus.
      */
    public static final int     RESIGNED_FOCUS = 4;



    /** Sent by <b>textField</b> upon entering edit mode.
     */
    public void textEditingDidBegin(TextField textField);

    /** Sent by <b>textField</b> each time the user changes the
      * <b>textField</b>'s contents.
      */
    public void textWasModified(TextField textField);

    /** Sent by <b>textField</b> when it is about to complete editing.
      * <b>endCondition</b> describes why editing will end, with the possible
      * values TAB_KEY, BACKTAB_KEY, RETURN_KEY, LOST_FOCUS, and
      * RESIGNED_FOCUS.  For TAB_KEY, BACKTAB_KEY, and RETURN_KEY, returning
      * <b>false</b> causes the TextField to remain in edit mode, and
      * returning <b>true</b> allows editing to complete.  With the LOST_FOCUS
      * and RESIGNED_FOCUS conditions, the TextField exits edit mode
      * regardless of the return value (when another TextField requests input
      * focus, or the TextField resigns it voluntarily, there's no way to
      * prevent it).  <b>contentsChanged</b> is <b>true</b> if
      * the TextField's contents have been modified since entering edit mode.
      */
    public boolean textEditingWillEnd(TextField textField, int endCondition,
                                      boolean contentsChanged);

    /** Sent by <b>textField</b> when text editing has completed.
      * <b>endCondition</b> describes why editing ended, with the possible
      * values TAB_KEY, BACKTAB_KEY, RETURN_KEY, LOST_FOCUS, and
      * RESIGNED_FOCUS.  <b>contentsChanged</b> is <b>true</b> if the
      * TextField's contents were modified since entering edit mode.<p>
      * If you need to implement special behavior when the user switches
      * TextFields using keystrokes (Tab, Return, and so on), do it in this
      * method.  For example, if you need to determine which TextField should
      * receive keys next without using TextField's existing
      * <b>setTabField()</b> and <b>setBacktabField()</b> methods, you could
      * use the following code for this method:
      * <pre>
      *     if (endCondition == TextFieldOwner.RETURN_KEY) {
      *         field1.selectText();
      *     } else if (endCondition == TextFieldOwner.TAB_KEY) {
      *         field2.selectText();
      *     } else if (endCondition == TextFieldOwner.BACKTAB_KEY) {
      *         field3.selectText();
      *     }
      * </pre>
      */
    public void textEditingDidEnd(TextField textField, int endCondition,
                                  boolean contentsChanged);
}

