/* JJT: 0.2.2 */

package ec.edoc;


public class ASTInstanceOfExpr extends SimpleNode {
  ASTInstanceOfExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTInstanceOfExpr(id);
  }
}
