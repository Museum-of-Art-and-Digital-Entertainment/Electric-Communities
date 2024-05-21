/* JJT: 0.2.2 */

package ec.edoc;


public class ASTForStatement extends SimpleNode {
  ASTForStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTForStatement(id);
  }
}
