/* JJT: 0.2.2 */

package ec.edoc;


public class ASTResultType extends SimpleNode {
  ASTResultType(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTResultType(id);
  }
}
