// TextViewHTMLContainer.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;


/** Instances of this class are used to store containers like STRONG or LI
  * If you need to add some support for another container, subclass
  * TextViewHTMLContainer and use <b>TextView.setHTMLContainerClass()</b> to
  * tell TextView to use your subclass.
  * @note 1.0 changes
  */
public abstract class TextViewHTMLContainer extends TextViewHTMLElement {
    /** The marker for this container */
    String marker;

    /** Attributes for this marker */
    String attributes;

    /** Children for this marker */
    Object children[] = new Object[0];

    /** Prefix cache for this container */
    String prefix = null;

    /** Suffix cache for this container */
    String suffix   = null;

    /** Length cache. */
    int lengths[];

    /** Public methods */

    /** You can override this method to return what string should prefix the
      * container.  This method is usualy used to add extra characters like
      * carriage returns. For example, Headers always start with a cariage
      * return. This method for an header should return a cariage return.
      * <b>context</b> is the context <b>lastchar</b> is the last character
      * added to the textView. It is often useful to check if lastChar is '\n'
      * before adding another '\n' The default implementation returns nothing.
      *
      */
     public String prefix(Hashtable context, char lastChar) {
         return "";
     }

    /** You can override this method to return what string should suffix the
      * container.  This method is usualy used to add extra characters like
      * carriage returns. For example, Headers always end with a cariage return.
      * This method for an header should return a cariage return. <b>context</b>
      * is the context <b>lastchar</b> is the last character added to the
      * textView. It is often useful to check if lastChar is '\n' before adding
      * another '\n' The default implementation returns nothing.
      *
      */
     public String suffix(Hashtable context, char lastChar) {
         return "";
     }

    /** Setup the context for children. Override this method and add or change
      * any key if you want to add some state for children. The default
      * implementation does nothing.
      *
      */
    public void setupContext(Hashtable context) {
    }

    /**
     *  Cleanup the context. If you have added some state in setupContext,
     *  you should override this method and remove any state added during
     *  setupContext()
     *  The default implementation does nothing
     */
    public void cleanupContext(Hashtable context) {
    }

    /**
     *  Compute the TextView attributes for the prefix according to the
     *  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes </b>.
     *  If you need to change the attributes, you should clone <b>initialAttributes</b> and
     *  return a new hashtable.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForPrefix(Hashtable context,Hashtable initialAttributes,
                                         TextView textView) {
        return initialAttributes;
    }

    /** Compute the TextView attributes for the container contents according to
      * the context and initial attributes.  Return the new attributes. The
      * default implementation returns <b>initialAttributes</b> If you need to
      * change the attributes, you should clone <b>initialAttributes</b> and
      * return a new hashtable. textView is the TextView for which the HTML is
      * parsed.
      *
      */
    public Hashtable attributesForContents(Hashtable context,
                                           Hashtable initialAttributes,
                                           TextView textView) {
        return initialAttributes;
    }

    /** Compute the TextView attributes for the suffix according to the context
      * and initial attributes.  Return the new attributes. The default
      * implementation returns <b> initialAttributes </b>  If you need to change
      * the attributes, you should clone <b>initialAttributes</b> and return a
      * new hashtable.  textView is the TextView for which the HTML is parsed.
      *
      */
    public Hashtable attributesForSuffix(Hashtable context,Hashtable initialAttributes,
                                         TextView textView) {
        return initialAttributes;
    }


    /** Return the string for all children.
      * The default implementation concatenates all children's strings and
      * will fill the lengths cache with the appropriate lengths.
      * The lengths cache is used to speedup the attributes setting phase.
      * You need to override this method only when implementating markers
      * producing attachment like Tables and TextArea. In this case you
      * want to return TextView.TEXT_ATTACHMENT_STRING.
      *
      */
     public String string(Hashtable context) {
         FastStringBuffer fb = new FastStringBuffer();
         int i,c;
         int length = 0;

         if( children.length > 0 )
             lengths = new int[children.length];

         for(i = 0 , c = children.length ; i < c ; i++ ) {
             ((TextViewHTMLElement)children[i]).appendString(context,fb);
             if( i == 0 ) {
                 lengths[i] = fb.length();
                 length = lengths[i];
             } else {
                 lengths[i] = fb.length() - length;
                 length += lengths[i];
             }
         }
         return fb.toString();
     }

    /** Return the children for this container.
      *
      */
    public Object[] children() {
        return children;
    }

    /** Convenience to return the children for this container
      * inside a Vector.
      *
      */
    public Vector childrenVector() {
        Vector v = new Vector();
        int i;

        for(i=0; i < children.length ; i++)
            v.addElement(children[i]);
        return v;
    }

    /** Return the marker for this container */
    public String marker() {
        return marker;
    }

    /** Return the HTML attributes in an hashtable.
      * Ex: for <foo bar=1> ... </foo> will produce an hashtable with
      * one key "FOO" (note upper case) with a value "1" as a string
      *
      */
    public Hashtable attributes() {
        return hashtableForHTMLAttributes(attributes);
    }


    /* Private methods */

    /** When parsing HTML,TextView will allocate one instance of TextViewHTMLContainer
      * for each recognized HTML containers and then call setMarker(), setAttributes() and
      * setChildren() on it.
      * The default implementation of setMarker() is to store the marker into the <b>
      * marker</b> instance variable.
      * @private
      */
    public void setMarker(String aString) {
        marker = aString;
    }

    /** When parsing HTML,TextView will allocate one instance of TextViewHTMLContainer
      * for each recognized HTML containers and then call setMarker(), setAttributes() and
      * setChildren() on it.
      * The default implementation of setAttributes() is to store the attributes into the <b>
      * attributes</b> instance variable.
      * @private
      */
    public void setAttributes(String attr) {
        attributes = attr;
    }

    /** When parsing HTML,TextView will allocate one instance of TextViewHTMLContainer
      * for each recognized HTML containers and then call setMarker(), setAttributes() and
      * setChildren() on it.
      * The default implementation of setChildren() is to store the children into the <b>
      * children</b> instance variable.
      * @private
      */
    public void setChildren(Object children[]) {
        if(children == null)
            this.children = new Object[0];
        else
            this.children = children;
    }

    void appendString(Hashtable context,FastStringBuffer fb) {
      char lastChar = (char) 0;

      if( fb.length() > 0 )
          lastChar = fb.charAt(fb.length() - 1);

      prefix = prefix(context,lastChar);
      if( prefix != null && prefix.length() > 0 )
          fb.append(prefix);

      if( children != null ) {
        setupContext(context);
        fb.append(string(context));
        cleanupContext(context);
      }

      if( fb.length() > 0 )
          lastChar = fb.charAt(fb.length() - 1);

      suffix = suffix(context,lastChar);
      if(suffix != null && suffix.length() > 0)
          fb.append(suffix);
    }


    /** Set the attributes for the string starting at index index
      *
      */
    void setAttributesStartingAt(int index, Hashtable initialAttributes,TextView target,
                                        Hashtable context) {
        int i,c,offset = 0;
        Hashtable newAttributes = null;

        if( prefix != null  && prefix.length() > 0) {
            newAttributes = attributesForPrefix(context,initialAttributes,target);
            if(newAttributes != initialAttributes)
                target.addAttributesForRange(newAttributes,new Range(index,prefix.length()));
            offset += prefix.length();
        }

        if( appliesAttributesToChildren() ) {
            if( children != null && children.length > 0 && lengths != null ) {
                newAttributes = attributesForContents(context,initialAttributes,target);
                setupContext(context);
                for(i=0,c=children.length ; i < c ; i++ ) {
                    ((TextViewHTMLElement)children[i]).setAttributesStartingAt( index + offset,
                                                                                newAttributes,
                                                                                target,context);
                    if( lengths != null )
                        offset += lengths[i];
                    else
                        offset += ((TextViewHTMLElement)children[i]).string(context).length();
                }
                cleanupContext(context);
            } else { /** Apply attributes when Length is 0
                      *  this can happen for a link destination
                      *  attribute.
                      */
                Range r = TextView.allocateRange(index + offset,0);
                newAttributes = attributesForContents(context,initialAttributes,target);
                target.addAttributesForRange(newAttributes,r);
                TextView.recycleRange( r );
            }
        } else {
            newAttributes = attributesForContents(context,initialAttributes,target);
            if(newAttributes != initialAttributes)
                target.addAttributesForRange(newAttributes,new Range(index + offset,
                                                                     string(context).length()));
        }

        if( suffix != null && suffix.length() > 0 ) {
            newAttributes = attributesForPrefix(context,initialAttributes,target);
            if( newAttributes != initialAttributes )
                target.addAttributesForRange(newAttributes,new Range(index + offset,suffix.length()));
            offset += suffix.length();
        }
    }

    /** Return <b>true</b> if this marker should applies contents attributes to
      * children.  <b>False</b> if attributes should be applied to the result of
      * string(contents). The default value is true. Override this method and
      * return false, if you are implementing a container that replaces its
      * children with a TextAttachment.
      *
      */
    public boolean appliesAttributesToChildren() {
        return true;
    }

    /**
     * @private
     */
    public void setString(String aString) {
    }



    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i;

        sb.append(marker + attributes);
        for(i=0; i < children.length ; i++ )
            sb.append(children[i].toString());
        return sb.toString();
    }
}

