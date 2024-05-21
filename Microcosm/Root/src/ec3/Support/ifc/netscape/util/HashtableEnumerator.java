// HashtableEnumerator.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

class HashtableEnumerator implements Enumeration {
    boolean keyEnum;
    int index;
    int returnedCount;
    Hashtable table;

    HashtableEnumerator(Hashtable table, boolean keyEnum) {
        super();
        this.table = table;
        this.keyEnum = keyEnum;
        returnedCount = 0;

        if (table.keys != null)
            index = table.keys.length;
        else
            index = 0;
    }

    public boolean hasMoreElements() {
        if (returnedCount < table.count)
            return true;

        return false;
    }

    public Object nextElement() {
        index--;

        while (index >= 0 && table.elements[index] == null)
            index--;

        if (index < 0 || returnedCount >= table.count)
            throw new NoSuchElementException();

        returnedCount++;

        if (keyEnum)
            return table.keys[index];

        return table.elements[index];
    }
}
