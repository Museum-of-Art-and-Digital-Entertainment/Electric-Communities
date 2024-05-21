/* JJT: 0.2.2 */

package ec.edoc;


public class ASTFieldDeclaration extends SimpleNode {
  ASTFieldDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTFieldDeclaration(id);
  }
}
