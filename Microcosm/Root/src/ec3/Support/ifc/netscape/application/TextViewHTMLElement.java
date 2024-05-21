// TextViewHTMLElement.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

/** This abstract class describes an HTML element.
  * It also provides some general conveniences to produce some strings
  * and attributes for TextView.
  * You never subclass TextViewHTMLElement directly. You subclass
  * TextViewHTMLString, TextViewHTMLMarker or TextViewHTMLContainer.
  *
  * @note 1.0 changes
  */
public abstract class TextViewHTMLElement implements HTMLElement{

    /** Convenience to convert an attribute string to an attribute Hashtable
     *  return null if the string is empty or does not contain
     *  HTML attributes syntax.
     */
    public Hashtable hashtableForHTMLAttributes(String attr) {
        Hashtable h = null;
        if( attr != null ) {
            try {
                h = HTMLParser.hashtableForAttributeString(attr);
            } catch (HTMLParsingException e) {
                h = null;
            }
        }
        return h;
    }

    /** Convenience to extract a font from a TextView attribute dictionary.
     *  If the dictionary contains a font, this font will be returned. Otherwise,
     *  this method will try the TextView's default font. If no font is found,
     *  return Font.defaultFont()
     */
    public Font fontFromAttributes(Hashtable attr,TextView textView) {
        Font f = (Font)attr.get(TextView.FONT_KEY);
        if( f == null ) {
            attr = textView.defaultAttributes();
            f = (Font)attr.get(TextView.FONT_KEY);
            if( f == null )
                f = Font.defaultFont();
        }
        return f;
    }

    /** Return it's string according to a context */
    public abstract String string(Hashtable context);

    /** Append the string for the component */
    abstract void appendString(Hashtable context,FastStringBuffer fb);


    /** Set the attributes for the string starting at index index */
    abstract void setAttributesStartingAt(int index, Hashtable initialAttributes,TextView target,
                                          Hashtable context);

    /** This method is called with the marker in argument if appropriate.
     * @private
     */
    public void setMarker(String aString) { }

    /** This method is called with the attributes in argument if appropriate
     * @private
     */
    public void setAttributes(String attributes) { }

    /** This method is called with the string in argument if appropriate.
     * @private
     */
    public void setString(String aString) { }

    /** This method is called if the HTMLElement is a container.
     * child[] contains some other HTML elements.
     * null is sent to this method if the container has no children
     * @private
     */
    public void setChildren(Object child[]) { }
}




