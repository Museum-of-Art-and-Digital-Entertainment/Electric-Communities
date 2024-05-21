/* JJT: 0.2.2 */

package ec.edoc;


public class ASTImplements extends SimpleNode {
  ASTImplements(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTImplements(id);
  }
}
