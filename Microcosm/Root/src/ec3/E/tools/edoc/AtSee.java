/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

class AtSee extends AtCommand {

    int lookahead() {
        return 1;
        /* other class / method */
    }

    boolean expectSemiColon() {
        return true;
    }

    private String myLink = null;

    void addAnnotation(String s)
            throws StringNotUnderstoodException {

        //System.out.println("Adding see annotation as \""+s+"\"");

        myLink = s;
    }

    /** get the link refered to by this AtSee
     *  XXX Note, at the current time it doesn't do a type lookup on \
     *  unqualified names. i need to look at where that should go to
     *  make it neatest.
     *
     *  @return nullFatal; the string representing the link.
     */
    String link() {
        return (myLink == null) ? "" : myLink;
    }


}