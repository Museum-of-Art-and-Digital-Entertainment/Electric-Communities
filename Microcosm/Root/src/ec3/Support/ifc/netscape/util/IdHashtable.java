// IdHashtable.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** @private
  */
public class IdHashtable {
    /** For the multiplicative hash choose A = ((sqrt(5) - 1) / 2) * (1 << 32)
      */
    static final int A = 0x9e3779b9;

    /** You cannot store this value in the table.  This value should
      * probably change to -1 if this class is made public.
      */
    static final int NOT_FOUND = 0;

    int power;
    int count;
    int maxCount;
    int indexMask;
    Object keys[];
    int values[];
    boolean equals;

    /** We have two different uses of this class: one wants equals()
      * comparisons, the other wants == comparisons.
      */
    IdHashtable(boolean equals) {
        super();

        this.equals = equals;
        power = 5;
        count = 0;
        indexMask = (1 << power) - 1;
        maxCount = (3 * (1 << power)) / 4;
        keys = new Object[1 << power];
        values = new int[1 << power];
    }

    private boolean equalKeys(Object keyA, Object keyB) {
        if (keyA == keyB)
            return true;

        if (equals)
            return keyA.equals(keyB);

        return false;
    }

    private void rehash() {
        int i, oldLength, oldValues[];
        Object oldKeys[];

        oldLength = keys.length;
        oldKeys = keys;
        oldValues = values;

        power++;
        count = 0;
        indexMask = (1 << power) - 1;
        maxCount = (3 * (1 << power)) / 4;
        keys = new Object[1 << power];
        values = new int[1 << power];

        for (i = 0; i < oldLength; i++) {
            if (oldKeys[i] != null)
                putKnownAbsent(oldKeys[i], oldValues[i]);
        }
    }

    int get(Object object) {
        int product, index, step, probeCount;
        Object key;

        // On sparc it appears that the last 3 bits of Object.hashCode() are
        // insignificant!  ALERT!

        product = object.hashCode() * A;
        index = product >>> (32 - power);

        key = keys[index];
        if (key == null)
            return NOT_FOUND;
        else if (equalKeys(key, object))
            return values[index];

        step = ((product >>> (32 - 2 * power)) & indexMask) | 1;
        probeCount = 1;

        do {
            probeCount++;
            index = (index + step) & indexMask;

            key = keys[index];
            if (key == null)
                return NOT_FOUND;
            else if (equalKeys(key, object))
                return values[index];

        } while (probeCount <= count);

        throw new InconsistencyException("IdHashtable overflow");
    }

    void putKnownAbsent(Object object, int id) {
        int product, index, step, probeCount;
        Object key;

        if (count >= maxCount)
            rehash();

        // On sparc it appears that the last 3 bits of Object.hashCode() are
        // insignificant!  ALERT!

        product = object.hashCode() * A;
        index = product >>> (32 - power);

        if (keys[index] == null) {
            keys[index] = object;
            values[index] = id;
            count++;
            return;
        }

        step = ((product >>> (32 - 2 * power)) & indexMask) | 1;
        probeCount = 1;

        do {
            probeCount++;
            index = (index + step) & indexMask;

            if (keys[index] == null) {
                keys[index] = object;
                values[index] = id;
                count++;
                return;
            }
        } while (probeCount <= count);

        throw new InconsistencyException("IdHashtable overflow");
    }
}
