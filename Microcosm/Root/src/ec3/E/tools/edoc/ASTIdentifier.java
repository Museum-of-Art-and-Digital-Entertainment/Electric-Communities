/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

/**
 *  This represents an identifier in the parse tree.
 */
public class ASTIdentifier extends SimpleNode {
    private String name;

    public static Node jjtCreate(String id) {
        return new ASTIdentifier(id);
    }

    ASTIdentifier(String id){
        super(id);
    }

    public void setName(String n) {
        name = n;
    }

    /** this method retrieves the identifer
     */
    public String getName() {
        return name;
    }

    public String toString() {
        return "Identifier: " + name;
    }
}
