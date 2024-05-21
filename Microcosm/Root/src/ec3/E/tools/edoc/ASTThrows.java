/* JJT: 0.2.2 */

package ec.edoc;


public class ASTThrows extends SimpleNode {
  ASTThrows(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTThrows(id);
  }
}
