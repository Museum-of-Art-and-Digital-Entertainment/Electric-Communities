/* JJT: 0.2.2 */

package ec.edoc;


public class ASTBlock extends SimpleNode {
  ASTBlock(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTBlock(id);
  }
}
