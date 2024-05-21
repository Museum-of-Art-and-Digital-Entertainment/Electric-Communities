/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

/** this exception is thrown if we try to deal with an @command which
 *  does not correspond to something we understand */
class StringNotUnderstoodException extends Exception {
    StringNotUnderstoodException(String s) {
        super(s);
    }
}

