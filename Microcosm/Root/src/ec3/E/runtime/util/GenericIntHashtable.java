package ec.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This is a better Hashtable than the normal java.util one,
 * additionally modified for int values.
 * It is abstract so as to be able to have its particular hashing
 * and equality checks defined by subclasses. The two provided concrete
 * versions are IdentityIntHashTable--based
 * on == and System.identityHashCode()--and EqualsIntHashTable--based on
 * Object.equals() and Object.hashCode().
 *
 * @see ec.util.EqualsIntHashtable
 * @see ec.util.IdentityIntHashtable
 * @author Dan Bornstein, danfuzz@communities.com
 */
abstract public class GenericIntHashtable
{
    /**
     * The array of values in the table.
     */
    private int values[];

    /**
     * The special value that means null entry
     */
    private int noValue;

    /**
     * The array of keys in the table. Many entries may be null.
     */
    private Object keys[];

    /**
     * The array of computed hashes for each key in the set.
     */
    private int hashes[];

    /**
     * The size of the underlying arrays.
     */
    private int maxSize;

    /**
     * The number of elements in the table.
     */
    private int curSize;

    /**
     * The earliest spot where an element is stored (optimization for
     * making removeOne() work well).
     */
    private int earliestStored;

    /**
     * The current size threshold for the table, that is, the number
     * of elements to hold before growing. It is calculated as
     * maxSize * loadFactor.
     */
    private int sizeThreshold;

    /**
     * The load factor for the table.
     */
    private double loadFactor;

    /**
     * The maximum number of probes to make.
     */
    private int maxProbes;

    /**
     * Construct a new, empty Hashtable, with the specified parameters.
     *
     * @param initialCapacity the initial number of elements the
     * table should be prepared to hold
     * @param loadFactor a number between 0.0 (exclusive) and 1.0, defining
     * the threshold for growing the table's underlying arrays.
     * @param maxProbes the maximum number of probes into the
     * table's underlying arrays to make. Larger numbers mean
     * slower performance but better memory usage.
     *
     * @exception IllegalArgumentException thrown if initialCapacity <= 0
     * @exception IllegalArgumentException thrown if loadFactor <= 0 or
     * loadFactor > 1
     * @exception IllegalArgumentException thrown if maxProbes <= 0
     */
    public GenericIntHashtable(int initialCapacity, double loadFactor,
            int maxProbes, int noValue) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException(
                "Bad value for initialCapacity");
        }
        if (   (loadFactor <= 0.0)
            || (loadFactor > 1.0)) {
            throw new IllegalArgumentException(
                "Bad value for loadFactor");
        }
        if (maxProbes <= 0) {
            throw new IllegalArgumentException(
                "Bad value for maxProbes");
        }

        this.loadFactor = loadFactor;
        this.maxProbes = maxProbes;
        this.noValue = noValue;
        maxSize = (int) (initialCapacity / loadFactor);
        initializeTable();
    }

    /**
     * Constructs a new, empty Hashtable, with a reasonable set of defaults.
     */
    public GenericIntHashtable() {
        this (50, 0.75, 16, 0);
    }

    /**
     * Get the number of elements in the table.
     *
     * @return the number of elements in the table.
     */
    public int size() {
        return curSize;
    }

    /**
     * Return true if the table contains no elements.
     *
     * @return true if the table contains no elements.
     */
    public boolean isEmpty() {
        return curSize == 0;
    }

    /**
     * Return an enumeration of the values in the table.
     *
     * @return the enumeration
     */
    public Enumeration elements() {
        return new IntHashtableEnumerator(values, noValue);
    }

    /**
     * Return an enumeration of the keys in the set.
     *
     * @return the enumeration
     */
    public Enumeration keys() {
        return new HashtableEnumerator(keys);
    }

    /**
     * Returns true if the specified object is a key in the
     * table, as defined by the equality function of the table.
     *
     * @param value the object to look for
     * @exception NullPointerException thrown if key == null
     * @return true if the key is in the table
     */
    public boolean containsKey(Object key) {
        return get(key) != noValue;
    }

    /**
     * Gets the object in the table for the key that is equal to the given
     * one, as defined by the equality function of the table, or
     * null if there is no such object.
     *
     * @param key the object to look for
     * @exception NullPointerException thrown if value == null
     * @return nullOk; the value in the set, or null if there is
     * no such object
     */
    public int get(Object key) {
        if (key == null) {
            throw new NullPointerException("key may not be null");
        }

        int hash = performHashCode(key);
        int slot = findSlot(key, hash, false);

        if (slot == -1) {
            return noValue;
        }

        return values[slot];
    }

    /**
     * Put the given key/value pair into the table. If the key is equivalent
     * (according to the equal function) to a key already in
     * the table, the key/value pair is replaced.
     *
     * @param key the key to place in the table
     * @param value the value to place in the table
     * @exception NullPointerException thrown if key == null
     * @exception NullPointerException thrown if value == null
     * @return nullOk; the old value in the table, or null if
     * there was no such object
     */
    public int put(Object key, int value) {
        if (value == noValue) {
            throw new NullPointerException("value may not be noValue, " + value);
        }
        if (key == null) {
            throw new NullPointerException("key may not be null");
        }

        int hash = performHashCode(key);
        int slot = findSlot(key, hash, true);
        int oldValue = values[slot];
        if (oldValue == noValue) {
            curSize++;
        }
        hashes[slot] = hash;
        keys[slot] = key;
        values[slot] = value;
        if (slot < earliestStored) {
            earliestStored = slot;
        }
        return oldValue;
    }

    /**
     * Removes the given key (or its equivalent, according to the
     * equal function) from the table. This does nothing if the
     * object is not actually a key in the table.
     *
     * @param key the key to remove
     * @exception NullPointerException thrown if key == null
     * @return the old value in the table or noValue if there was
     * no such key
     */
    public int remove(Object key) {
        if (key == null) {
            throw new NullPointerException("key may not be null");
        }
        
        int hash = performHashCode(key);
        int slot = findSlot(key, hash, false);

        if (slot == -1) {
            return noValue;
        }

        int oldValue = values[slot];
        curSize--;
        values[slot] = noValue;
        keys[slot] = null;
        hashes[slot] = 0;
        if (slot == earliestStored) {
            earliestStored = slot + 1;
        }
        return oldValue;
    }

    /**
     * This removes and returns one element from the table. If there
     * are no elements in the table, this returns null.
     *
     * @return a value removed from the table or noValue if the
     * table is empty
     */
    public int removeOne() {
        if (curSize == 0) {
            return noValue;
        }

        for (int i = earliestStored; i < maxSize; i++) {
            if (values[i] != noValue) {
                int result = values[i];
                keys[i] = null;
                values[i] = noValue;
                hashes[i] = 0;
                curSize--;
                earliestStored = i + 1;
                return result;
            }
        }

        return noValue; // to shut up the compiler
    }

    /**
     * This clears out the table so that it has no elements in it.
     */
    public void clear() {
        initializeTable();
    }

    /**
     * This method must be overridden by subclasses to define the
     * way to compare two keys for equality.
     *
     * @param o1 an object to compare
     * @param o2 the object to compare o1 to
     * @return true if the two objects should be considered equal
     * in the eyes of the set
     */
    abstract public boolean performEquals(Object o1, Object o2);

    /**
     * This method must be overridden by subclasses to define the
     * way to get a hashCode for a key.
     *
     * @param key the (possibly potential) key whose hashCode is desired
     * @return the hashCode of the key
     */
    abstract public int performHashCode(Object key);

    /**
     * This private routine finds the proper slot in the
     * underlying array for the given object to reside in.
     * If the object (or an equivalent) is already in the table,
     * then the slot containing that object is returned. If
     * the object is not already there and willPut is true, then
     * the table is grown so as to be able to contain the object.
     *
     * @param key the key to look for
     * @param hash the hash of the key
     * @param willPut true if the value will be put in the table
     * @return the slot number for the value, or -1 if the value
     * was not found and willPut == false
     */
    private int findSlot(Object key, int hash, boolean willPut) {
        // we'll just keep looping until the table is grown
        // enough, if necessary. If willPut is false, then
        // we'll always end up returning in the middle of
        // the first iteration through this loop.
        for (;;) {
            int curProbe = hash % maxSize;
            int probeSkip = (hash & 0xf) + 1;
            int nullAt = -1;
            if (curProbe < 0) {
                curProbe = -curProbe;
            }
            
            // search the array for the element (or equivalent)
            for (int i = maxProbes; i > 0; i--) {
                Object curKey = keys[curProbe];
                if (   (curKey != null)
                    && (hashes[curProbe] == hash)
                    && (performEquals (curKey, key))) {
                    // we found it
                    return curProbe;
                }
                else if (   (curKey == null)
                         && (nullAt == -1)) {
                    // we found the first null; it'll be used if
                    // the element isn't eventually found and
                    // willPut == true
                    nullAt = curProbe;
                }
                
                curProbe += probeSkip;
                if (curProbe >= maxSize) {
                    // note, since the minimum possible maxSize is
                    // 17 (see initializeTable(), below), we don't
                    // have to worry about curProbe >= (maxSize * 2)
                    curProbe -= maxSize;
                }
            }
            
            // we fell through, which means there was no
            // such element. Merely return -1 if we don't have
            // to grow the table for a put operation
            if (! willPut) {
                return -1;
            }

            // if we found a null, return it, unless we're at the
            // size limit (derived from loadFactor)
            if (   (nullAt != -1)
                && (curSize < sizeThreshold)) {
                return nullAt;
            }

            // grow the table. we do the easy thing of just making
            // new keys[], values[], and hashes[] arrays and then using
            // the public put() operation to populate it based on
            // the old table
            Object oldKeys[] = keys;
            int oldValues[] = values;
            int oldMax = maxSize;

            // grow by a 3/2 ratio
            maxSize = (maxSize * 3) / 2;

            initializeTable();
            for (int i = 0; i < oldMax; i++) {
                Object k = oldKeys[i];
                if (k != null) {
                    put(k, oldValues[i]);
                }
            }
        }
    }

    /**
     * Little sorted array of primes for use by initializeTable().
     * The elements grow exponentially at somewhat less than 2x.
     * Also, note that the last number isn't prime; rather it's MAXINT.
     */
    final private int possibleSizes[] = 
    { 17, 23, 37, 53, 79, 109, 151, 211, 293, 421, 593, 829, 1171, 1637, 2293,
      3209, 4493, 6299, 8819, 12347, 17257, 24197, 33871, 47431, 66403,
      92959, 130147, 182209, 255107, 357139, 500009, 700027, 980047, 1372051,
      1920901, 2689261, 3764953, 5270939, 7379327, 10331063, 14463487,
      20248897, 28348447, 39687871, 55563023, 77788201, 108903523, 
      152464943, 213450911, 298831279, 418363789, 585709217, 819993047, 
      1147990271, 1607186393, 0x7fffffff 
    };

    /**
     * Private method to initialize the values[] and hashes[]
     * arrays as well as maxSize and sizeThreshold. In particular,
     * it adjusts maxSize to an appropriate value,
     * making it a prime number so that we get better distribution
     * of elements through the table.
     */
    void initializeTable() {
        // adjust maxSize
        for (int i = 0; i < possibleSizes.length; i++) {
            if (maxSize <= possibleSizes[i]) {
                maxSize = possibleSizes[i];
                break;
            }
        }

        if (maxSize == 0x7fffffff) {
            // just in case we ever really make a table that can't
            // ever grow, we have to make sure both maxProbes and
            // loadFactor reflect that fact
            maxProbes = maxSize;
            loadFactor = 1.0;
        }

        sizeThreshold = (int) (maxSize * loadFactor);
        keys = new Object[maxSize];
        values = new int[maxSize];
        hashes = new int[maxSize];
        curSize = 0;
        earliestStored = maxSize;
    }
}



/**
 * This is the Enumerator for IntHashtables.
 */
class IntHashtableEnumerator
implements Enumeration
{
    private int elements[];
    private int maxSize;
    private int cursor;
    private int myNoValue;

    /**
     * Construct a IntHashtableEnumerator, given the array of elements.
     * BUG--this should know about curSize and earliestStored
     *
     * @param theElements the array of elements
     */
    public IntHashtableEnumerator(int theElements[], int noValue) {
        elements = theElements;
        myNoValue = noValue;
        maxSize = theElements.length;
        cursor = -1;
        advance();
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public boolean hasMoreElements() {
        return cursor < maxSize;
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public Object nextElement() {
        if (cursor >= maxSize) {
            throw new NoSuchElementException("SetEnumerator");
        }

        int result = elements[cursor];
        advance();
        return new Integer(result);
    }

    /**
     * Advance the cursor past any nulls to the next non-null
     * element, or stop just past the end of the elements array
     * if there is no next non-null element to point at.
     */
    private void advance() {
        do {
            cursor++;
        } while ((cursor < maxSize) && (elements[cursor] == myNoValue));
    }
}



/**
 * This is the Enumerator for Hashtable keys.
 */
class HashtableEnumerator
implements Enumeration
{
    private Object elements[];
    private int maxSize;
    private int cursor;

    /**
     * Construct a HashtableEnumerator, given the array of elements.
     * BUG--this should know about curSize and earliestStored
     *
     * @param theElements the array of elements
     */
    public HashtableEnumerator(Object theElements[]) {
        elements = theElements;
        maxSize = theElements.length;
        cursor = -1;
        advance();
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public boolean hasMoreElements() {
        return cursor < maxSize;
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public Object nextElement() {
        if (cursor >= maxSize) {
            throw new NoSuchElementException("SetEnumerator");
        }

        Object result = elements[cursor];
        advance();
        return result;
    }

    /**
     * Advance the cursor past any nulls to the next non-null
     * element, or stop just past the end of the elements array
     * if there is no next non-null element to point at.
     */
    private void advance() {
        do {
            cursor++;
        } while ((cursor < maxSize) && (elements[cursor] == null));
    }
}












