/* JJT: 0.2.2 */

package ec.edoc;


public class ASTWhileStatement extends SimpleNode {
  ASTWhileStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTWhileStatement(id);
  }
}
