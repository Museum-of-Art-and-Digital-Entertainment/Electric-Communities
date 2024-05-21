/*
 * @(#)ObjKeyTable.java 0.01 96/05/15
 *
 */

package ec.util;

import ec.tables.IntTable;
import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * ObjKeyTable class. Maps keys to values. Any object can be used 
 * as a key, but the values are ints. <p>
 *
 * To successfully store and retrieve objects from a hash table, the
 * object used as the key must implement the hashCode() methods.<p>
 *
 * The 'useEquals' construction argument determines whether 'equals()'
 * or '==' is the equality operator used.
 */
public class ObjKeyTable implements Cloneable {
    
    /**
     * Return this user-supplied value when a key is not found.
     */
    private int myNoSuchKey;
    
    private IntTable myTable;

    private ObjKeyTable(IntTable table) {
        myTable = table;
    }

    /**
     * Constructs a new, empty hashtable with the specified initial 
     * capacity and the specified load factor.
     *
     * @param noSuchKey A reserved number that should never be used as
     * a value.  This number is then used to indicate that a key-value
     * association was not found.
     * @param initialCapacity the initial number of buckets
     * @param useEquals whether to use the equals() method or '=='
     * @param loadFactor a number between 0.0 and 1.0, it defines
     *      the threshold for rehashing the hashtable into
     *      a bigger one.
     * @exception IllegalArgumentException If the initial capacity
     * is less than or equal to zero.
     * @exception IllegalArgumentException If the load factor is
     * less than or equal to zero.
     */
    public ObjKeyTable(int noSuchKey,
                       int initialCapacity,
                       boolean useEquals,
                       float loadFactor) {

        myNoSuchKey = noSuchKey;
        //XXX loadFactor ignored
        myTable = new IntTable(!useEquals, initialCapacity);
    }
    

    /**
     * Constructs a new, empty hashtable with the specified initial 
     * capacity and comparison type.
     *
     * @param noSuchKey A reserved number that should never be used as
     * a value.  This number is then used to indicate that a key-value
     * association was not found.
     * @param initialCapacity the initial number of buckets
     * @param useEquals whether to use the equals() method or '=='
     */
    public ObjKeyTable(int noSuchKey, int initialCapacity, boolean useEquals) {
        this(noSuchKey, initialCapacity, useEquals, 0.75f);
    }
    

    /**
     * Constructs a new, empty hashtable with the specified initial 
     * capacity.  'useEquals' defaults to false, ie, '==' is the
     * default equality operator.
     *
     * @param noSuchKey A reserved number that should never be used as
     * a value.  This number is then used to indicate that a key-value
     * association was not found.
     * @param initialCapacity the initial number of buckets
     */
    public ObjKeyTable(int noSuchKey, int initialCapacity) {
        this(noSuchKey, initialCapacity, false, 0.75f);
    }
    

    /**
     * Constructs a new, empty hashtable. A default capacity and load factor
     * is used. Note that the hashtable will automatically grow when it gets
     * full.  'useEquals' defaults to false, ie, '==' is the default
     * equality operator. 
     *
     * @param noSuchKey A reserved number that should never be used as
     * a value.  This number is then used to indicate that a key-value
     * association was not found.
     */
    public ObjKeyTable(int noSuchKey) {
        this(noSuchKey, 101, false, 0.75f);
    }
    

    /**
     * Returns whether this ObjKeyTable is using 'equals()' for
     * equality.  If it isn't, then it's using '==' instead.
     */
    public boolean usingEquals() {
        return ! myTable.isIdentity();
    }
    

    /**
     * Returns the number, reserved by the 'noSuchKey' construction
     * argument, that should not be used as a value in this
     * ObjKeyTable. 
     */
    public int noSuchKey() {
        return myNoSuchKey;
    }
    

    /**
     * Returns the number of elements contained in the hashtable. 
     */
    public int size() {
        return myTable.size();
    }
    

    /**
     * Returns true if the hashtable contains no elements.
     */
    public boolean isEmpty() {
        return myTable.isEmpty();
    }
    
    /**
     * Returns an enumeration of the hashtable's keys.
     * @see ObjKeyTable#elements
     * @see Enumeration
     */
    public Enumeration keys() {
        return myTable.keys();
    }
    

    /**
     * Returns an enumeration of the elements. Use the Enumeration methods 
     * on the returned object to fetch the elements sequentially.
     * @see ObjKeyTable#keys
     * @see Enumeration
     */
    public Enumeration elements() {
        return myTable.elements();
    }
    
    
    /**
     * Returns true if the collection contains an element for the key.
     * @param key the key that we are looking for
     * @see ObjKeyTable#contains
     */
    public boolean containsKey(Object key) {
        nonNull(key);
        return myTable.containsKey(key);
    }
    

    /**
     * Gets the object associated with the specified key in the 
     * hashtable.
     * @param key the specified key
     * @returns the element for the key or the 'noSuchKey'
     * construction argument if the key is not defined in the hash table.
     * @see ObjKeyTable#put
     */
    public int get(Object key) {
        nonNull(key);
        return myTable.getInt(key, myNoSuchKey);
    }
    
    
    /**
     * Puts the specified element into the hashtable, using the specified
     * key.  The element may be retrieved by doing a get() with the same key.
     * The key cannot be null, and the value cannot be the 'noSuchKey'
     * construction argument.
     * @param key the specified key in the hashtable
     * @param value the specified element
     * @exception NullPointerException If the key of the element 
     * is equal to null.
     * @see ObjKeyTable#get
     * @return the old value of the key, or noSuchKey if it did not have one.
     */
    public int put(Object key, int value) {
        if (value == myNoSuchKey) {
            throw new NullPointerException
              ("value " + value + " not allowed in this ObjKeyTable");
        }
        int result = get(key);
        myTable.putInt(key, value);
        return result;
    }
    

    /**
     * Removes the element corresponding to the key. Does nothing if the
     * key is not present.
     * @param key the key that needs to be removed
     * @return the value of key, or noSuchKey if the key was not found.
     */
    public int remove(Object key) {
        int result = get(key);
        myTable.remove(key);
        return result;
    }
    

    /**
     * Clears the hash table so that it has no more elements in it.
     */
    public void clear() {
        myTable.clear();
    }
    

    /**
     * Creates a clone of the hashtable. A shallow copy is made,
     * the keys and elements themselves are NOT cloned. This is a
     * relatively expensive operation.
     */
    public Object clone() {
        return new ObjKeyTable((IntTable)myTable.clone());
    }
    

    /**
     * Converts to a rather lengthy String.
     */
    public String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        Enumeration k = keys();
        Enumeration e = elements();
        buf.append("{");
        for (int i = 0; i <= max; i++) {
            String s1 = k.nextElement().toString();
            String s2 = e.nextElement().toString();
            buf.append(s1 + "=" + s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append("}");
        return buf.toString();
    }


    /**
     *
     */
    private void nonNull(Object obj) {
        if (obj == null) {
            throw new NullPointerException("nulls not allowed in ObjKeyTable");
        }
    }
}
