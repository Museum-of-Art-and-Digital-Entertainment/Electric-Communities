// TextViewHTMLString.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

/** Instances of this class are used to store strings while converting HTML to
  * strings and attributes.
  * If you need to add some processing on strings, subclass TextViewHTMLString
  * and use <b>TextView.setHTMLStringClass()</b> to tell TextView to use your
  * subclass.
  *
  * @note 1.0 changes
  */
public class TextViewHTMLString extends TextViewHTMLElement {
    /** Ivar to store the string for this instance */
    String string;


    /* Public methods */


    /** Return the string for the component, given the context <b>context</b>
     *  override this method if you want to perform some computation on the
     *  string according to <b>context</b>. The default implementation is
     *  equivalent to string()
     */
    public String string(Hashtable context) {
        return string;
    }


    /** Return the string */
    public String string() {
        return string;
    }


    /* Private methods */

    void appendString(Hashtable context,FastStringBuffer fb) {
      fb.append(string(context));
    }

    /** Set the attributes for the string starting at index index */
    void setAttributesStartingAt(int index, Hashtable attributes,TextView target,
                                 Hashtable context) {
      Range  r;
      String s = string(context);
      int length = s.length();
      if( attributes != null  && length > 0) {
          r = TextView.allocateRange(index,length);
          target.addAttributesForRange( attributes , r );
          TextView.recycleRange(r);
      }
    }
    /** When parsing HTML, TextView will allocate an instance of the HTML
      * string class for each strings and then call setString() on it.
      * @private
      */
    public void setString(String aString) {
        string = aString;
    }

    /** @private */
    public void setMarker(String aString) {
    }

    /** @private */
    public void setAttributes(String attributes) {
    }

    /** @private */
    public void setChildren(Object child[]) {
    }

    public String toString() {
        return string;
    }
}



