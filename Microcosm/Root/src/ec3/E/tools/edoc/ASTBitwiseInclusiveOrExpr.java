/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBitwiseInclusiveOrExpr extends SimpleNode {
  ASTBitwiseInclusiveOrExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBitwiseInclusiveOrExpr(id);
  }
}
