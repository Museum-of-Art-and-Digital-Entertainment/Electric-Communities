/* JJT: 0.2.2 */

package ec.edoc;


public class ASTAnnotation extends SimpleNode {
  ASTAnnotation(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTAnnotation(id);
  }
}
