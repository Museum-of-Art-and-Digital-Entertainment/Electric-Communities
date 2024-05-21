package ec.tables;

import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.lang.reflect.Array;


/**
 * Like a java.util.Hashtable, except that it admits nulls for both
 * keys and values, and so must indicate absence differently.  If you
 * want strict compatibility, use SimTable, which wraps this
 * Table in a compatible simulation of java.util.Hashtable.
 */
public class Table {

    static public final int STRONG = 0;
    static public final int KEYS_WEAK = 01;
    static public final int VALUES_WEAK = 02;
    static public final int BOTH_WEAK = KEYS_WEAK | VALUES_WEAK;

    static public final float DEFAULT_LOAD_FACTOR = 0.75f;
    static public final int DEFAULT_INIT_CAPACITY = 101;
    static public final int DEFAULT_NUM_SLOTS
        = (int)(DEFAULT_INIT_CAPACITY / DEFAULT_LOAD_FACTOR);
    static private final Integer DEFAULT_BOXED_INITCAP
        = new Integer(DEFAULT_INIT_CAPACITY);

    static public final Class OBJECT_TYPE = new Object().getClass();

    /*package*/ KeyColumn myKeys;
    /*package*/ Column myValues;

    /*package*/ int myWeakness;
    /*package*/ NextOfKin myNextOfKin;

    /**
     * The current size threshold for the table, that is, the number
     * of elements to hold before growing. It is calculated as
     * myNumSlots * myLoadFactor.
     */
    /*package*/ int mySizeThreshold;

    /**
     * The load factor for the table.
     */
    /*package*/ float myLoadFactor;

    /*package*/ ShareCount myShareCount;

    /*package*/ Table(KeyColumn keys,
                      Column values,
                      NextOfKin nextOfKin,
                      float loadFactor) {
        this(keys,
             values,
             nextOfKin,
             loadFactor,
             new ShareCount());
    }

    /*package*/ Table(KeyColumn keys,
                      Column values,
                      NextOfKin nextOfKin,
                      float loadFactor,
                      ShareCount shareCount) {
        if (keys.numSlots() != values.numSlots()) {
            throw new IllegalArgumentException
              ("columns must be same size");
        }
        myKeys = keys;
        myValues = values;

        myWeakness = STRONG;
        if (keys.isWeak()) {
            myWeakness = KEYS_WEAK;
        }
        if (values.isWeak()) {
            myWeakness |= VALUES_WEAK;
        }
        if (myWeakness == STRONG) {
            myNextOfKin = null;
        } else if (myWeakness == BOTH_WEAK
                   && nextOfKin != null) {
            throw new IllegalArgumentException
              ("can't finalize if both columns are weak");
        } else {
            myNextOfKin = nextOfKin;
        }

        if (loadFactor <= 0.0 || 1.0 < loadFactor) {
            throw new IllegalArgumentException
              ("Bad value for loadFactor" + loadFactor);
        }
        myLoadFactor = loadFactor;
        mySizeThreshold = (int)(myKeys.numSlots() * myLoadFactor);
        myShareCount = shareCount;

        if (myKeys.numTaken() >= mySizeThreshold) {
            rehash();
        }
    }


    /**
     * Reasonable defaults
     */
    /*package*/ Table(KeyColumn keys,
                      Column values) {
        this(keys, values, null, DEFAULT_LOAD_FACTOR);
    }


    /**
     *
     */
    public Table(boolean isIdentity,
                 Class keyType,
                 Class valueType,
                 int initCapacity,
                 float loadFactor) {

        myLoadFactor = loadFactor;
        int numSlots = 1 + (int)(initCapacity / loadFactor);
        if (isIdentity) {
            myKeys = new IdentityKeyColumn(keyType, numSlots);
        } else {
            myKeys = new EqualityKeyColumn(keyType, numSlots);
        }
        myValues = Column.values(valueType, myKeys.numSlots());

        myWeakness = STRONG;
        myNextOfKin = null;

        mySizeThreshold = (int)(myKeys.numSlots() * myLoadFactor);
        myShareCount = new ShareCount();
        if (myKeys.numTaken() >= mySizeThreshold) {
            rehash();
        }
    }


    /**
     * Reasonable defaults
     */
    public Table() {
        this(false,
             OBJECT_TYPE,
             OBJECT_TYPE,
             DEFAULT_INIT_CAPACITY,
             DEFAULT_LOAD_FACTOR);
    }


    /**
     * Reasonable defaults
     */
    public Table(Class keyType, Class valueType) {
        this(false,
             keyType,
             valueType,
             DEFAULT_INIT_CAPACITY,
             DEFAULT_LOAD_FACTOR);
    }


    /**
     * Reasonable defaults with identity-based (as opposed to
     * equality-based) key lookup
     */
    public Table(boolean isIdentity, Class keyType, Class valueType) {
        this(isIdentity,
             keyType,
             valueType,
             DEFAULT_INIT_CAPACITY,
             DEFAULT_LOAD_FACTOR);
    }


    /**
     *
     */
    public Table(ArgsHolder prefaceArgs) {
        this(((Boolean)prefaceArgs.get("isIdentity",
                                       Boolean.FALSE)).booleanValue(),
             (Class)prefaceArgs.get("keyType",   OBJECT_TYPE),
             (Class)prefaceArgs.get("valueType", OBJECT_TYPE),
             ((Integer)prefaceArgs.get("size",
                                       DEFAULT_BOXED_INITCAP)).intValue(),
             DEFAULT_LOAD_FACTOR);
    }


    /**
     * The number of keys in the collection
     */
    public int size() {
        return myKeys.numTaken();
    }


    /**
     * Does the collection contain any keys?
     */
    public boolean isEmpty() {
        return myKeys.numTaken() <= 0;
    }


    /**
     * Return an enumeration of the keys in the collection.  May include a
     * null.
     *
     * @return the enumeration
     */
    public Enumeration keys() {
        Table snapshot = (Table)clone();
        return new ColumnEnumeration(snapshot, snapshot.myKeys);
    }


    /**
     * Return an enumeration of the values in the table.  May include
     * nulls.
     *
     * @return the enumeration
     */
    public Enumeration elements() {
        Table snapshot = (Table)clone();
        return new ColumnEnumeration(snapshot, snapshot.myValues);
    }


    /**
     * Returns true if the specified object is a key in the
     * collection, as defined by the equality function of the collection.
     *
     * @param key the object to look for
     * @return true if the key is in the collection
     */
    public boolean containsKey(Object key) {
        return myKeys.findSlotOf(key) != -1;
    }


    /**
     * Returns the value to which the key is mapped in this table.
     * Unlike java.util.Dictionary, Table doesn't indicate a lookup
     * failure by returning null, since null is a valid value.
     * Table throws an exception instead.
     */
    public Object get(Object key) throws NoSuchElementException {
        int slot = myKeys.findSlotOf(key);
        if (slot == -1) {
            throw new NoSuchElementException("key not found");
        }
        return myValues.get(slot);
    }


    /**
     * Returns the value to which the key is mapped in this table.
     * If key is not mapped, return 'instead' instead.
     */
    public Object get(Object key, Object instead) {
        int slot = myKeys.findSlotOf(key);
        if (slot == -1) {
            return instead;
        }
        return myValues.get(slot);
    }


    /**
     * Like 'put' of three arguments, but defaults 'strict' to false.
     */
    public void put(Object key, Object value) {
        put(key, value, false);
    }


    /**
     * Causes 'key' to map to 'value'.  If 'strict' is false (the
     * default), this will overwrite a previous value if necessary.
     * If 'strict' is true, this only succeeds if there is not already
     * an association for 'key' in the Table.  If 'strict' is true and
     * there is an already an association, even to the same value,
     * this throws an Exception instead (XXX currently an
     * IllegalArgumentException) and leaves the Table unmodified. <p>
     *
     * Unlike Dictionary, this doesn't return the old value.  If you
     * want it, use 'get' first.
     */
    public void put(Object key, Object value, boolean strict) {
        writeFault();

        //XXX this should instead be done with one lookup
        if (strict && containsKey(key)) {
            throw new IllegalArgumentException(key + " already in Table");
        }

        if ((myKeys.numTaken() + 1) >= mySizeThreshold) {
            //just in case the key is novel
            rehash();
        }
        while (true) {
            int slot = myKeys.store(key);
            if (slot != -1) {
                myValues.put(slot, value);
                return;
            }
            rehash();
        }
    }


    /**
     * A convenience, and a bit of a hack, for initializing Tables
     * from a kind of data that can easily be expressed literally and
     * composably in Java.  Let's call this kind of data a LitTree.  A
     * LitTree is a tree whose internal nodes are arrays, and whose
     * leaves are non-arrays.  The members of a LitTree are only the
     * non-array leaves.  <p>
     *
     * (The hacky part is that a LitTree cannot represent an array
     * object as a member.  It's like defining a string format that
     * can't quote quote.  Oh well, it's only a convenience.) <p>
     *
     * Both 'keys' and 'elements' are LitTrees.  When their structure
     * is parallel, corresponding leaf key-element members are put
     * into this Table.  The other allowed case is for a 'keys'
     * subtree to match a single 'elements' leaf member, in which case
     * an association is put into the Table from each of the leaf keys
     * in that subtree to that same element.  No other cases are
     * allowed.
     */
    public void putAll(Object keys, Object elements, boolean strict) {
        if (keys != null && keys.getClass().isArray()) {
            int len = Array.getLength(keys);
            if (elements != null && elements.getClass().isArray()) {
                int len2 = Array.getLength(elements);
                if (len != len2) {
                    throw new IllegalArgumentException(len + " != " + len2);
                }
                for (int i = 0; i < len; i++) {
                    putAll(Array.get(keys, i), Array.get(elements, i), strict);
                }
            } else {
                for (int i = 0; i < len; i++) {
                    putAll(Array.get(keys, i), elements, strict);
                }
            }
        } else {
            if (elements != null && elements.getClass().isArray()) {
                throw new IllegalArgumentException("only one element per key");
            } else {
                put(keys, elements, strict);
            }
        }
    }


    /**
     * Helps with another convenient literal pattern, an array (not
     * a LitTree) of key-element pairs.  Each key-element pair is put
     * into the Table
     */
    public void putPairs(Object[][] pairs, boolean strict) {
        for (int i = 0; i < pairs.length; i++) {
            Object[] pair = pairs[i];
            if (pair.length != 2) {
                throw new IllegalArgumentException("must be a pair");
            }
            put(pair[0], pair[1], strict);
        }
    }


    /**
     * Helps with a literal pattern for expressing multi-valued
     * mappings.  'mappings' is an array (not a LitTree) of maps,
     * where a map is an array whose zeroth member is a key and whose
     * remaining members are the elements the key maps to.  Since
     * Table only represents single-valued mapping directly, the
     * multi-valued mapping is represented as a Table that maps the
     * key onto a Table whose domain is the set of elements.
     */
    public void putMulti(Object[][] mappings, boolean strict) {
        for (int i = 0; i < mappings.length; i++) {
            Object[] map = mappings[i];
            Object key = map[0];
            Table elements;
            if (!strict && containsKey(key)) {
                elements = (Table)get(key);
            } else {
                elements = new Table(OBJECT_TYPE, Void.TYPE);
                put(key, elements, true);
            }
            for (int j = 1; j < map.length; j++) {
                elements.put(map[j], null, strict);
            }
        }
    }


    /**
     * Defaults 'strict' to false
     */
    public void remove(Object key) {
        remove(key, false);
    }


    /**
     * Removes the given key (or its equivalent, according to the
     * equal function) from the collection. If 'strict' is false (the
     * default), this does nothing if 'key' is not currently a key in
     * the collection.  If 'strict' is true and 'key' isn't already
     * there to be removed, this throws an Exception (XXX currently an
     * IllegalArgumentException).  <p>
     *
     * Unlike Dictionary, this does not return the old value.  If you
     * want this for a Table, use 'get' first.
     *
     * @param key the key to remove
     */
    public void remove(Object key, boolean strict) {
        int slot = myKeys.findSlotOf(key);
        if (slot != -1) {
            writeFault();
            myKeys.vacate(slot);
            myValues.vacate(slot);

        } else if (strict) {
            throw new IllegalArgumentException(key + " not in table");
        }
    }


    /**
     * 'keys' is a LitTree (as defined in Table.putAll()).  All
     * members of the LitTree are removed from this Table
     *
     * @see ec.tables.Table#putAll
     */
    public void removeAll(Object keys, boolean strict) {
        if (keys != null && keys.getClass().isArray()) {
            int len = Array.getLength(keys);
            for (int i = 0; i < len; i++) {
                removeAll(Array.get(keys, i), strict);
            }
        } else {
            //base case
            remove(keys, strict);
        }
    }


    /**
     * Removes all associations from this table, leaving this table
     * empty. <p>
     *
     * Rather than doing a write-fault (which would make a private
     * copy to be immediately dropped) this decrements the sharing
     * count and re-initializes.
     */
    public void clear() {
        myShareCount = myShareCount.release();
        int numSlots = myKeys.numSlots();
        myKeys = (KeyColumn)myKeys.newVacant(numSlots);
        myValues = myValues.newVacant(numSlots);
    }


    /**
     * Unlike java.util.Hashtable, this part efficiently makes a lazy
     * copy by copy-on-write sharing.  Modify operations on a shared
     * Table then cause the delayed copy to happen.
     */
    public Object clone() {
        return new Table(myKeys,
                         myValues,
                         myNextOfKin,
                         myLoadFactor,
                         myShareCount.dup());
    }


    /**
     * Describes the weakness attributes of this table by returning
     * one of STRONG, KEYS_WEAK, VALUES_WEAK, or BOTH_WEAK.
     */
    public int weakness() {
        return myWeakness;
    }


    /**
     * Does this Table compare keys based on EQ identity rather than
     * equality?
     */
    public boolean isIdentity() {
        return myKeys instanceof IdentityKeyColumn;
    }


    /**
     * Note that this isn't a write fault!
     *
     * Called by some appropriate garbage collector to collect an
     * association whose weak reference points at something being
     * collected.  The actions of this method are considered to be an
     * atomic part of the garbage collector.
     */
    /*package*/ void garbageCollect(int slot) {
        if (! myKeys.isSlotTaken(slot)) {
            return;
        }
        Object key = myKeys.get(slot);
        Object value = myValues.get(slot);
        myKeys.vacate(slot);
        myValues.vacate(slot);

        if (myNextOfKin != null) {
            if (myWeakness == KEYS_WEAK) {
                myNextOfKin <- estate(this, value);
            } else if (myWeakness == VALUES_WEAK) {
                myNextOfKin <- estate(this, key);
            } else {
                throw new InternalError("I'm very confused");
            }
        }
    }


    /**
     * All keys in this collection must be of this type
     */
    public Class keyType() {
        return myKeys.memberType();
    }


    /**
     * All values in this table must be of this type
     */
    public Class valueType() {
        return myValues.memberType();
    }


    /*package*/ void writeFault() {
        if (myShareCount.isExclusive()) {
            return;
        }
        myShareCount = myShareCount.release();
        myKeys = (KeyColumn)myKeys.clone();
        myValues = (Column)myValues.clone();
    }


    /*package*/ void rehash() {
        if (! myShareCount.isExclusive()) {
            myShareCount = myShareCount.release();
        }
        KeyColumn keys = myKeys;
        Column values = myValues;
        int num = 1 + Math.max((keys.numSlots() * 3) / 2,
                               (int)(keys.numTaken() / mySizeThreshold));
        myKeys = (KeyColumn)keys.newVacant(num);
        num = myKeys.numSlots();
        myValues = myValues.newVacant(num);
        mySizeThreshold = (int)(num * myLoadFactor);
        for (int i = 0; ; i++) {
            i = keys.firstTaken(i);
            if (i == -1) {
                return;
            }
            this.put(keys.get(i), values.get(i));
        }
    }
}
