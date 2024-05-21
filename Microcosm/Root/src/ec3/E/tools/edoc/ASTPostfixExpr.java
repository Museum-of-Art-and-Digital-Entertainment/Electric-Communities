/* JJT: 0.2.2 */

package ec.edoc;


public class ASTPostfixExpr extends SimpleNode {
  ASTPostfixExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTPostfixExpr(id);
  }
}
