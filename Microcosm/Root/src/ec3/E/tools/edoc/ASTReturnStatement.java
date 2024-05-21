/* JJT: 0.2.2 */

package ec.edoc;


public class ASTReturnStatement extends SimpleNode {
  ASTReturnStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTReturnStatement(id);
  }
}
