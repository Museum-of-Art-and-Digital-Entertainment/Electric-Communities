/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** This class simply holds an integer, used to store a pointer to a child. 
 *  It allows us to pass a pointer to an integer. */
class PointerToInteger {
    int datum;
    PointerToInteger(int i) {
        datum = i;
    }
}


