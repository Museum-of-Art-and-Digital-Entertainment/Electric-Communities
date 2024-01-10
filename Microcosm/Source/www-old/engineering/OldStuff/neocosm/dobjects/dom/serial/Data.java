/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/serial/SerializableData.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.serial;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A serializable dictionary, that uses a Hashtable underneath.  It is
 * meant to be serialized, and so implements the Serializable interface.  It is used
 * so that DObjects (and the View itself) can have a generic data structure for sending
 * arbitrary key (string) -> serializable object relations.
 *
 * @see DObjectPacket
 * @see SessionViewPacket
 * 
 * @author Scott Lewis
 */
public class Data implements Serializable {

    Hashtable myElements;
    
    /** 
     * Create an instance of a new serializable dictionary for sending arbitrary
     * key (String) -> serializable object relations
     */
    public Data()
    {
        myElements = new Hashtable();
    }
    /**
     * Put a new element in the serializable dictionary.  Caller must guarantee
     * uniqueness of key.
     *
     * @param key the String key for this element
     * @param element the Serializable element to be associated with the given key.  Should
     * not be null.
     */
    public void put(String key, Serializable element)
    {
        myElements.put(key, element);
    }

    /**
     * Get a serializable object from this dictionary given a String key.
     *
     * @param key the String key to look for.
     * @return Object that corresponds to given key.  If not found, null is returned.
     */
    public Object get(String key)
    {
        return myElements.get(key);
    }
    
    /**
     * Put Serializable element into this dictionary using an integer as a key.
     * Caller must guarantee uniqueness of index.
     *
     * @param index the int index to associate with the given element
     * @param element the Serializable element to add to the dictionary
     */
    public void put(int index, Serializable element)
    {
        put(Integer.toString(index), element);
    }
    
    /**
     * Get an object out of this dictionary given an integer key.
     *
     * @param index the int index to use as the key
     * @return Object that corresponds to key in this dictionary.  Returns null if
     * object not found
     */
    public Object get(int index)
    {
        return get(Integer.toString(index));
    }
    
    /**
     * Provide information about the current number of key->element mappings in 
     * this dictionary.
     *
     * @return int number of elements in dictionary
     */
    public int size()
    {
        return myElements.size();
    }
    
    /**
     * Get an Enumeration of all of the current keys (Strings) currently in this
     * dictionary.  All elements of the Enumeration will be of type String.
     *
     * @return Enumeration of the current keys in this dictionary
     */
    public Enumeration keys()
    {
        return myElements.keys();
    }
    
    /**
     * Return an Enumeration of the elements that are currently in this dictionary.
     *
     * @return Enumeration of all elements (values) in this dictionary
     */
    public Enumeration elements()
    {
        return myElements.elements();
    }
    
    /**
     * Return String representation of this dictionary.  Mostly used for debugging.
     *
     * @return String representation of this dictionary
     */
    public String toString()
    {
        return "Data("+minimalString()+")";
    }
    
    /**
     * Return a minimal string for this dictionary.
     *
     * @return String that is a 'small' representation of this dictionary.  Just includes
     * information on the elements of the dictionary, no information about the dictionary
     * as a whole.
     */
    public String minimalString()
    {
        String elements = "";
        for(Enumeration e=myElements.keys(); e.hasMoreElements(); )
        {
            String key = (String) e.nextElement();
            Object value = myElements.get(key);
            if (elements != "") elements = elements+",";
            elements = elements+"["+key+","+value+"]";
        }
        return elements;
    }
    
}