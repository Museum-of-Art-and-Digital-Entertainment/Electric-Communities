// TextViewOwner.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Interface implemented by objects wanting information on important TextView
  * events, such as when the selection has changed.  An object implementing
  * this interface must make itself the TextView's owner using TextView's
  * <b>setOwner()</b> method.
  */

public interface TextViewOwner {

    /** Sent by <b>textView</b> upon entering edit mode.
      */
    public void textEditingDidBegin(TextView textView);

    /** Sent by <b>textView</b> when text editing has completed.
      */
    public void textEditingDidEnd(TextView textView);

    /** Sent by <b>textView</b> before <b>textView</b> replaces the characters
      * defined by <b>aRange</b> with a different string.
      */
    public void textWillChange(TextView textView, Range aRange);

    /** Sent by <b>textView</b> after <b>textView</b> has replaced a range
      * of characters with new characters. <b>aRange</b> represents the
      * range of new characters.
      */
    public void textDidChange(TextView textView, Range aRange);

    /** Sent by <b>textView</b> before <b>textView</b> changes the attributes
      * of characters within the range <b>aRange</b>.
      */
    public void attributesWillChange(TextView textView, Range aRange);

    /** Sent by <b>textView</b> after <b>textView</b> has changed the
      * attributes of characters within the range <b>aRange</b>.
      */
    public void attributesDidChange(TextView textView,Range aRange);

   /** Sent by <b>textView</b> after the selection changes. Use
     * <b>selectedRange()</b> to determine the current selection range.
     */
    public void selectionDidChange(TextView textView);

   /** Sent by <b>textView</b> when the user clicks an HTML link within the
     * TextView. <b>linkRange</b> is the link's range within the TextView.
     * <b>stringURL</b> contains the link's URL, which can be relative.
     */
    public void linkWasSelected(TextView sender, Range linkRange,
                                String stringURL);
}


