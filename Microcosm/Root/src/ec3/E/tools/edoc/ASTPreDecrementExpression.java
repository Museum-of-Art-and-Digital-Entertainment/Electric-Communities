/* JJT: 0.2.2 */

package ec.edoc;


public class ASTPreDecrementExpression extends SimpleNode {
  ASTPreDecrementExpression(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTPreDecrementExpression(id);
  }
}
