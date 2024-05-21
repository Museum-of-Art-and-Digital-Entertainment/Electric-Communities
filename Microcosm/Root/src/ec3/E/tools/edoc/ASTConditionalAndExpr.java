/* JJT: 0.2.2 */

package ec.edoc;


public class ASTConditionalAndExpr extends SimpleNode {
  ASTConditionalAndExpr(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTConditionalAndExpr(id);
  }
}
