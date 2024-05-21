/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

class AtThrows extends AtCommand {

    int lookahead() {
        return 1;
        /* name | trusted/suspect | nullOK/nullFatal */
    }

    boolean expectSemiColon() {
        return true;
    }

    private String myName = null;

    void addAnnotation(String s)
            throws StringNotUnderstoodException {

        myName = s;
    }

    /** get the name of the exception in the AtThrows.
     *  XXX Note, at the current time it doesn't do a type lookup on \
     *  unqualified names. i need to look at where that should go to
     *  make it neatest.
     *
     *  @return nullFatal; the name of the exception.
     */
    String name() {
        return (myName == null) ? "" : myName;
    }

}