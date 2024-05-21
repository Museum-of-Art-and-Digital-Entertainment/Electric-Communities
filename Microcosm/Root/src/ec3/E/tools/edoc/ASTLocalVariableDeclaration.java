/* JJT: 0.2.2 */

package ec.edoc;


public class ASTLocalVariableDeclaration extends SimpleNode {
  ASTLocalVariableDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTLocalVariableDeclaration(id);
  }
}
