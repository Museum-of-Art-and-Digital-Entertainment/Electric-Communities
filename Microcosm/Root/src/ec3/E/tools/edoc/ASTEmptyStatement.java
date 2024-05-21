/* JJT: 0.2.2 */

package ec.edoc;


public class ASTEmptyStatement extends SimpleNode {
  ASTEmptyStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTEmptyStatement(id);
  }
}
