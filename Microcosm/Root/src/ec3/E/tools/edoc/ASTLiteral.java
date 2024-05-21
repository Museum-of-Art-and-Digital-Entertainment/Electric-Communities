/* JJT: 0.2.2 */

package ec.edoc;


public class ASTLiteral extends SimpleNode {
  ASTLiteral(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTLiteral(id);
  }
}
