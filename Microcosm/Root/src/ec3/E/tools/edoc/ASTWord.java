/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;
/* JJT: 0.2.2 */

/** This class is just used to store the words of a comment, which are then
 *  retrieved to rebuild the text at a later date.
 */
public class ASTWord extends SimpleNode {

    private String name;

    ASTWord(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new ASTWord(id);
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "Word: " + name;
    }

}

