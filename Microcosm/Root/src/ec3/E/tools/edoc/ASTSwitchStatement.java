/* JJT: 0.2.2 */

package ec.edoc;


public class ASTSwitchStatement extends SimpleNode {
  ASTSwitchStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTSwitchStatement(id);
  }
}
