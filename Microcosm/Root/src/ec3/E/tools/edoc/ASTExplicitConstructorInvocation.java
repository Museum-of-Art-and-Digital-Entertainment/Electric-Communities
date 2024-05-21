/* JJT: 0.2.2 */

package ec.edoc;


public class ASTExplicitConstructorInvocation extends SimpleNode {
  ASTExplicitConstructorInvocation(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTExplicitConstructorInvocation(id);
  }
}
