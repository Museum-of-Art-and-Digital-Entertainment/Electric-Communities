/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBooleanLiteral extends SimpleNode {
  ASTBooleanLiteral(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBooleanLiteral(id);
  }
}
