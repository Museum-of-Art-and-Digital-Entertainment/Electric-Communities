/*
 * IntKeyTable.java 0.0 96/05/14
 *
 */

package ec.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * IntKeyTable collision list.
 */
class IntKeyTableEntry {
    int key;
    Object value;
    IntKeyTableEntry next;

    protected Object clone() {
    IntKeyTableEntry entry = new IntKeyTableEntry();
    entry.key = key;
    entry.value = value;
    entry.next = (next != null) ? (IntKeyTableEntry)next.clone() : null;
    return entry;
    }
}

/**
 * IntKeyTable class. Maps keys to values. Keys are of type int
 * and values can be any Object.
 *
 */
public class IntKeyTable implements Cloneable {
    /**
     * Use the equals method for comparing values,
     * or else use '=='.
     */
    boolean useEquals = false;

    /**
     * The hash table data.
     */
    private IntKeyTableEntry table[];

    /**
     * The total number of entries in the hash table.
     */
    private int count;

    /**
     * Rehashes the table when count exceeds this threshold.
     */
    private int threshold;

    /**
     * The load factor for the hashtable.
     */
    private float loadFactor;

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity and the specified load factor.
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
    public IntKeyTable(int initialCapacity, boolean useEquals,
               float loadFactor) {
    if ((initialCapacity <= 0) || (loadFactor <= 0.0)) {
        throw new IllegalArgumentException();
    }
    this.useEquals = useEquals;
    this.loadFactor = loadFactor;
    table = new IntKeyTableEntry[initialCapacity];
    threshold = (int)(initialCapacity * loadFactor);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity and load factor.
     * @param initialCapacity the initial number of buckets
     */
    public IntKeyTable(int initialCapacity, float loadFactor) {
    this(initialCapacity, false, loadFactor);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity and comparison type.
     * @param initialCapacity the initial number of buckets
     * @param useEquals whether to use the equals() method or '=='
     */
    public IntKeyTable(int initialCapacity, boolean useEquals) {
    this(initialCapacity, useEquals, 0.75f);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity.
     * @param initialCapacity the initial number of buckets
     */
    public IntKeyTable(int initialCapacity) {
    this(initialCapacity, false, 0.75f);
    }

    /**
     * Constructs a new, empty hashtable. A default capacity and load factor
     * is used. Note that the hashtable will automatically grow when it gets
     * full.
     */
    public IntKeyTable() {
    this(101, false, 0);
    }

    /**
     * Returns the number of elements contained in the hashtable.
     */
    public int size() {
    return count;
    }

    /**
     * Returns true if the hashtable contains no elements.
     */
    public boolean isEmpty() {
    return count == 0;
    }

    /**
     * Returns an enumeration of the hashtable's keys.
     * @see Hashtable#elements
     * @see Enumeration
     */
    public synchronized Enumeration keys() {
    return new IntKeyTableEnumerator(table, true);
    }

    /**
     * Returns an enumeration of the elements. Use the Enumeration methods
     * on the returned object to fetch the elements sequentially.
     * @see Hashtable#keys
     * @see Enumeration
     */
    public synchronized Enumeration elements() {
    return new IntKeyTableEnumerator(table, false);
    }

    /**
     * Returns true if the specified object is an element of the hashtable.
     * This operation is more expensive than the containsKey() method.
     * @param value the value that we are looking for
     * @exception NullPointerException If the value being searched
     * for is equal to null.
     * @see Hashtable#containsKey
     */
    public synchronized boolean contains(Object value) {
    if (value == null) {
        throw new NullPointerException();
    }

    for (int i = table.length ; i-- > 0 ;) {
        for (IntKeyTableEntry e = table[i] ; e != null ; e = e.next) {
        if (useEquals) {
            if (e.value.equals(value)) {
            return true;
            }
        }
        else {
            if (e.value == value) {
            return true;
            }
        }
        }
    }
    return false;
    }


    public synchronized void replace(Object value, Object newValue) {
    if ((value == null) || (newValue == null)) {
        throw new NullPointerException();
    }

    for (int i = table.length ; i-- > 0 ;) {
        for (IntKeyTableEntry e = table[i] ; e != null ; e = e.next) {
        if (useEquals) {
            if (e.value.equals(value)) {
            e.value = newValue;
            return;
            }
        }
        else {
            if (e.value == value) {
            e.value = newValue;
            return;
            }
        }
        }
    }
    }




    /**
     * Returns true if the collection contains an element for the key.
     */
    public synchronized boolean containsKey(int key) {
    int index = (key & 0x7FFFFFFF) % table.length;
    for (IntKeyTableEntry e = table[index] ; e != null ; e = e.next) {
        if (e.key == key) {
        return true;
        }
    }
    return false;
    }

    /**
     * Gets the object associated with the specified key in the
     * hashtable.
     * @returns the element for the key or null if the key
     *      is not defined in the hash table.
     * @see Hashtable#put
     */
    public synchronized Object get(int key) {
    int index = (key & 0x7FFFFFFF) % table.length;
    for (IntKeyTableEntry e = table[index] ; e != null ; e = e.next) {
        if (e.key == key) {
        return e.value;
        }
    }
    return null;
    }

    /**
     * Rehashes the content of the table into a bigger table.
     * This method is called automatically when the hashtable's
     * size exceeds the threshold.
     */
    protected void rehash() {
    int oldCapacity = table.length;
    IntKeyTableEntry oldTable[] = table;

    int newCapacity = oldCapacity * 2 + 1;
    IntKeyTableEntry newTable[] = new IntKeyTableEntry[newCapacity];

    threshold = (int)(newCapacity * loadFactor);
    table = newTable;

//  System.out.println("rehash old=" + oldCapacity + ", new=" + newCapacity + ", thresh=" + threshold + ", count=" + count);

    for (int i = oldCapacity ; i-- > 0 ;) {
        for (IntKeyTableEntry old = oldTable[i] ; old != null ; ) {
        IntKeyTableEntry e = old;
        old = old.next;

        int index = (e.key & 0x7FFFFFFF) % newCapacity;
        e.next = newTable[index];
        newTable[index] = e;
        }
    }
    }

    /**
     * Puts the specified element into the hashtable, using the specified
     * key.  The element may be retrieved by doing a get() with the same key.
     * The key and the element cannot be null.
     * @param value the specified element
     * @exception NullPointerException If the value of the element
     * is equal to null.
     * @see Hashtable#get
     * @return the old value of the key, or null if it did not have one.
     */
    public synchronized Object put(int key, Object value) {
    // Make sure the value is not null
    if (value == null) {
        throw new NullPointerException();
    }

    // Makes sure the key is not already in the hashtable.
    int index = (key & 0x7FFFFFFF) % table.length;
    for (IntKeyTableEntry e = table[index] ; e != null ; e = e.next) {
        if (e.key == key) {
        Object old = e.value;
        e.value = value;
        return old;
        }
    }

    if (count >= threshold) {
        // Rehash the table if the threshold is exceeded
        rehash();
        return put(key, value);
    }

    // Creates the new entry.
    IntKeyTableEntry e = new IntKeyTableEntry();
    e.key = key;
    e.value = value;
    e.next = table[index];
    table[index] = e;
    count++;
    return null;
    }

    /**
     * Removes the element corresponding to the key. Does nothing if the
     * key is not present.
     * @return the value of key, or null if the key was not found.
     */
    public synchronized Object remove(int key) {
    int index = (key & 0x7FFFFFFF) % table.length;
    IntKeyTableEntry e, prev;
    for (e = table[index], prev = null ; e != null ; prev = e, e = e.next) {
        if (e.key == key) {
        if (prev != null) {
            prev.next = e.next;
        } else {
            table[index] = e.next;
        }
        count--;
        return e.value;
        }
    }
    return null;
    }

    /**
     * Clears the hash table so that it has no more elements in it.
     */
    public synchronized void clear() {
    for (int index = table.length; --index >= 0; )
        table[index] = null;
    count = 0;
    }

    /**
     * Creates a clone of the hashtable. A shallow copy is made,
     * the keys and elements themselves are NOT cloned. This is a
     * relatively expensive operation.
     */
    public synchronized Object clone() {
    try {
        IntKeyTable t = (IntKeyTable)super.clone();
        t.table = new IntKeyTableEntry[table.length];
        for (int i = table.length ; i-- > 0 ; ) {
        t.table[i] = (table[i] != null)
            ? (IntKeyTableEntry)table[i].clone() : null;
        }
        return t;
    } catch (CloneNotSupportedException e) {
        // this shouldn't happen, since we are Cloneable
        throw new InternalError();
    }
    }

    /**
     * Converts to a rather lengthy String.
     */
    public synchronized String toString() {
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
}

/**
 * A hashtable enumerator class.  This class should remain opaque
 * to the client. It will use the Enumeration interface.
 */
class IntKeyTableEnumerator implements Enumeration {
    boolean keys;
    int index;
    IntKeyTableEntry table[];
    IntKeyTableEntry entry;

    IntKeyTableEnumerator(IntKeyTableEntry table[], boolean keys) {
    this.table = table;
    this.keys = keys;
    this.index = table.length;
    }

    public boolean hasMoreElements() {
    if (entry != null) {
        return true;
    }
    while (index-- > 0) {
        if ((entry = table[index]) != null) {
        return true;
        }
    }
    return false;
    }

    public Object nextElement() {
    if (entry == null) {
        while ((index-- > 0) && ((entry = table[index]) == null));
    }
    if (entry != null) {
        IntKeyTableEntry e = entry;
        entry = e.next;
        return keys ? new Integer(e.key) : e.value;
    }
    throw new NoSuchElementException("IntKeyTableEnumerator");
    }
}
