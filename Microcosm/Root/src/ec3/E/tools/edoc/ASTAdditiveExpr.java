/* JJT: 0.2.2 */

package ec.edoc;


public class ASTAdditiveExpr extends SimpleNode {
  ASTAdditiveExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTAdditiveExpr(id);
  }
}
