/* JJT: 0.2.2 */

package ec.edoc;


public class ASTPackageDeclaration extends SimpleNode {
  ASTPackageDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTPackageDeclaration(id);
  }
  
    String getPackage() throws MalformedASTException {
    
        if (this.jjtGetNumChildren() < 1 
                || ! (this.jjtGetChild(0) instanceof ASTName)) {
            throw new MalformedASTException();
        }
        
        ASTName name = (ASTName)(this.jjtGetChild(0)); 
        StringBuffer sb = new StringBuffer();
        int children = name.jjtGetNumChildren();        
        for (int i = 0; i < children; i++ ) {
            Object o = name.jjtGetChild(i);
            if (o instanceof ASTIdentifier) {
                sb.append(((ASTIdentifier)o).getName());
            } else {
                throw new MalformedASTException();
            }
            if (i != (children - 1)) {
                sb.append(".");
            }
        }
        return sb.toString();
    }  
}
