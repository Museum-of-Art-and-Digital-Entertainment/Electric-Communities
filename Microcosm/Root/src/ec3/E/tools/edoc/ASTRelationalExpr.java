/* JJT: 0.2.2 */

package ec.edoc;


public class ASTRelationalExpr extends SimpleNode {
  ASTRelationalExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTRelationalExpr(id);
  }
}
