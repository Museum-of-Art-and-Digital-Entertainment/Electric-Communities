/* JJT: 0.2.2 */

package ec.edoc;


public class ASTArgumentList extends SimpleNode {
  ASTArgumentList(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTArgumentList(id);
  }
}
