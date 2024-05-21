/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBlockStatement extends SimpleNode {
  ASTBlockStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBlockStatement(id);
  }
}
