/* JJT: 0.2.2 */

package ec.edoc;


public class ASTForUpdate extends SimpleNode {
  ASTForUpdate(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTForUpdate(id);
  }
}
