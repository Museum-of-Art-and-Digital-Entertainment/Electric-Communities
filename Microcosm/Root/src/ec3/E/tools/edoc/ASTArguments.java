/* JJT: 0.2.2 */

package ec.edoc;


public class ASTArguments extends SimpleNode {
  ASTArguments(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTArguments(id);
  }
}
