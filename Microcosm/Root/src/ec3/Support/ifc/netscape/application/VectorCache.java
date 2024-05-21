// VectorCache.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/**
  * @private
  * @note 1.0 went from kit private to public tag private
  */
public class VectorCache extends Object {
    static private Vector    _vectorCache = new Vector();
    static private boolean   _shouldCache = true;

    public static Vector newVector() {
        Vector       theVector;

        synchronized(_vectorCache) {
            if (!_shouldCache || _vectorCache.isEmpty()) {
                return new Vector();
            }

            theVector = (Vector)_vectorCache.removeLastElement();
        }

        return theVector;
    }

    public static void returnVector(Vector aVector) {
        if (!_shouldCache) {
            return;
        }

        synchronized(_vectorCache) {
            if (aVector != null && _vectorCache.count() < 15) {
                aVector.removeAllElements();
                _vectorCache.addElement(aVector);
            }
        }
    }

    static void setShouldCacheVectors(boolean flag) {
        synchronized(_vectorCache) {
            _shouldCache = flag;

            if (!_shouldCache) {
                _vectorCache.removeAllElements();
            }
        }
    }
}
