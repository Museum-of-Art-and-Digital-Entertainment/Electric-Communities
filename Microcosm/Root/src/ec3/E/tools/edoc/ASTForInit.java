/* JJT: 0.2.2 */

package ec.edoc;


public class ASTForInit extends SimpleNode {
  ASTForInit(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTForInit(id);
  }
}
