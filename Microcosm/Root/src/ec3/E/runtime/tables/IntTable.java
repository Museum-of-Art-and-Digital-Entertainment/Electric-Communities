package ec.tables;

import java.util.NoSuchElementException;
import java.util.Enumeration;


/**
 * An optimization to provide a non-boxing protocol for int gets and
 * puts.
 */
public class IntTable extends Table {

    /*package*/ IntTable(KeyColumn keys,
                         NextOfKin nextOfKin,
                         float loadFactor) {
        super(keys,
              new IntColumn(keys.numSlots()),
              nextOfKin,
              loadFactor);
    }

    /*package*/ /* JAY -- had to add this to clear up a foo2j bug where it was not finding constructor */
    IntTable(KeyColumn keys,
            Column values,
            NextOfKin nextOfKin,
            float loadFactor,
            ShareCount shareCount) {
            super(keys,
              values,
              nextOfKin,
              loadFactor,
              shareCount);
    }

    /**
     * Reasonable defaults
     */
    public IntTable() {
        this(new EqualityKeyColumn(), null, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Reasonable defaults
     */
    /*package*/ IntTable(KeyColumn keys) {
        this(keys, null, DEFAULT_LOAD_FACTOR);
    }


    /**
     * Reasonable defaults
     */
    public IntTable(Class keyType) {
        this(new EqualityKeyColumn(keyType));
    }


    /**
     * Reasonable defaults
     */
    public IntTable(boolean isIdentity, Class keyType) {
        this(KeyColumn.make(isIdentity, keyType, DEFAULT_NUM_SLOTS));
    }


    /**
     * Reasonable defaults
     */
    public IntTable(boolean isIdentity, Class keyType, int initCap) {
        //XXX confusion between initCap and initial numSlots
        this(KeyColumn.make(isIdentity, keyType, initCap));
    }


    /**
     * Reasonable defaults
     */
    public IntTable(boolean isIdentity, int initCap) {
        //XXX confusion between initCap and initial numSlots
        this(KeyColumn.make(isIdentity, OBJECT_TYPE, initCap));
    }


    private IntTable(KeyColumn keys,
                     IntColumn values,
                     NextOfKin nextOfKin,
                     float loadFactor,
                     ShareCount shareCount) {
        super(keys,
              values,
              nextOfKin,
              loadFactor,
              shareCount);
    }


    /**
     *
     */
    public IntTable(ArgsHolder prefaceArgs) {
        super(prefaceArgs);
        IntColumn foo = (IntColumn)myValues;    //type assertion
    }


    /**
     * unboxed value optimization
     *
     * @see ec.tables.Table#get
     */
    public int getInt(Object key) throws NoSuchElementException {
        int slot = myKeys.findSlotOf(key);
        if (slot == -1) {
            throw new NoSuchElementException("key not found");
        }
        return ((IntColumn)myValues).getInt(slot);
    }

    /**
     * unboxed value optimization
     *
     * @see ec.tables.Table#get
     */
    public int getInt(Object key, int instead) {
        int slot = myKeys.findSlotOf(key);
        if (slot == -1) {
            return instead;
        }
        return ((IntColumn)myValues).getInt(slot);
    }


    /**
     * unboxed value optimization
     *
     * @see ec.tables.Table#put
     */
    public void putInt(Object key, int value) {
        putInt(key, value, false);
    }


    /**
     * unboxed value optimization
     *
     * @see ec.tables.Table#put
     */
    public void putInt(Object key, int value, boolean strict) {
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
                ((IntColumn)myValues).putInt(slot, value);
                return;
            }
            rehash();
        }
    }


    /**
     *
     */
    public Object clone() {
    /* JAY -- put in explicit casts to work around foo2j bug. */

        return new IntTable((KeyColumn) myKeys,
                            (Column) myValues,
                            (NextOfKin) myNextOfKin,
                            (float) myLoadFactor,
                            (ShareCount) myShareCount.dup());
    }
}
