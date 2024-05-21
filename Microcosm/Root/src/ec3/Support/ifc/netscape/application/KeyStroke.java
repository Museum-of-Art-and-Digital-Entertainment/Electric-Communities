// KeyStroke.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.awt.Event;

/** A class to represent key strokes.
 * @private
 */
public class KeyStroke implements Codable {
    int key;
    int modifiers;

    final static String KEY_KEY = "key";
    final static String MODIFIERS_KEY = "modifiers";



    /** Default constructor for archiving **/
    public KeyStroke() {
        super();
    }

    /** Create a new key stroke that represents a <b>key</b> pressed
     *  with the <b>modifiers</b> down.
     */
    public KeyStroke(int key,int modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    /** Create a new key stroke that matches <b>anEvent</b> **/
    public KeyStroke(KeyEvent anEvent) {
        this(anEvent.key,anEvent.modifiers);
    }

    /** Return the key for this key stroke **/
    public int key() {
        return key;
    }

    /** Return the modifiers for this key stroke **/
    public int modifiers() {
        return modifiers;
    }

    public boolean equals(Object anotherKey) {
        if(!(anotherKey instanceof KeyStroke))
            return false;

        if(key == ((KeyStroke)anotherKey).key &&
           modifiers == ((KeyStroke)anotherKey).modifiers)
            return true;
        else {
            /** BIG hack to workaround keycode changing when control is down **/
            if((modifiers & KeyEvent.CONTROL_MASK) > 0) {
                int ak = (((KeyStroke)anotherKey).key() + 64);
                int mk = key;
                if(ak >= 'a' && ak <= 'z')
                    ak -= ('a' - 'A');
                if(mk >= 'a' && mk <= 'z')
                    mk -= ('a' - 'A');
                if(ak == mk)
                    return true;
                else
                    return false;
            }
            else
                return false;
        }
    }

    public boolean matchesKeyEvent(KeyEvent anEvent) {
        if( anEvent == null )
            return false;
        if( anEvent.type == KeyEvent.KEY_DOWN &&
            anEvent.key == key &&
            anEvent.modifiers == modifiers )
            return true;
        else
            return false;
    }

   public int hashCode() {
        return ("" + this).hashCode();
   }

   public String toString() {
       return "KeyStroke ("+key+","+modifiers+")";
   }

    /* archiving */

    /** Describes the KeyStroke's class information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.KeyStroke", 1);
        info.addField(KEY_KEY, INT_TYPE);
        info.addField(MODIFIERS_KEY,INT_TYPE);
    }

    /** Encodes the View instance.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(KEY_KEY,key);
        encoder.encodeInt(MODIFIERS_KEY,modifiers);
    }

    /** Decodes the View instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        key = decoder.decodeInt(KEY_KEY);
        modifiers = decoder.decodeInt(MODIFIERS_KEY);
    }

    /** Finishes the View instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
