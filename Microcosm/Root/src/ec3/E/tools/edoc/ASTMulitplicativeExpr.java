/* JJT: 0.2.2 */

package ec.edoc;


public class ASTMulitplicativeExpr extends SimpleNode {
  ASTMulitplicativeExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTMulitplicativeExpr(id);
  }
}
