// Hashtable.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

// Each entry in the table can be in one of three states:
//   1. Empty   -> hashCodes[index] == EMPTY
//   2. Removed -> hashCodes[index] == REMOVED
//   3. Filled  -> keys[index] != null

// You must call Hashtable.hash() to get the real hash code for this table.

// There needs to be a way to call Object.hashCode() directly when you want
// to do identity hashing.  ALERT!

/** Object subclass that implements a hash table.
  * @note 1.0 toString() prints as formatted text
  */
public class Hashtable implements Cloneable, Codable {
    /** For the multiplicative hash, choose the golden ratio:
      * <pre>
      *     A = ((sqrt(5) - 1) / 2) * (1 << 32)
      * </pre>
      * ala Knuth...
      */
    static final int A = 0x9e3779b9;

    /** We use EMPTY and REMOVED as special markers in the table. If some
      * poor object returns one of these two values as their hashCode, it
      * is wacked to DEFAULT.
      */
    static final int EMPTY = 0;
    static final int REMOVED = 1;
    static final int DEFAULT = 2;

    static final String keysField = "keys";
    static final String elementsField = "elements";

    int count;
    int totalCount;
    int shift;
    int capacity;
    int indexMask;

    int hashCodes[];
    Object keys[];
    Object elements[];

    /** Constructs an empty Hashtable. The Hashtable will grow on demand
      * as more elements are added.
      */
    public Hashtable() {
        super();
        shift = 32 - 3 + 1;
    }

    /** Constructs a Hashtable capable of holding at least
      * <b>initialCapacity</b> elements before needing to grow.
      */
    public Hashtable(int initialCapacity) {
        this();

        if (initialCapacity < 0)
            throw new IllegalArgumentException("initialCapacity must be > 0");

        grow(initialCapacity);
    }

    /** Creates a shallow copy of the Hashtable. The table itself is cloned,
      * but none of the keys or elements are copied.
      */
    public Object clone() {
        int len;
        Hashtable newTable;

        try {
            newTable = (Hashtable)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(
                "Error in clone(). This shouldn't happen.");
        }

        // If there is nothing in the table, then we cloned to make sure the
        // class was preserved, but just null everything out except for shift,
        // which implies the default initial capacity.

        if (count == 0) {
            newTable.shift = 32 - 3 + 1;
            newTable.totalCount = 0;
            newTable.capacity = 0;
            newTable.indexMask = 0;
            newTable.hashCodes = null;
            newTable.keys = null;
            newTable.elements = null;

            return newTable;
        }

        len = hashCodes.length;
        newTable.hashCodes = new int[len];
        newTable.keys = new Object[len];
        newTable.elements = new Object[len];

        System.arraycopy(hashCodes, 0, newTable.hashCodes, 0, len);
        System.arraycopy(keys, 0, newTable.keys, 0, len);
        System.arraycopy(elements, 0, newTable.elements, 0, len);

        return newTable;
    }

    /** Returns the number of elements in the Hashtable.
      */
    public int count() {
        return count;
    }

    /** Returns the number of elements in the Hashtable.
      */
    public int size() {
        return count;
    }

    /** Returns <b>true</b> if there are no elements in the Hashtable.
      */
    public boolean isEmpty() {
        return (count == 0);
    }

    /** Returns an Enumeration of the Hashtable's keys.
      * @see #elements
      */
    public Enumeration keys() {
        return new HashtableEnumerator(this, true);
    }

    /** Returns an Enumeration of the Hashtable's elements.
      * @see #keys
      */
    public Enumeration elements() {
        return new HashtableEnumerator(this, false);
    }

    /** Returns a Vector containing the Hashtable's keys.
      */
    public Vector keysVector() {
        int i, vectCount;
        Object key;
        Vector vect;

        if (count == 0)
            return new Vector();

        vect = new Vector(count);
        vectCount = 0;

        for (i = 0; i < keys.length && vectCount < count; i++) {
            key = keys[i];
            if (key != null) {
                vect.addElement(key);
                vectCount++;
            }
        }

        return vect;
    }

    /** Returns a Vector containing the Hashtable's elements.
      */
    public Vector elementsVector() {
        int i, vectCount;
        Object element;
        Vector vect;

        if (count == 0)
            return new Vector();

        vect = new Vector(count);
        vectCount = 0;

        for (i = 0; i < elements.length && vectCount < count; i++) {
            element = elements[i];
            if (element != null) {
                vect.addElement(element);
                vectCount++;
            }
        }

        return vect;
    }

    /** Returns an Object array containing the Hashtable's keys.
      */
    public Object[] keysArray() {
        int i, arrayCount;
        Object key, array[];

        if (count == 0)
            return null;

        array = new Object[count];
        arrayCount = 0;

        for (i = 0; i < keys.length && arrayCount < count; i++) {
            key = keys[i];
            if (key != null) {
                array[arrayCount++] = key;
            }
        }

        return array;
    }

    /** Returns an Object array containing the Hashtable's elements.
      */
    public Object[] elementsArray() {
        int i, arrayCount;
        Object element, array[];

        if (count == 0)
            return null;

        array = new Object[count];
        arrayCount = 0;

        for (i = 0; i < elements.length && arrayCount < count; i++) {
            element = elements[i];
            if (element != null) {
                array[arrayCount++] = element;
            }
        }

        return array;
    }

    /** Returns <b>true</b> if the Hashtable contains the element. This method
      * is slow -- O(n) -- because it must scan the table searching for the
      * element.
      */
    public boolean contains(Object element) {
        int i;
        Object tmp;

        // We need to short-circuit here since the data arrays may not have
        // been allocated yet.

        if (count == 0)
            return false;

        if (element == null)
            throw new NullPointerException();

        if (elements == null)
            return false;

        for (i = 0; i < elements.length; i++) {
            tmp = elements[i];
            if (tmp != null && element.equals(tmp))
                return true;
        }

        return false;
    }

    /** Returns <b>true</b> if the Hashtable contains the key <b>key</b>.
      */
    public boolean containsKey(Object key) {
        return (get(key) != null);
    }

    /** Returns the element associated with the <b>key</b>. This method returns
      * <b>null</b> if the Hashtable does not contain <b>key</b>.  Hashtable
      * hashes and compares <b>key</b> using <b>hashCode()</b> and
      * <b>equals()</b>.
      */
    public Object get(Object key) {
        // We need to short-circuit here since the data arrays may not have
        // been allocated yet.

        if (count == 0)
            return null;

        return elements[tableIndexFor(key, hash(key))];
    }

    /** Removes <b>key</b> and the element associated with it from the
      * Hashtable. Returns the element associated with <b>key</b>, or
      * <b>null</b> if <b>key</b> was not present.
      */
    public Object remove(Object key) {
        int index;
        Object oldValue;

        // We need to short-circuit here since the data arrays may not have
        // been allocated yet.

        if (count == 0)
            return null;

        index = tableIndexFor(key, hash(key));
        oldValue = elements[index];
        if (oldValue == null)
            return null;

        count--;
        hashCodes[index] = REMOVED;
        keys[index] = null;
        elements[index] = null;

        return oldValue;
    }

    /** Places the <b>key</b>/<b>element</b> pair in the Hashtable. Neither
      * <b>key</b> nor <b>element</b> may be <b>null</b>. Returns the old
      * element associated with <b>key</b>, or <b>null</b> if the <b>key</b>
      * was not present.
      */
    public Object put(Object key, Object element) {
        int index, hash;
        Object oldValue;

        if (element == null)
            throw new NullPointerException();

        // Since we delay allocating the data arrays until we actually need
        // them, check to make sure they exist before attempting to put
        // something in them.

        if (hashCodes == null)
            grow();

        hash = hash(key);
        index = tableIndexFor(key, hash);
        oldValue = elements[index];

        // If the total number of occupied slots (either with a real element or
        // a removed marker) gets too big, grow the table.

        if (oldValue == null) {
            if (hashCodes[index] == EMPTY) {
                if (totalCount >= capacity) {
                    grow();
                    return put(key, element);
                }
                totalCount++;
            }
            count++;
        }

        hashCodes[index] = hash;
        keys[index] = key;
        elements[index] = element;

        return oldValue;
    }

    /** We preclude the hashCodes EMPTY and REMOVED because we use them to
      * indicate empty and previously filled slots in the table. All the
      * Hashtable code should go through here and not call hashCode()
      * directly on the key.
      */
    private int hash(Object key) {
        int hash;

        // On sparc it appears that the last 3 bits of Object.hashCode() are
        // insignificant!  ALERT!

        hash = key.hashCode();
        if (hash == EMPTY || hash == REMOVED)
            hash = DEFAULT;

        return hash;
    }

    /** Primitive method used internally to find slots in the
      * table. If the key is present in the table, this method will return the
      * index
      * under which it is stored. If the key is not present, then this
      * method will return the index under which it can be put. The caller
      * must look at the hashCode at that index to differentiate between
      * the two possibilities.
      */
    private int tableIndexFor(Object key, int hash) {
        int product, testHash, index, step, removedIndex, probeCount;

        product = hash * A;
        index = product >>> shift;

        // Probe the first slot in the table.  We keep track of the first
        // index where we found a REMOVED marker so we can return that index
        // as the first available slot if the key is not already in the table.

        testHash = hashCodes[index];

        if (testHash == hash) {
            if (key.equals(keys[index]))
                return index;
            removedIndex = -1;
        } else if (testHash == EMPTY)
            return index;
        else if (testHash == REMOVED)
            removedIndex = index;
        else
            removedIndex = -1;

        // Our first probe has failed, so now we need to start looking
        // elsewhere in the table.

        step = ((product >>> (2 * shift - 32)) & indexMask) | 1;
        probeCount = 1;

        do {
            probeCount++;
            index = (index + step) & indexMask;

            testHash = hashCodes[index];

            if (testHash == hash) {
                if (key.equals(keys[index]))
                    return index;
            } else if (testHash == EMPTY) {
                if (removedIndex < 0)
                    return index;
                else
                    return removedIndex;
            } else if (testHash == REMOVED && removedIndex == -1)
                removedIndex = index;

        } while (probeCount <= totalCount);

        // Something very bad has happened.

        throw new InconsistencyException("Hashtable overflow");
    }

    /** Grows the table to accommodate at least capacity number of elements.
      */
    private void grow(int capacity) {
        int tableSize, power;

        // Find the lowest power of 2 size for the table which will allow
        // us to insert initialCapacity number of objects before having to
        // grow.

        tableSize = (capacity * 4) / 3;

        power = 3;
        while ((1 << power) < tableSize)
            power++;

        // Once shift is set, then grow() will do the right thing when
        // called.

        shift = 32 - power + 1;
        grow();
    }

    /** Grows the table by a factor of 2 (or creates it if necessary). All
      * the REMOVED markers go away and the elements are rehashed into the
      * bigger table.
      */
    private void grow() {
        int i, index, hash, power, oldHashCodes[];
        Object key, oldKeys[], oldValues[];

        // The table size needs to be a power of two, and it should double
        // when it grows.  We grow when we are more than 3/4 full.

        shift--;
        power = 32 - shift;
        indexMask = (1 << power) - 1;
        capacity = (3 * (1 << power)) / 4;

        oldHashCodes = hashCodes;
        oldKeys = keys;
        oldValues = elements;

        hashCodes = new int[1 << power];
        keys = new Object[1 << power];
        elements = new Object[1 << power];

        // Reinsert the old elements into the new table if there are any.  Be
        // sure to reset the counts and increment them as the old entries are
        // put back in the table.

        totalCount = 0;

        if (count > 0) {
            count = 0;

            for (i = 0; i < oldHashCodes.length; i++) {
                key = oldKeys[i];

                if (key != null) {
                    hash = oldHashCodes[i];
                    index = tableIndexFor(key, hash);

                    hashCodes[index] = hash;
                    keys[index] = key;
                    elements[index] = oldValues[i];

                    count++;
                    totalCount++;
                }
            }
        }
    }

    /** Removes all keys and elements from the Hashtable.
      */
    public void clear() {
        int i;

        if (hashCodes == null)
            return;

        for (i = 0; i < hashCodes.length; i++) {
            hashCodes[i] = EMPTY;
            keys[i] = null;
            elements[i] = null;
        }

        count = 0;
        totalCount = 0;
    }

    /** Returns a string serialization of the Hashtable using the
      * Serializer.
      * @see Serializer
      */
    public String toString() {
        return FormattingSerializer.serializeObject(this);
    }

    /** Describes the Hashtable class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.util.Hashtable", 1);
        info.addField(keysField, OBJECT_ARRAY_TYPE);
        info.addField(elementsField, OBJECT_ARRAY_TYPE);
    }

    /** Encodes the Hashtable instance. All the keys and elements must be
      * Codable.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        Object keysArray[], elementsArray[];

        if (count == 0)
            return;

        keysArray = keysArray();
        elementsArray = elementsArray();

        encoder.encodeObjectArray(keysField, keysArray, 0, keysArray.length);
        encoder.encodeObjectArray(elementsField, elementsArray, 0,
            elementsArray.length);
    }

    /** Decodes the Hashtable instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int i;
        Object keysArray[], elementsArray[];

        keysArray = decoder.decodeObjectArray(keysField);
        elementsArray = decoder.decodeObjectArray(elementsField);

        if (keysArray == null || keysArray.length == 0)
            return;

        grow(keysArray.length);

        for (i = 0; i < keysArray.length; i++)
            put(keysArray[i], elementsArray[i]);
    }

    /** Finishes the Hashtable's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
