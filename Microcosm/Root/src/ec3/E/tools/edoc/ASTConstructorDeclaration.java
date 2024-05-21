/* JJT: 0.2.2 */

package ec.edoc;


public class ASTConstructorDeclaration extends SimpleNode {
  ASTConstructorDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTConstructorDeclaration(id);
  }
}
