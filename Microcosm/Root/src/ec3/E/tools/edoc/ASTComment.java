/* JJT: 0.2.2 */

package ec.edoc;


public class ASTComment extends SimpleNode {
  ASTComment(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTComment(id);
  }
}
