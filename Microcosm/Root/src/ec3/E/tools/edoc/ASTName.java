/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;
/* JJT: 0.2.2 */

/** this represents a name (an identifier, or a FQ namE) in the parse tree
 */
public class ASTName extends SimpleNode {
    ASTName(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new ASTName(id);
    }
  
    // Manually added code here;

    /** get Name will pull out a FQ name from its children.
     */
    public String getName() {

        StringBuffer sb = new StringBuffer();
    
        int numChildren = this.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++ ) {
            sb.append(((ASTIdentifier)(this.jjtGetChild(i))).getName());
            if (i < (numChildren - 1)) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
    

}
