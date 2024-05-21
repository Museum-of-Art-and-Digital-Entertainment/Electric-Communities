/* JJT: 0.2.2 */

package ec.edoc;


public class ASTCommand extends SimpleNode {
  ASTCommand(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTCommand(id);
  }
}
