/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

import java.util.Vector;
import java.util.Enumeration;

/** this class holds all the interesting information about a doc comment
 *  it stores a textual description, ie. the bit before the @commands start,
 *  and some collection of AtCommands
 */
class Comment {

    /** Used to store the string description */
    private String myDescription = "";

    /** Get this comment's description
     *  @returns nullFatal; the string description for this comment
     */
    String description() {
        return myDescription;
    }

    /** Set the string description of this comment
     *  @param description, nullOK; the appropriate string. null is ignored
     */
    void description(String description) {
        if (description != null) {
            myDescription = description;
        }
    }

    /** used to store the collection of @commands for this comment */
    private Vector myAtCommands = new Vector();

    /** Add an @command to this comment
     *  @param c, nullOK; the AtCommand to be added. null is ignored
     *  @see AtCommand
     */
    void addAtCommand(AtCommand c) {
        if (c != null) {
            myAtCommands.addElement(c);
        }
    }

    /** Get the collection of @commands for this comment
     *  @returns nullFatal; an Enumeration of the AtCommands in question.
     *  @see Enumeration
     *  @see AtCommand
     */
    Enumeration getAtCommands() {
        return myAtCommands.elements();
    }
}