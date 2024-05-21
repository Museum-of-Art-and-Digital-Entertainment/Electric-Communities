/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

class AtParam extends AtCommand {

    int lookahead() {
        return 3;
        /* name | trusted/suspect | nullOK/nullFatal */
    }

    boolean expectSemiColon() {
        return true;
    }

    private String myName = null;
    private Boolean myTrust = null;
    private Boolean myNullability = null;

    void addAnnotation(String s)
            throws StringNotUnderstoodException {

        if (myName == null) {
            /* then the first annotation is assumed to be the name */
            myName = s;
        } else if (s.equals("trusted")) {
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

    /** get the name of this AtParam
     *  @returns nullFatal; a string containing the name if this has one.
     */
    String name() {
        return (myName == null ? "" : myName);
    }

    /** get whether this is 'trusted' or not
     *  @returns nullOK; null if we have had no declaration, Boolean.TRUE if
     *   the param is 'trusted', Boolean.FALSE if the param is 'suspect'
     */
    Boolean trust() {
        return myTrust;
    }

    /** get whether this is 'null safe' or not
     *  @returns nullOK; null if we have had no declaration, Boolean.TRUE if
     *   the param can safely be null, Boolean.FALSE if the param must
     *   not be null
     */
    Boolean nullability() {
        return myNullability;
    }





}