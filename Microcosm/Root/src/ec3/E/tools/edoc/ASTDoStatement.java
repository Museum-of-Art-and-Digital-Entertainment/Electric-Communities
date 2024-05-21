/* JJT: 0.2.2 */

package ec.edoc;


public class ASTDoStatement extends SimpleNode {
  ASTDoStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTDoStatement(id);
  }
}
