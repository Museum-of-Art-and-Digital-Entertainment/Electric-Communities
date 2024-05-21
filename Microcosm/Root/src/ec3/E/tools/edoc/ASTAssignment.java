/* JJT: 0.2.2 */

package ec.edoc;


public class ASTAssignment extends SimpleNode {
  ASTAssignment(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTAssignment(id);
  }
}
