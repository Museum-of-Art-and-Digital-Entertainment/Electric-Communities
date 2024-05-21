package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 * Based on the GenericHashtable in the vat by Dan Bornstein
 */
abstract /*package*/ class KeyColumn extends Column {

    /**
     * The array of keys in the table.  null is a valid entry.
     * The value of an entry is only valid if indicated by the
     * corresponding myKeyStatus entry.
     */
    /*package*/ Object myKeys[];

    /**
     * The array of status of keys in the table.
     */
    /*package*/ byte myKeyStatus[];

    /**
     * Values for key status indicators
     */
    /*package*/ static final byte KEY_UNUSED = 0;
    /*package*/ static final byte KEY_USED = 1;
    /*package*/ static final byte KEY_DELETED = 2;


    /**
     * The number of key currently in the column.
     */
    /*package*/ int myNumTaken;

    /**
     * The number of key currently marked deleted in the column.
     */
    /*package*/ int myNumDeleted;

    /**
     * Construct a new, empty KeyColumn, with the specified parameters.
     *
     * @param memberType All keys will be (implicitly) checked for
     * conformance to this type. 
     * @param numSlots The column will hold at least this number of
     * slots.  To find out the actual number, call numSlots() on the
     * constructed column.
     */
    protected KeyColumn(Class memberType,
                        int numSlots) {
        numSlots = firstSize(numSlots);
        myKeys = (Object[])Array.newInstance(memberType, numSlots);
        myKeyStatus = new byte[numSlots];
        myNumTaken = 0;
        myNumDeleted = 0;
    }

    
    /**
     *
     */
    static public KeyColumn make(boolean isIdentity,
                                 Class memberType,
                                 int numSlots) {
        if (isIdentity) {
            return new IdentityKeyColumn(memberType, numSlots);
        } else {
            return new EqualityKeyColumn(memberType, numSlots);
        }
    }


    /*package*/ KeyColumn(Object[] keys,
                          byte[] keyStatus,
                          int numTaken,
                          int numDeleted) {
        myKeys = keys;
        myKeyStatus = keyStatus;
        myNumTaken = numTaken;
        myNumDeleted = numDeleted;
    }

    /*package*/ int numSlots() {
        return myKeys.length;
    }

    /**
     * Get the number of keys in the column
     */
    /*package*/ int numTaken() {
        return myNumTaken;
    }

    /**
     * Given a slot number, say whether this slot contains a valid key.
     */
    /*package*/ boolean isSlotTaken(int slot) {
        return ((slot >= 0) && (myKeyStatus[slot] == KEY_USED));
    }

    /*package*/ Object get(int slot) {
        return myKeys[slot];
    }

    /*package*/ void put(int slot, Object value) {
        throw new Error("internal: don't 'put' on a KeyColumn");
    }

    /**
     * cause slot not to contain a valid key
     */
    /*package*/ void vacate(int slot) {
        if (! isSlotTaken(slot)) {
            return;
        }
        myNumTaken--;
        myNumDeleted++;
        myKeys[slot] = null;
        myKeyStatus[slot] = KEY_DELETED;
    }

    /**
     * mark slot as containing a valid key
     */
    /*package*/ void markUsed(int slot) {
        myKeyStatus[slot] = KEY_USED;
    }

    /*package*/ Class memberType() {
        return myKeys.getClass().getComponentType();
    }

    /**
     * Returns the first non-vacant slot number at or after 'slot', or
     * -1 if none. 
     */
    /*package*/ int firstTaken(int slot) {
        if (myNumTaken == 0) {
            return -1;
        }
        for (; slot < myKeys.length; slot++) {
            if (myKeyStatus[slot] == KEY_USED) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Returns the slot at which key resides, or -1 if the key is
     * absent from the table.
     */
    abstract /*package*/ int findSlotOf(Object key);

    /** 
     * Put the given key into the table, and return its slot
     * number. If the key is equivalent (according to the equal
     * function) to a key already in the table, that slot number is
     * returned.  If the key is novel but the table is too small to
     * add it, a -1 is returned.
     *
     * @param key the key to place in the table
     * @return the slot number at which the key now resides in the
     * table, or -1 if we need more room. 
     */
    abstract /*package*/ int store(Object key);


    /**
     * Little sorted array of primes for use to size key columns.
     * The elements grow exponentially at somewhat less than 2x.
     */
    static final private int possibleSizes[] = {
      17, 23, 37, 53, 79, 109, 151, 211, 293, 421, 593, 829, 1171, 1637, 2293,
      3209, 4493, 6299, 8819, 12347, 17257, 24197, 33871, 47431, 66403,
      92959, 130147, 182209, 255107, 357139, 500009, 700027, 980047, 1372051,
      1920901, 2689261, 3764953, 5270939, 7379327, 10331063, 14463487,
      20248897, 28348447, 39687871, 55563023, 77788201, 108903523, 
      152464943, 213450911, 298831279, 418363789, 585709217, 819993047, 
      1147990271, 1607186393
    };

    /** 
     * Returns the first good size for a key column that's no less
     * than candidate.  This will be a prime number so that we get
     * better distribution of elements through the table. 
     */
    static private int firstSize(int candidate) {
        for (int i = 0; i < possibleSizes.length; i++) {
            if (candidate <= possibleSizes[i]) {
                return possibleSizes[i];
            }
        }
        throw new IllegalArgumentException("too big");
    }

    static /*package*/ int skip(int hash, int len) {
        int result = (hash + (hash / len)) % len;
        return Math.max(1, result);
    }
}

