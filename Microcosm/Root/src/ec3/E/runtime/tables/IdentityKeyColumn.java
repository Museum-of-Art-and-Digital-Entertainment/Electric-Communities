package ec.tables;

import java.lang.reflect.Array;


/**
 */
/*package*/ class IdentityKeyColumn extends KeyColumn {

    /**
     * The maximum number of probes made so far.
     */
    private int myMaxProbes;

    private boolean myIsWeak;

    public IdentityKeyColumn(Class memberType,
                             int numSlots,
                             boolean isWeak) {
        super(memberType, numSlots);
        myMaxProbes = 0;
        myIsWeak = isWeak;
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(Class memberType, int numSlots) {
        this(memberType, numSlots, false);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(int numSlots) {
        this(THE_OBJECT_CLASS, numSlots, false);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(Class memberType) {
        this(memberType, Table.DEFAULT_NUM_SLOTS, false);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn() {
        this(THE_OBJECT_CLASS, Table.DEFAULT_NUM_SLOTS, false);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(int numSlots, boolean isWeak) {
        this(THE_OBJECT_CLASS, numSlots, isWeak);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(Class memberType, boolean isWeak) {
        this(memberType, Table.DEFAULT_NUM_SLOTS, isWeak);
    }

    /**
     * Reasonable defaults
     */
    public IdentityKeyColumn(boolean isWeak) {
        this(THE_OBJECT_CLASS, Table.DEFAULT_NUM_SLOTS, isWeak);
    }

    private IdentityKeyColumn(Object[] keys,
                              byte[] keyStatus,
                              int numTaken,
                              int numDeleted,
                              int maxProbes,
                              boolean isWeak) {
        super(keys, keyStatus, numTaken, numDeleted);
        myMaxProbes = maxProbes;
        myIsWeak = isWeak;
    }

    /*package*/ boolean isWeak() {
        return myIsWeak;
    }

    /*package*/ int findSlotOf(Object key) {
        int hash = System.identityHashCode(key);
        if (hash < 0) {
            hash = -hash;
        }
        int curProbe = hash % myKeys.length;

        //do the first probe before the loop, so we only calculate
        //probeSkip if the first one misses
        Object curKey;
        byte curStatus = myKeyStatus[curProbe];
        if (curStatus == KEY_UNUSED) {
            return -1;
        }
        curKey = myKeys[curProbe];
        if ((curStatus == KEY_USED) && (curKey == key)) {
            //we found it
            return curProbe;
        }
        int probeSkip = skip(hash, myKeys.length);
        int initialProbe = curProbe;
        for (;;) {
            curProbe += probeSkip;
            if (curProbe >= myKeys.length) {
                curProbe -= myKeys.length;
            }
            if (curProbe == initialProbe) {
                // not in table
                return -1;
            }
            curStatus = myKeyStatus[curProbe];
            if (curStatus == KEY_UNUSED) {
                // not in table
                return -1;
            }
            curKey = myKeys[curProbe];
            if ((curStatus == KEY_USED) && (curKey == key)) {
                //we found it
                return curProbe;
            }
        }
    }

    /*package*/ int store(Object key) {
        int hash = System.identityHashCode(key);
        if (hash < 0) {
            hash = -hash;
        }
        int curProbe = hash % myKeys.length;
        int initialProbe = curProbe;
        int probeSkip = skip(hash, myKeys.length);
        int firstVacant = -1;
            
        // search the array for the key
        for (;;) {
            Object curKey = myKeys[curProbe];
            byte curStatus = myKeyStatus[curProbe];
            if (curStatus == KEY_UNUSED) {
                if (firstVacant != -1) {
                    return occupy(firstVacant, key);
                } else {
                    return occupy(curProbe, key);
                }
            } else if (curStatus == KEY_DELETED) {
                //the slot is vacant
                if (firstVacant == -1) {
                    // we found the first vacant slot of the search path.
                    // It'll be used if the element isn't eventually found
                    firstVacant = curProbe;
                }
            } else if (curKey == key) {
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
                    return occupy(firstVacant, key);
                } else {
                    return -1;
                }
            }
        }
    }

    private int occupy(int slot, Object key) {
        myKeys[slot] = key;
        myNumTaken++;
        if (myKeyStatus[slot] == KEY_DELETED) {
            myNumDeleted--;
        }
        myKeyStatus[slot] = KEY_USED;
        return slot;
    }

    protected Object clone() {
        int len = myKeys.length;
        Object[] keys = (Object[])Array.newInstance(memberType(), len);
        byte[] status = new byte[len];
        System.arraycopy(myKeys, 0, keys, 0, len);
        System.arraycopy(myKeyStatus, 0, status, 0, len);

        return new IdentityKeyColumn(keys,
                                     status,
                                     myNumTaken,
                                     myNumDeleted,
                                     myMaxProbes,
                                     myIsWeak);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new IdentityKeyColumn(memberType(),
                                     numSlots,
                                     myIsWeak);
    }
}
