/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;
/* JJT: 0.2.2 */

/** this class represents a type in the parse tree. It has been extended so 
 *  that it knows how to return a string type whether it represents a class
 *  type or a primitive type
 */
public class ASTType extends SimpleNode {
    ASTType(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new ASTType(id);
    }

    // Manually added code here

    /** this method returns a string of the type.
     *  this will work regardless of whether it is primitive or class.
     *  it does require the parse tree, though
     */
    String getName() {

        int children = this.jjtGetNumChildren();
        StringBuffer sb = null;
        if (this.jjtGetNumChildren() == 0) {
            return null;
        }

        Node n = (this.jjtGetChild(0));

        if (n instanceof ASTPrimitiveType) {
            sb = new StringBuffer(((Token)(((ASTPrimitiveType)n).getInfo())).image);
        } else if (n instanceof ASTName) {
            sb = new StringBuffer(((ASTName)n).getName());
        } 

        for( int i = 1; i < children; i++ ) {
            sb.append("[]");
        }
        return sb.toString();
    }
}

