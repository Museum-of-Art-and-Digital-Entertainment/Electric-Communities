/* JJT: 0.2.2 */

package ec.edoc;


public class ASTTryStatement extends SimpleNode {
  ASTTryStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTTryStatement(id);
  }
}
