/* JJT: 0.2.2 */

package ec.edoc;


public class ASTModifier extends SimpleNode {
  ASTModifier(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTModifier(id);
  }
  
    /** This method retrieves an int for this node. 
     *  @return; the appropriate Info.BLAH int
     */
    int intValue() throws MalformedASTException {
        
        String modifierName = null;
        try { /* if !instanceof token, then die */
            modifierName = ((Token)this.getInfo()).image;
        } catch (ClassCastException e) {
            throw new MalformedASTException();
        }
        
        if (modifierName.compareTo("public") == 0) {
            return Info.PUBLIC;
        }
        if (modifierName.compareTo("private") == 0) {
            return Info.PRIVATE;
        }
        if (modifierName.compareTo("protected") == 0) {
            return Info.PROTECTED;
        }
        if (modifierName.compareTo("static") == 0) {
            return Info.STATIC;
        }
        if (modifierName.compareTo("abstract") == 0) {
            return Info.ABSTRACT;
        }
        if (modifierName.compareTo("final") == 0) {
            return Info.FINAL;
        }
        if (modifierName.compareTo("native") == 0) {
            return Info.NATIVE;
        }
        if (modifierName.compareTo("synchronized") == 0) {
            return Info.SYNCHRONIZED;
        }
        if (modifierName.compareTo("volatile") == 0) {
            return Info.VOLATILE;
        }
        if (modifierName.compareTo("transient") == 0) {
            return Info.TRANSIENT;
        }
        return 0;
    }
    
  
}
