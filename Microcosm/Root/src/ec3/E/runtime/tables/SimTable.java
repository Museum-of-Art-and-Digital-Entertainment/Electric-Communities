package ec.tables;

import java.util.Dictionary;
import java.util.Enumeration;


/**
 * Implements the interface contract of java.util.Hashtable by
 * wrapping an ec.tables.Table.  In addition, the clone() operation is
 * typically more efficient by being copy-on-write, and the
 * Enumerations returned enumerate a snapshot of the table as of the
 * time the Enumeration was requested, whether or not the table has
 * changed since.
 */
public class SimTable extends Dictionary implements Cloneable {

    static private final Class OBJECT_TYPE = new Object().getClass();

    /**
     * Not private only to support clone() by subclasses
     */
    protected Table myTable;

    /**
     * Not private only to support clone() by subclasses
     */
    protected SimTable(Table table) {
        myTable = table;
    }

    /**
     * Constructs a new, empty SimTable with the specified initial
     * capacity and the specified load factor.
     * @param initialCapacity the initial number of buckets
     * @param loadFactor a number between 0.0 and 1.0, it defines
     *      the threshold for rehashing the SimTable into
     *      a bigger one.
     * @exception IllegalArgumentException If the initial capacity
     * is less than or equal to zero.
     * @exception IllegalArgumentException If the load factor is
     * less than or equal to zero or greater that 1.
     */
    public SimTable(boolean isIdentity,
                    int initialCapacity,
                    float loadFactor) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException
              ("bad initialCapacity " + initialCapacity);
        }
        if (loadFactor <= 0.0 || loadFactor > 1) {
            throw new IllegalArgumentException
              ("bad loadFactor " + loadFactor);
        }
        myTable = new Table(false,
                            OBJECT_TYPE,
                            OBJECT_TYPE,
                            initialCapacity,
                            loadFactor);
    }


    /**
     *
     */
    public SimTable(int initialCapacity, float loadFactor) {
        this(false, initialCapacity, loadFactor);
    }


    /**
     * Constructs a new, empty SimTable with the specified initial
     * capacity.
     * @param initialCapacity the initial number of buckets
     */
    public SimTable(int initialCapacity) {
        this(false, initialCapacity, Table.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty SimTable. A default capacity and load factor
     * is used. Note that the SimTable will automatically grow when it gets
     * full.
     */
    public SimTable() {
        this(false, 101, Table.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Returns the number of elements contained in the SimTable.
     */
    public int size() {
        return myTable.size();
    }

    /**
     * Returns true if the SimTable contains no elements.
     */
    public boolean isEmpty() {
        return myTable.isEmpty();
    }

    /**
     * Returns an enumeration of the SimTable's keys.
     * @see SimTable#elements
     * @see Enumeration
     */
    public Enumeration keys() {
        return myTable.keys();
    }

    /**
     * Returns an enumeration of the elements. Use the Enumeration methods
     * on the returned object to fetch the elements sequentially.
     * @see SimTable#keys
     * @see Enumeration
     */
    public Enumeration elements() {
        return myTable.elements();
    }

    /**
     * Returns true if the specified object is an element of the SimTable.
     * This operation is more expensive than the containsKey() method.
     * @param value the value that we are looking for
     * @exception NullPointerException If the value being searched
     * for is equal to null.
     * @see SimTable#containsKey
     */
    public boolean contains(Object value) {
        nonNull(value);
        ColumnEnumeration iter = (ColumnEnumeration)elements();
        while (iter.hasMoreElements()) {
            Object element = iter.nextElement();
            if (element.equals(value)) {
                iter.skipRest();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the collection contains an element for the key.
     * @param key the key that we are looking for
     * @see SimTable#contains
     */
    public boolean containsKey(Object key) {
        nonNull(key);
        return myTable.containsKey(key);
    }

    /**
     * Gets the object associated with the specified key in the
     * SimTable.
     * @param key the specified key
     * @returns the element for the key or null if the key
     *      is not defined in the hash table.
     * @see SimTable#put
     */
    public Object get(Object key) {
        nonNull(key);
        return myTable.get(key, null);
    }

    /**
     * Puts the specified element into the SimTable, using the specified
     * key.  The element may be retrieved by doing a get() with the same key.
     * The key and the element cannot be null.
     * @param key the specified key in the SimTable
     * @param value the specified element
     * @exception NullPointerException If the value of the element
     * is equal to null.
     * @see SimTable#get
     * @return the old value of the key, or null if it did not have one.
     */
    public Object put(Object key, Object value) {
        nonNull(value);
        Object result = get(key);
        myTable.put(key, value);
        return result;
    }

    /**
     * Removes the element corresponding to the key. Does nothing if the
     * key is not present.
     * @param key the key that needs to be removed
     * @return the value of key, or null if the key was not found.
     */
    public Object remove(Object key) {
        Object result = get(key);
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
     * Creates a clone of the SimTable. A shallow copy is made,
     * the keys and elements themselves are NOT cloned. Unlike
     * java.util.Hashtable, this is not necessarily a relatively
     * expensive operation.
     */
    public Object clone() {
        return new SimTable((Table)myTable.clone());
    }

    /**
     * Converts to a rather lengthy String.
     */
    public String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        //XXX parallel enumerations bad!  Unwarranted assumption that
        //they will enumerate in synch
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
            throw new NullPointerException("nulls not allowed in SimTable");
        }
    }
}
