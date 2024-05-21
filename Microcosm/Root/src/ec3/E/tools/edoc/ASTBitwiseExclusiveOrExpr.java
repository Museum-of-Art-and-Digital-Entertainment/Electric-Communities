/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBitwiseExclusiveOrExpr extends SimpleNode {
  ASTBitwiseExclusiveOrExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBitwiseExclusiveOrExpr(id);
  }
}
