/* JJT: 0.2.2 */

package ec.edoc;


public class ASTAssignmentOperator extends SimpleNode {
  ASTAssignmentOperator(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTAssignmentOperator(id);
  }
}
