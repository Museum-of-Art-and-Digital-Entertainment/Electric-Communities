/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;
/* JJT: 0.2.2 */

/** This class represents part of variable declarations in the syntax tree.
 *  It was extended to that it knows how to pull array brackets[] from 
 *  declarations so that that information can be moved into the type info for 
 *  that variable.
 */
public class ASTVariableDeclaratorId extends SimpleNode {
    ASTVariableDeclaratorId(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new ASTVariableDeclaratorId(id);
    }

    public int getBrackets() {

        int children = this.jjtGetNumChildren();

        // we _REALLY_ should have at least one child...
        if (children == 0) {
            return -1;
        }

        // child 0 is the identifier

        for (int i = 1; i < children; i++) {
            if (!((this.jjtGetChild(i)) instanceof ASTArrayBrackets)) {
    
                return -1; // we should only find brackets...
            }
        }
    
        return (children - 1);
    }
    
    public String getName() {

        int children = this.jjtGetNumChildren();

        // we _REALLY_ should have at least one child...
        if (children == 0) {
            return null;
        }
    
        if (children == 1) {
            return (((ASTIdentifier)(this.jjtGetChild(0))).getName());
        } else {
            StringBuffer sb = new StringBuffer(
                ((ASTIdentifier)(this.jjtGetChild(0))).getName());
      
            for (int i = 1; i < children; i++) {
                if ((this.jjtGetChild(i)) instanceof ASTArrayBrackets) {
      
                    sb.append("[]");
                }
            }
            return sb.toString();
        }
    }
}
