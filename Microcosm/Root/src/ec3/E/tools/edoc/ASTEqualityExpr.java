/* JJT: 0.2.2 */

package ec.edoc;


public class ASTEqualityExpr extends SimpleNode {
  ASTEqualityExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTEqualityExpr(id);
  }
}
