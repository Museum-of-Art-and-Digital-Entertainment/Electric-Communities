/* JJT: 0.2.2 */

package ec.edoc;


public class ASTIfStatement extends SimpleNode {
  ASTIfStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTIfStatement(id);
  }
}
