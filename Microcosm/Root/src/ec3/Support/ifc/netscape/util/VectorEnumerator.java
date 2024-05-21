// VectorEnumerator.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

class VectorEnumerator implements Enumeration {
    Vector vector;
    int    index;

    VectorEnumerator(Vector vector) {
        this.vector = vector;
        index = 0;
    }

    VectorEnumerator(Vector vector, int index) {
        this.vector = vector;
        this.index = index;
    }

    public boolean hasMoreElements() {
        return index < vector.count();
    }

    public Object nextElement() {
        return vector.elementAt(index++);
    }
}
