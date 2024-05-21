// TextViewHTMLMarker.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;
import java.net.*;

/** Instances of this class are used to store single markers like IMG or HR.
  * If you need to add some support for another marker, subclass
  * TextViewHTMLMarker and use <b>TextView.setHTMLMarkerClass()</b> to tell
  * TextView to use your subclass.
  *
  * @note 1.0 changes
  */
public abstract class TextViewHTMLMarker extends TextViewHTMLElement {
    /** The marker */
    String marker;

    /** The marker attributes */
    String attributes;

    /** prefix cache */
    String prefix;

    /** String cache */
    String string;

    /** suffix cache */
    String suffix;

    /* Public methods */

    /**
     * You can override this method to return what string should prefix the marker.
     * This method is usualy used to add extra characters like cariage returns.
     * <b>context</b> is the context
     * <b>lastchar</b> is the last character added to the textView. It is often useful to
     * check if lastChar is '\n' before adding another '\n'
     * The default implementation returns nothing.
     */
     public String prefix(Hashtable context, char lastChar) {
         return "";
     }

    /**
     * You can override this method to return what string should suffix the marker.
     * This method is usualy used to add extra characters like cariage returns.
     * <b>context</b> is the context
     * <b>lastchar</b> is the last character added to the textView. It is often useful to
     * check if lastChar is '\n' before adding another '\n'
     * The default implementation returns nothing.
     */
     public String suffix(Hashtable context, char lastChar) {
         return "";
     }


    /**
     *  Compute the TextView attributes for the prefix according to the
     *  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes </b>.
     *  It is not necessary to allocate a new hashtable.You can just
     *  modify initialAttributes and return it.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForPrefix(Hashtable context,Hashtable initialAttributes,
                                         TextView textView) {
        return initialAttributes;
    }

    /**
     *  Compute the TextView attributes for the marker itself (the string
     *  returned by string() according to  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes</b>
     *  It is not necessary to allocate a new hashtable.You can just
     *  modify initialAttributes and return it.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForMarker(Hashtable context,Hashtable initialAttributes,
                                         TextView textView) {
        return initialAttributes;
    }

    /**
     *  Compute the TextView attributes for the suffix according to the
     *  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes </b>.
     *  It is not necessary to allocate a new hashtable.You can just
     *  modify initialAttributes and return it.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForSuffix(Hashtable context,Hashtable initialAttributes,
                                         TextView textView) {
        return initialAttributes;
    }


    /** Return the string for this marker given the context <b>context</b>
     *  You have to override this method.
     */
    public abstract String string(Hashtable context);


    /** Return the HTML marker */
    public String marker() {
        return marker;
    }

    /** Return the HTML attributes in an hashtable.
      * Ex: for <foo bar=1> will produce an hashtable with
      * one key "FOO" with a value "1" as a string
      */
    public Hashtable attributes() {
        return hashtableForHTMLAttributes(attributes);
    }

    /** Private methods */

    /** When parsing HTML,TextView will allocate one instance of TextViewHTMLMarker
      * for each recognized HTML markers and then call setMarker() and setAttributes()
      * on it.
      * The default implementation of setMarker() is to store the marker into the <b>
      * marker</b> instance variable.
      * @private
      */
    public void setMarker(String aString) {
        marker = aString;
    }

    /** When parsing HTML,TextView will allocate one instance of TextViewHTMLMarker
      * for each recognized HTML markers and then call setMarker() and setAttributes()
      * on it.
      * The default implementation of setAttributes() is to store the attributes into the <b>
      * attributes</b> instance variable.
      * Use hashtableForHTMLAttributes() to convert the <b>attr</b> to a useable hashtable.
      * You probably want to delay this conversion as long as possible to keep the number
      * of allocated objets as low as possible.
      * @private
      */
    public void setAttributes(String attr) {
        attributes = attr;
    }

    void appendString(Hashtable context,FastStringBuffer fb) {
        char lastChar = (char) 0;

        if( fb.length() > 0 )
            lastChar = fb.charAt(fb.length() - 1);

        prefix = prefix(context,lastChar);
        if(prefix != null && prefix.length() > 0)
            fb.append(prefix);

        string = string(context);
        if( string != null && string.length() > 0 )
            fb.append(string);
        else
            string = "";

        if( fb.length() > 0 )
            lastChar = fb.charAt(fb.length() - 1);

        suffix = suffix(context,lastChar);
        if(suffix != null && suffix.length() > 0)
            fb.append(suffix);
    }

    void setAttributesStartingAt(int index,Hashtable initialAttributes,
                                 TextView textView, Hashtable context) {

        int offset = 0;
        Hashtable newAttributes;

        if( prefix != null  && prefix.length() > 0) {
            newAttributes = attributesForPrefix(context,initialAttributes,textView);
            if( newAttributes != initialAttributes )
                textView.addAttributesForRange(newAttributes,new Range(index,prefix.length()));
            offset += prefix.length();
        }

        if( string == null )
            string = string(context);

        if( string != null && string.length() > 0 ) {
            newAttributes = attributesForMarker(context,initialAttributes,textView);
            textView.addAttributesForRange(newAttributes,new Range(index+offset,string.length()));
            offset += string.length();
        }

        if( suffix != null && suffix.length() > 0 ) {
            newAttributes = attributesForPrefix(context,initialAttributes,textView);
            if( newAttributes != initialAttributes )
                textView.addAttributesForRange(newAttributes,new Range(index + offset,suffix.length()));
            offset += suffix.length();
        }
    }


    /** @private */
    public void setChildren(Object child[]) {
    }

    /** @private */
    public void setString(String aString) {
    }

    public String toString() {
        return marker + attributes;
    }
}



