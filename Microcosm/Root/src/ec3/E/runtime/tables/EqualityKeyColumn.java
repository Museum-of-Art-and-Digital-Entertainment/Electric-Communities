package ec.tables;

import java.lang.reflect.Array;


/**
 */
/*package*/ class EqualityKeyColumn extends KeyColumn {

    /**
     * The maximum number of probes made so far.
     */
    private int myMaxProbes;

    /**
     * The array of computed hashes for each key in the set.
     */
    private int myHashes[];

    /**
     *
     *
     * @param memberType
     * @param numSlots
     */
    public EqualityKeyColumn(Class memberType, int numSlots) {
        super(memberType, numSlots);
        myMaxProbes = 0;
        myHashes = new int[myKeys.length];
    }

    /**
     * Reasonable defaults
     */
    public EqualityKeyColumn(int numSlots) {
        this(THE_OBJECT_CLASS, numSlots);
    }

    /**
     * Reasonable defaults
     */
    public EqualityKeyColumn(Class memberType) {
        this(memberType, Table.DEFAULT_NUM_SLOTS);
    }

    /**
     * Reasonable defaults
     */
    public EqualityKeyColumn() {
        this(THE_OBJECT_CLASS, Table.DEFAULT_NUM_SLOTS);
    }

    private EqualityKeyColumn(Object[] keys,
                              byte[] status,
                              int numTaken,
                              int numDeleted,
                              int maxProbes,
                              int[] hashes) {
        super(keys, status, numTaken, numDeleted);
        myMaxProbes = maxProbes;
        myHashes = hashes;
    }

    /*package*/ void vacate(int slot) {
        super.vacate(slot);
        myHashes[slot] = 0;
    }

    /*package*/ int findSlotOf(Object key) {
        int hash = 0;
        if (key != null) {
            hash = key.hashCode();
            if (hash < 0) {
                hash = -hash;
            }
        }
        int curProbe = hash % myKeys.length;
        int initialProbe = curProbe;
        int probeSkip = skip(hash, myKeys.length);
            
        // search the array for the key (or equivalent)
        for (;;) {
            byte curStatus = myKeyStatus[curProbe];
            if (curStatus == KEY_UNUSED) {
                // not in table
                return -1;
            }
            Object curKey = myKeys[curProbe];
            if ((curStatus == KEY_USED) &&
                (hash == myHashes[curProbe]) &&
                (curKey.equals(key))) {
                // we found it.
                return curProbe;
            }
            curProbe += probeSkip;
            if (curProbe >= myKeys.length) {
                curProbe -= myKeys.length;
            }
            if (curProbe == initialProbe) {
                //not in table
                return -1;
            }
        }
    }

    /*package*/ int store(Object key) {
        int hash = 0;
        if (key != null) {
            hash = key.hashCode();
            if (hash < 0) {
                hash = -hash;
            }
        }
        int curProbe = hash % myKeys.length;
        int initialProbe = curProbe;
        int probeSkip = skip(hash, myKeys.length);
        int firstVacant = -1;
            
        // search the array for the key
        for (;;) {
            Object curKey = myKeys[curProbe];
            int curStatus = myKeyStatus[curProbe];
            if (curStatus == KEY_UNUSED) {
                if (firstVacant != -1) {
                    return occupy(firstVacant, key, hash);
                } else {
                    return occupy(curProbe, key, hash);
                }
            } else if (curStatus == KEY_DELETED) {
                //the slot is vacant
                if (firstVacant == -1) {
                    // we found the first vacant slot of the search path.
                    // It'll be used if the element isn't eventually found
                    firstVacant = curProbe;
                }
            } else if ((hash == myHashes[curProbe]) &&
                       (curKey.equals(key))) {
                // we found it.
                return curProbe;
            }
            curProbe += probeSkip;
            if (curProbe >= myKeys.length) {
                curProbe -= myKeys.length;
            }
            if (curProbe == initialProbe) {
                // we wrapped. Either we passed a deleted slot or
                // there's no room in the table 
                if (firstVacant != -1) {
                    return occupy(firstVacant, key, hash);
                } else {
                    return -1;
                }
            }
        }
    }

    private int occupy(int slot, Object key, int hash) {
        myNumTaken++;
        myKeys[slot] = key;
        if (myKeyStatus[slot] == KEY_DELETED) {
            myNumDeleted--;
        }
        myHashes[slot] = hash;
        myKeyStatus[slot] = KEY_USED;
        return slot;
    }

    protected Object clone() {
        int len = myKeys.length;
        Object[] keys = (Object[])Array.newInstance(memberType(), len);
        System.arraycopy(myKeys, 0, keys, 0, len);
        byte[] status = new byte[len];
        int[] hashes = new int[len];
        System.arraycopy(myKeyStatus, 0, status, 0, len);
        System.arraycopy(myHashes, 0, hashes, 0, len);

        return new EqualityKeyColumn(keys,
                                     status,
                                     myNumTaken,
                                     myNumDeleted,
                                     myMaxProbes,
                                     hashes);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new EqualityKeyColumn(memberType(),
                                     numSlots);
    }
}
