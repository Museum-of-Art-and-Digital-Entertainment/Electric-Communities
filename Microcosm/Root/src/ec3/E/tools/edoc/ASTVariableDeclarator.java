/* JJT: 0.2.2 */

package ec.edoc;


public class ASTVariableDeclarator extends SimpleNode {
  ASTVariableDeclarator(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTVariableDeclarator(id);
  }
}
