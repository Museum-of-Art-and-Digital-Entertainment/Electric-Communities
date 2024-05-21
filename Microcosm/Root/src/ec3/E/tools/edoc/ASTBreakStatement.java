/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBreakStatement extends SimpleNode {
  ASTBreakStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBreakStatement(id);
  }
}
