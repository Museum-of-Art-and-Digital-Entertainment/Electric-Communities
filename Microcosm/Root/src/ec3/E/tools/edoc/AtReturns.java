/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

class AtReturns extends AtCommand {

    int lookahead() {
        return 2;
        /* trusted/suspect | nullOK/nullFatal */
    }

    boolean expectSemiColon() {
        return true;
    }

    private Boolean myTrust = null;
    private Boolean myNullability = null;

    void addAnnotation(String s)
            throws StringNotUnderstoodException {

        if (s.equals("trusted")) {
            myTrust = Boolean.TRUE;
        } else if (s.equals("suspect") || s.equals("untrusted")) {
            myTrust = Boolean.FALSE;
        } else if (s.equals("nullFatal")) {
            myNullability = Boolean.FALSE;
        } else if (s.equals("nullOK") || s.equals("nullOk")) {
            myNullability = Boolean.TRUE;
        } else {
            throw new StringNotUnderstoodException(s);
        }
    }

    /** get whether this is 'trusted' or not
     *  @returns nullOK; null if we have had no declaration, Boolean.TRUE if
     *   the return value is 'trusted', Boolean.FALSE if the return value
     *   is 'suspect'
     */
    Boolean trust() {
        return myTrust;
    }

    /** get whether this is 'null safe' or not
     *  @returns nullOK; null if we have had no declaration, Boolean.TRUE if
     *   the return might be null, Boolean.FALSE if the return will never
     *   be null
     */
    Boolean nullability() {
        return myNullability;
    }


}