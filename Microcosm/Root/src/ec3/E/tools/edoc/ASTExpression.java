/* JJT: 0.2.2 */

package ec.edoc;


public class ASTExpression extends SimpleNode {
  ASTExpression(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTExpression(id);
  }
}
