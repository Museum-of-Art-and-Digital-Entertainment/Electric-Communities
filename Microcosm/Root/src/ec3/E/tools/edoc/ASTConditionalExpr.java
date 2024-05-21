/* JJT: 0.2.2 */

package ec.edoc;


public class ASTConditionalExpr extends SimpleNode {
  ASTConditionalExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTConditionalExpr(id);
  }
}
