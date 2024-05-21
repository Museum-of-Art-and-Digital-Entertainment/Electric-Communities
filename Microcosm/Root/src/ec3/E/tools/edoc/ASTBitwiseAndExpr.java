/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBitwiseAndExpr extends SimpleNode {
  ASTBitwiseAndExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBitwiseAndExpr(id);
  }
}
